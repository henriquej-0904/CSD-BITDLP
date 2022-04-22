package itdlp.tp1.data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import itdlp.tp1.api.Account;
import itdlp.tp1.api.AccountId;
import itdlp.tp1.api.UserId;
import itdlp.tp1.api.operations.InvalidOperationException;
import itdlp.tp1.api.operations.LedgerDeposit;
import itdlp.tp1.api.operations.LedgerOperation;
import itdlp.tp1.api.operations.LedgerTransaction;
import itdlp.tp1.util.Pair;
import itdlp.tp1.util.Result;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;

/**
 * An implementation of the LedgerDBlayer in Memory.
 */
public class LedgerDBinMemory extends LedgerDBlayer
{
    private static LedgerDBinMemory instance;

    private Map<AccountId, Account> accounts;
    private List<LedgerOperation> ledger;
    private ReadWriteLock lock;

    private Map<byte[], List<Integer>> nonceMap;

    /**
     * Get the current instance of the Ledger DB.
     * @return The Ledger DB.
     */
    public static LedgerDBinMemory getInstance()
    {
        if (instance != null)
            return instance;

        instance = new LedgerDBinMemory();
        return instance;
    }

    private LedgerDBinMemory()
    {
        this.accounts = new HashMap<>();
        this.nonceMap = new HashMap<>();
        this.ledger = new LinkedList<>();
        this.lock = new ReentrantReadWriteLock();
    }

    @Override
    public Result<Account> createAccount(Account account)
    {
        try
        {
            // check if account already exists.
            getReadLock().lock();

            if (this.accounts.containsKey(account.getId()))
                return accountAlreadyExistsConflict(account.getId());

            // insert account
            try 
            {
                getReadLock().unlock();
                getWriteLock().lock();

                if (this.accounts.putIfAbsent(account.getId(), account) != null)
                    return accountAlreadyExistsConflict(account.getId());

                return Result.ok(account);

            } finally
            {
                getReadLock().lock();
                getWriteLock().unlock();
            }

        } finally
        {
            getReadLock().unlock();
        }        
    }

    @Override
    public Result<Account> getAccount(AccountId accountId) {
        try
        {
            getReadLock().lock();

            Account account = this.accounts.get(accountId);

            if (account == null)
                return accountNotFound(accountId);

            return Result.ok(account);
        } finally
        {
            getReadLock().unlock();
        }
    }

    @Override
    public Result<Integer> getBalance(AccountId accountId) {
        Result<Account> accountRes = getAccount(accountId);
        if (accountRes.isOK())
            return Result.ok(accountRes.value().getBalance());

        return Result.error(accountRes.errorException());
    }

    @Override
    public Result<Integer> getTotalValue(AccountId[] accounts) {
        try
        {
            getReadLock().lock();

            int total = 0;

            for (AccountId accountId : accounts) {
                Account account = this.accounts.get(accountId);

                if (account == null)
                    return accountNotFound(accountId);

                total += account.getBalance();
            }

            return Result.ok(total);
        } finally
        {
            getReadLock().unlock();
        }
    }

    @Override
    public Result<Integer> getGlobalLedgerValue() {
        try
        {
            getReadLock().lock();

            int total = this.accounts.values().stream()
                .mapToInt(Account::getBalance)
                .sum();

            return Result.ok(total);
        } finally
        {
            getReadLock().unlock();
        }
    }

    @Override
    public Result<Void> loadMoney(LedgerDeposit deposit)
    {
        Result<Account> accountRes = getAccount(deposit.getId());
        if (!accountRes.isOK())
            return Result.error(accountRes.errorException());

        try
        {
            getWriteLock().lock();

            accountRes.value().processOperation(deposit);

            ledger.add(deposit);

            return Result.ok();
        } catch (InvalidOperationException e){
            return Result.error(new InternalServerErrorException(e.getMessage(), e));
        } finally
        {
            getWriteLock().unlock();
        }
    }

    @Override
    public Result<Void> sendTransaction(LedgerTransaction transaction) {
        
        Result<Account> accountOr = getAccount(transaction.getOrigin());
        Result<Account> accountDest = getAccount(transaction.getDest());

        if (!accountOr.isOK())
            return Result.error(accountOr.errorException());
        if (!accountDest.isOK())
            return Result.error(accountDest.errorException());

        try
        {
            getWriteLock().lock();

            accountOr.value().processOperation(transaction);
            accountDest.value().processOperation(transaction);

            ledger.add(transaction);

            return Result.ok();
        } catch (InvalidOperationException e){
            return Result.error(new WebApplicationException(e.getMessage(), e, Status.CONFLICT));
        } finally
        {
            getWriteLock().unlock();
        }
    }

    @Override
    public Result<LedgerOperation[]> getLedger() {
        try
        {
            getReadLock().lock();
            
            return Result.ok(ledger.toArray(new LedgerOperation[0]));
        } finally
        {
            getReadLock().unlock();
        }
    }

    @Override
    public synchronized Result<Boolean> nonceVerification(byte[] requestKey, int nonce){

        return Result.ok(addNonce(requestKey, nonce));
    }

    public boolean addNonce(byte[] requestKey, int nonce){
        boolean result = true;
        List<Integer> res = nonceMap.get(requestKey);

        if(res == null){
            res = new LinkedList<>();
            nonceMap.put(requestKey, res);
        }
        
        if(!res.contains(nonce))
            res.add(nonce);
        else
            result = false;
        
        return result;
    }

    private Lock getReadLock()
    {
        return this.lock.readLock();
    }

    private Lock getWriteLock()
    {
        return this.lock.writeLock();
    }

    @Override
    public Result<Void> loadState(Pair<AccountId, UserId>[] accounts, LedgerOperation[] operations) {

        try{
            this.lock.writeLock().lock();

            this.ledger = Stream.of(operations).toList();

            this.accounts = Stream.of(accounts).map((acc) -> new Account(acc.getLeft(), acc.getRight()))
            .collect(Collectors.toMap((acc) -> acc.getId(), (acc) -> acc));

            for (LedgerOperation lOp : operations) {
                if(lOp instanceof LedgerDeposit){

                    LedgerDeposit deposit = (LedgerDeposit) lOp;
                    Account acc = this.accounts.get(deposit.getId());
                    acc.processOperation(deposit);

                }else if(lOp instanceof LedgerTransaction){
                    
                    LedgerTransaction transaction = (LedgerTransaction) lOp;
                   
                    Account origin = this.accounts.get(transaction.getOrigin());
                    Account dest = this.accounts.get(transaction.getDest());

                    origin.processOperation(transaction);
                    dest.processOperation(transaction);
                    addNonce(transaction.digest(), transaction.getNonce());
                }  
            }
        } catch (InvalidOperationException e) {
            return Result.error(500);
        }finally{
            this.getWriteLock().unlock();
        }
                
        return Result.ok();
    }

    @Override
    public Result<Pair<Pair<AccountId, UserId>[], LedgerOperation[]>> getState() {
        // TODO Auto-generated method stub
        try{
            this.lock.readLock().lock();

            Pair<AccountId,UserId>[] accs = accounts.values().stream().map(a -> new Pair<>(a.getId(), a.getOwner()))
            .collect(Collectors.toList())
            .toArray(new Pair<AccountId, UserId>[0]));

            LedgerOperation[] ops = ledger.toArray(new LedgerOperation[0]);

            Pair<Pair<AccountId, UserId>[], LedgerOperation[]> result = new Pair<>(accs, ops);

            return Result.ok(result);
        } catch (InvalidOperationException e) {
            return Result.error(500);
        }finally{
            this.lock.readLock().unlock();
        }
    }
}
