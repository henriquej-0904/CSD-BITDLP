package tp2.bitdlp.pow.transaction.pool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import tp2.bitdlp.api.AccountId;
import tp2.bitdlp.pow.transaction.LedgerTransaction;
import tp2.bitdlp.util.result.Result;

/**
 * A pool of valid transactions to mine.
 */
public class TransactionsToMine
{
    protected SortedSet<LedgerTransaction> transactions;

    protected Map<AccountId, Integer> accountPoolValue;


    protected ReadWriteLock lock;

    protected static TransactionsToMine instance;

    public static synchronized TransactionsToMine getInstance()
    {
        if (instance == null)
            instance = new TransactionsToMine();

        return instance;
    }

    /**
     * 
     */
    protected TransactionsToMine() {
        transactions = new TreeSet<>();
        accountPoolValue = new HashMap<>();
        lock = new ReentrantReadWriteLock();
    }

    public Result<Void> addTransaction(LedgerTransaction transaction, int originBalance)
    {
        lock.writeLock().lock();
        try
        {
            // verify origin account balance
            Integer originPoolValue = this.accountPoolValue.get(transaction.getOrigin());
            if (originPoolValue == null || originBalance + originPoolValue >= 0)
            {
                boolean result = this.transactions.add(transaction);
                if (!result)
                    return Result.error(new WebApplicationException("The transaction already exists in the pool.", Status.CONFLICT));

                // update pool value
                originPoolValue = originPoolValue == null ? -transaction.getValue() : originPoolValue - transaction.getValue();
                this.accountPoolValue.put(transaction.getOrigin(), originPoolValue);

                Integer destPoolValue = this.accountPoolValue.get(transaction.getDest());
                destPoolValue = destPoolValue == null ? transaction.getValue() : destPoolValue + transaction.getValue();
                this.accountPoolValue.put(transaction.getDest(), destPoolValue);

                return Result.ok();
            }
                return Result.error(new WebApplicationException("The transaction is invalid vecause origin account balance is not enough.", Status.CONFLICT));
        } finally
        {
            lock.writeLock().unlock();
        }
    }

    public List<LedgerTransaction> getTransactions(int n)
    {
        lock.readLock().lock();
        try
        {
            if (this.transactions.size() < n)
                return null;

            return this.transactions.stream()
                .limit(n).collect(Collectors.toList());
        } finally
        {
            lock.readLock().unlock();
        }
    }

    public void removeTransactions(List<LedgerTransaction> transactions)
    {
        lock.writeLock().lock();
        try
        {
            this.transactions.removeAll(transactions);
        } finally
        {
            lock.writeLock().unlock();
        }
    }
}
