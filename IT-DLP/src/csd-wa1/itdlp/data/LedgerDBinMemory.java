package itdlp.data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import itdlp.api.Account;
import itdlp.api.AccountId;
import itdlp.util.Result;

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
            lock(true);

            if (this.accounts.containsKey(account.getId()))
                return accountAlreadyExistsConflict(account.getId());

            // insert account
            try 
            {
                unlock(true);
                lock(false);

                if (this.accounts.putIfAbsent(account.getId(), account) != null)
                    return accountAlreadyExistsConflict(account.getId());

                return Result.ok(account);

            } finally
            {
                lock(true);
                unlock(false);
            }

        } finally
        {
            unlock(true);
        }        
    }

    @Override
    public Result<Account> getAccount(AccountId accountId) {
        try
        {
            lock(true);

            Account account = this.accounts.get(accountId);

            if (account == null)
                return accountNotFound(accountId);

            return Result.ok(account);
        } finally
        {
            unlock(true);
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
            lock(true);

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
            unlock(true);
        }
    }

    @Override
    public Result<Integer> getGlobalLedgerValue() {
        try
        {
            lock(true);

            int total = this.accounts.values().stream()
                .mapToInt(Account::getBalance)
                .sum();

            return Result.ok(total);
        } finally
        {
            unlock(true);
        }
    }

    @Override
    public Result<Integer> loadMoney(AccountId id, int value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Result<Void> sendTransaction(AccountId origin, AccountId dest, int value) {
        // TODO Auto-generated method stub
        return null;
    }

    private void lock(boolean read)
    {
        Lock lock = read ? this.lock.readLock() : this.lock.writeLock();
        lock.lock();
    }

    private void unlock(boolean read)
    {
        Lock lock = read ? this.lock.readLock() : this.lock.writeLock();
        lock.unlock();
    }
}
