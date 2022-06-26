package tp2.bitdlp.data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import tp2.bitdlp.api.Account;
import tp2.bitdlp.api.AccountId;
import tp2.bitdlp.api.UserId;
import tp2.bitdlp.pow.transaction.InvalidTransactionException;
import tp2.bitdlp.pow.transaction.LedgerTransaction;
import tp2.bitdlp.util.Pair;
import tp2.bitdlp.util.result.Result;
import tp2.bitdlp.util.Utils;
import jakarta.ws.rs.ForbiddenException;
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
    private List<LedgerTransaction> ledger;
    private ReadWriteLock lock;

    private Map<String, List<Integer>> nonceMap;

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
        this.accounts = new TreeMap<>();
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
    public Result<Void> sendTransaction(LedgerTransaction transaction)
    {
        Result<Account> accountOr = getAccount(transaction.getOrigin());
        Result<Account> accountDest = getAccount(transaction.getDest());

        if (!accountOr.isOK())
            return Result.error(accountOr.errorException());
        if (!accountDest.isOK())
            return Result.error(accountDest.errorException());

        try
        {
            getWriteLock().lock();

            addNonce(transaction.digest(), transaction.getNonce());   

            accountOr.value().processOperation(transaction);
            accountDest.value().processOperation(transaction);

            ledger.add(transaction);

            return Result.ok();
        } catch (InvalidTransactionException e){
            return Result.error(new WebApplicationException(e.getMessage(), e, Status.CONFLICT));
        }catch (WebApplicationException e){
            return Result.error(e);
        }
         finally
        {
            getWriteLock().unlock();
        }
    }

    @Override
    public Result<LedgerTransaction[]> getLedger() {
        try
        {
            getReadLock().lock();
            
            return Result.ok(ledger.toArray(new LedgerTransaction[0]));
        } finally
        {
            getReadLock().unlock();
        }
    }

    @Override
    public Result<Void> loadState(LedgerState state) {

        try
        {
            getWriteLock().lock();

            this.ledger = state.getTransactions();

            this.accounts = state.getAccounts().stream()
                .map((pairAccountUser) -> new Account(pairAccountUser.getLeft(), pairAccountUser.getRight()))
                .collect(Collectors.toMap(Account::getId, (acc) -> acc, (acc1, acc2) -> acc1, () -> new TreeMap<>()));

            for (LedgerTransaction transaction : this.ledger) {
                    Account origin = this.accounts.get(transaction.getOrigin());
                    Account dest = this.accounts.get(transaction.getDest());

                    addNonce(transaction.digest(), transaction.getNonce());
                    origin.processOperation(transaction);
                    dest.processOperation(transaction);
            }

            return Result.ok();
        } catch (InvalidTransactionException e) {
            return Result.error(new InternalServerErrorException(e.getMessage(), e));
        }finally{
            getWriteLock().unlock();
        }
    }

    @Override
    public Result<LedgerState> getState() {
        try{
            getReadLock().lock();

            List<Pair<AccountId, UserId>> accounts =
                this.accounts.values().stream()
                .map(account -> new Pair<>(account.getId(), account.getOwner()))
                .collect(Collectors.toList());

            return Result.ok(new LedgerState(accounts, this.ledger));
        } finally{
            getReadLock().unlock();
        }
    }

    protected void addNonce(byte[] requestKey, int nonce){
        String hash = Utils.toBase64(requestKey);
        List<Integer> res = nonceMap.get(hash);

        if(res == null){
            res = new LinkedList<>();
            nonceMap.put(hash, res);
        }
        
        if(!res.contains(nonce))
            res.add(nonce);
        else
            throw new ForbiddenException("Invalid Nonce.");    
    }

    protected Lock getReadLock()
    {
        return this.lock.readLock();
    }

    protected Lock getWriteLock()
    {
        return this.lock.writeLock();
    }
}
