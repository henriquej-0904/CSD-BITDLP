package itdlp.data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import itdlp.api.Account;
import itdlp.api.AccountId;
import itdlp.api.operations.InvalidOperationException;
import itdlp.api.operations.LedgerDeposit;
import itdlp.api.operations.LedgerOperation;
import itdlp.api.operations.LedgerTransaction;
import itdlp.util.Result;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;

/**
 * An implementation of the LedgerDBlayer in Memory.
 * TODO: implement the class.
 */
public class LedgerDBinMemory extends LedgerDBlayer
{
    private static LedgerDBinMemory instance;


    private Map<AccountId, Account> accounts;
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
        this.accounts = new HashMap<>();
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
    public Result<Void> loadMoney(AccountId id, LedgerDeposit deposit)
    {
        Result<Account> accountRes = getAccount(id);
        if (!accountRes.isOK())
            return Result.error(accountRes.errorException());

        try
        {
            getWriteLock().lock();
            accountRes.value().processOperation(deposit);

            return Result.ok();
        } catch (InvalidOperationException e){
            return Result.error(new InternalServerErrorException(e));
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

            return Result.ok();
        } catch (InvalidOperationException e){
            return Result.error(new WebApplicationException(e, Status.CONFLICT));
        } finally
        {
            getWriteLock().unlock();
        }
    }

    private Lock getReadLock()
    {
        return this.lock.readLock();
    }

    private Lock getWriteLock()
    {
        return this.lock.writeLock();
    }
}
