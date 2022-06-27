package tp2.bitdlp.pow.transaction.pool;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import tp2.bitdlp.pow.transaction.LedgerTransaction;

/**
 * A pool of valid transactions to mine.
 */
public class TransactionsToMine
{
    protected SortedSet<LedgerTransaction> transactions;
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
        lock = new ReentrantReadWriteLock();
    }

    public void addTransactions(List<LedgerTransaction> transactions)
    {
        lock.writeLock().lock();
        try
        {
            this.transactions.addAll(transactions);
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
