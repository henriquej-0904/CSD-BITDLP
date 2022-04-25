package itdlp.tp1.data;

import itdlp.tp1.api.Account;
import itdlp.tp1.api.AccountId;
import itdlp.tp1.api.operations.LedgerDeposit;
import itdlp.tp1.api.operations.LedgerOperation;
import itdlp.tp1.api.operations.LedgerTransaction;
import itdlp.tp1.data.mongo.LedgerDBWithMongo;
import itdlp.tp1.util.Result;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;

/**
 * The interface to the Ledger DB Layer.
 */
public abstract class LedgerDBlayer
{
    /**
     * The type of the LedgerDB.
     */
    public static enum DBtype
    {
        /**
         * Represents a DB stored in memory.
         */
        IN_MEMORY,

        /**
         * Represents a mongo DB.
         */
        MONGO;
    }

    public static final DBtype dbType = DBtype.MONGO;

    private static LedgerDBlayer instance;
    
    /**
     * Get the current instance of the Ledger DB.
     * @return The Ledger DB.
     */
    public static synchronized LedgerDBlayer getInstance() throws LedgerDBlayerException
    {
        if (instance == null)
        {
            switch (dbType) {
                case IN_MEMORY:
                    instance = LedgerDBinMemory.getInstance();
                    break;
                case MONGO:
                    instance = LedgerDBWithMongo.getInstance();
                    break;
            }
        }

        return instance;
    }


    /**
	 * Creates a new account.
	 *
	 * @param account The new account
     * 
     * @return The created account object.
	 */
    public abstract Result<Account> createAccount(Account account);

    /**
	 * Returns an account with the extract.
	 *
	 * @param accountId account id
     * 
     * @return The account object.
	 */
    public abstract Result<Account> getAccount(AccountId accountId);

    /**
	 * Returns the balance of an account.
	 *
	 * @param accountId account id
     * 
     * @return The balance of the account.
	 */
    public abstract Result<Integer> getBalance(AccountId accountId);

    
    /**
     * Return total balance of account list
     * @param accounts
     * @return total balance
     */
    public abstract Result<Integer> getTotalValue(AccountId[] accounts);

    /**
     * Return total amount of value registered in the ledger
     * @return total balance
     */
    public abstract Result<Integer> getGlobalLedgerValue();

    /**
	 * Loads money into an account.
	 *
     * @param deposit The value to load into the account.
	 */
    public abstract Result<Void> loadMoney(LedgerDeposit deposit);

    /**
	 * Transfers money from an origin to a destination.
	 *
	 * @param transaction The transaction to perform.
	 */
    public abstract Result<Void> sendTransaction(LedgerTransaction transaction);

    /**
     * Obtains the current Ledger.
     * @return The current Ledger.
     */
    public abstract Result<LedgerOperation[]> getLedger();

    /**
     * Load a ledger state
     * @param state the state of the ledger
     */
    public abstract Result<Void> loadState(LedgerState state);

    /**
     * get current ledger state
     * @return ledger state
     */
    public abstract Result<LedgerState> getState();
    

    protected <T> Result<T> accountAlreadyExistsConflict(AccountId id)
    {
        return Result.error(new WebApplicationException(String.format("Account %s already exists.", id), Status.CONFLICT));
    }

    protected <T> Result<T> accountNotFound(AccountId id)
    {
        return Result.error(new NotFoundException(String.format("Account %s does not exist.", id)));
    }
}
