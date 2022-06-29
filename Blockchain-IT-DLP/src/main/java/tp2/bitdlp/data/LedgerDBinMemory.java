package tp2.bitdlp.data;

import java.util.HashMap;
import java.util.Iterator;
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
import tp2.bitdlp.pow.block.BCBlock;
import tp2.bitdlp.pow.transaction.InvalidTransactionException;
import tp2.bitdlp.pow.transaction.LedgerTransaction;
import tp2.bitdlp.util.Pair;
import tp2.bitdlp.util.result.Result;
import tp2.bitdlp.util.Utils;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;

/**
 * An implementation of the LedgerDBlayer in Memory.
 */
public class LedgerDBinMemory extends LedgerDBlayer
{
    private static LedgerDBinMemory instance;

    private Map<AccountId, Account> accounts;
    private Map<String, List<Integer>> nonceMap;

    private List<BCBlock> ledger;
    private boolean emptyLedger;
    private String previousBlockHash;

    private ReadWriteLock lock;

    

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
        this.emptyLedger = true;
        this.previousBlockHash = null;
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


    /* public Result<Void> sendTransaction(LedgerTransaction transaction)
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
    } */


    @Override
    public Result<BCBlock[]> getLedger() {
        try
        {
            getReadLock().lock();
            
            return Result.ok(ledger.toArray(new BCBlock[0]));
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

            this.ledger = state.getLedger();

            this.accounts = state.getAccounts().stream()
                .map((pairAccountUser) -> new Account(pairAccountUser.getLeft(), pairAccountUser.getRight()))
                .collect(Collectors.toMap(Account::getId, (acc) -> acc, (acc1, acc2) -> acc1, () -> new TreeMap<>()));

            for (BCBlock block : this.ledger)
                processBlockTransactions(block.getTransactions().getTransactions());

            this.emptyLedger = this.ledger.isEmpty();

            this.previousBlockHash = this.emptyLedger ? null :
                Utils.toHex(this.ledger.get(this.ledger.size() - 1).digest());

            return Result.ok();
        } catch (Exception e) {
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

    @Override
    public Result<Boolean> emptyBlockchain() {
        if (!this.emptyLedger)
            return Result.ok(false);
        else
        {
            try{
                getReadLock().lock();

                return Result.ok(this.emptyLedger);
            } finally{
                getReadLock().unlock();
            }
        }
    }

    @Override
    public Result<String> getPreviousBlockHash()
    {
        try{
                getReadLock().lock();
                
                if (this.previousBlockHash == null)
                    return Result.error(new NotFoundException("The ledger is empty"));
                else
                    return Result.ok(this.previousBlockHash);
            } finally{
                getReadLock().unlock();
            }
    }

    @Override
    public Result<Void> verifySendTransaction(LedgerTransaction transaction)
    {
        Result<Account> accountOr = getAccount(transaction.getOrigin());
        Result<Account> accountDest = getAccount(transaction.getDest());

        if (!accountOr.isOK())
            return Result.error(accountOr.errorException());
        if (!accountDest.isOK())
            return Result.error(accountDest.errorException());

        try
        {
            getReadLock().lock();

            if (!verifyNonce(transaction.digest(), transaction.getNonce()))
                return Result.error(new ForbiddenException("Invalid Nonce."));

            // verify balance
            accountOr.value().verifyTransaction(transaction);

            return Result.ok();
        } catch (InvalidTransactionException e){
            return Result.error(new WebApplicationException(e.getMessage(), e, Status.CONFLICT));
        }catch (WebApplicationException e){
            return Result.error(e);
        }
         finally
        {
            getReadLock().unlock();
        }
    }

    @Override
    public Result<String> addBlock(BCBlock block)
    {
        // Add block to ledger
        // update aux data structures.

        try
        {
            getWriteLock().lock();

            /* if (!this.emptyLedger &&
                !this.previousBlockHash.equals(block.getHeader().getPreviousHash()))
                return Result.error(new WebApplicationException(
                    "Invalid previous block hash!", Status.CONFLICT)); */

            this.ledger.add(block);

            this.emptyLedger = false;
            this.previousBlockHash = Utils.toHex(block.digest());

            processBlockTransactions(block.getTransactions().getTransactions());

            return Result.ok(this.previousBlockHash);
            
        } catch (Exception e){
            return Result.error(new InternalServerErrorException(e.getMessage(), e));
        } finally{
            getWriteLock().unlock();
        }
    }

    /**
     * Process the transactions of a block.
     * The first transaction is a generation transaction.
     * @param transactions
     */
    protected void processBlockTransactions(List<LedgerTransaction> transactions)
    {
        if (transactions.isEmpty())
            return;
        
        Iterator<LedgerTransaction> it = transactions.iterator();
        Account origin, dest;

        LedgerTransaction transaction = it.next();

        // process generation transaction
        dest = this.accounts.get(transaction.getDest());
        dest.processOperation(transaction);

        // process the rest of the transactions.
        while (it.hasNext())
        {
            transaction = it.next();
            origin = this.accounts.get(transaction.getOrigin());
            dest = this.accounts.get(transaction.getDest());

            addNonce(transaction.getHash(), transaction.getNonce());
            origin.processOperation(transaction);
            dest.processOperation(transaction);
        }
    }

    /**
     * Verifies if the specified nonce is valid.
     * 
     * @param digest
     * @param nonce
     * @return true if it is valid, false otherwise.
     */
    protected boolean verifyNonce(byte[] digest, int nonce)
    {
        String hash = Utils.toBase64(digest);
        List<Integer> res = nonceMap.get(hash);

        if(res == null){
            res = new LinkedList<>();
            nonceMap.put(hash, res);
        }
        
        return !res.contains(nonce);
    }

    protected void addNonce(byte[] digest, int nonce){
        String hash = Utils.toBase64(digest);
        List<Integer> res = nonceMap.get(hash);

        if(res == null){
            res = new LinkedList<>();
            nonceMap.put(hash, res);
        }
        
        res.add(nonce);  
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
