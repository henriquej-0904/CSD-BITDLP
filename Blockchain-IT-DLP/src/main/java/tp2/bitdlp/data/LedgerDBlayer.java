package tp2.bitdlp.data;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import tp2.bitdlp.api.Account;
import tp2.bitdlp.api.AccountId;
import tp2.bitdlp.data.mongo.LedgerDBWithMongo;
import tp2.bitdlp.pow.block.BCBlock;
import tp2.bitdlp.pow.transaction.LedgerTransaction;
import tp2.bitdlp.util.result.Result;
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

    public static DBtype dbType;

    private static LedgerDBlayer instance;
    
    /**
     * Get the current instance of the Ledger DB.
     * @return The Ledger DB.
     */
    public static synchronized LedgerDBlayer getInstance() throws LedgerDBlayerException
    {
        if (instance == null)
        {
            dbType = getDBType();
            
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

    private static DBtype getDBType() throws LedgerDBlayerException
    {
        Properties props = new Properties();
        try (InputStream input = new FileInputStream("db-config.properties"))
        {
            props.load(input);
            String type = props.getProperty("DB_TYPE");

            if (type == null)
                throw new LedgerDBlayerException("DB_TYPE not specified.");

            return DBtype.valueOf(type.toUpperCase());
        } catch (Exception e) {
            throw new LedgerDBlayerException(e.getMessage(), e);
        }
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
	 * Transfers money from an origin to a destination.
	 *
	 * @param transaction The transaction to perform.
	 */
    //public abstract Result<Void> sendTransaction(LedgerTransaction transaction);

    /**
     * Obtains the current Ledger.
     * @return The current Ledger.
     */
    public abstract Result<BCBlock[]> getLedger();

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



    /**
	 * Verifies if the transaction is valid.
	 *
	 * @param transaction The transaction to perform.
	 */
    public abstract Result<Void> verifySendTransaction(LedgerTransaction transaction);

    /**
     * Check if the blockchain is empty.
     * @return true if the blockchain is empty.
     */
    public abstract Result<Boolean> emptyBlockchain();

    /**
     * Get the hash of the last block in the blockchain.
     * @return The hash of the last block in the blockchain.
     */
    public abstract Result<String> getPreviousBlockHash();

    /**
     * Add a block to the ledger.
     * @param block
     * @return The hash of the block if success.
     */
    public abstract Result<String> addBlock(BCBlock block);


    protected <T> Result<T> accountAlreadyExistsConflict(AccountId id)
    {
        return Result.error(new WebApplicationException(String.format("Account %s already exists.", id), Status.CONFLICT));
    }

    protected <T> Result<T> accountNotFound(AccountId id)
    {
        return Result.error(new NotFoundException(String.format("Account %s does not exist.", id)));
    }
}
