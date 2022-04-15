package itdlp.data;

import itdlp.api.Account;
import itdlp.api.AccountId;
import itdlp.util.Result;

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
        IN_MEMORY;
    }

    public static final DBtype dbType = DBtype.IN_MEMORY;

    private static LedgerDBlayer instance;
    
    /**
     * Get the current instance of the Ledger DB.
     * @return The Ledger DB.
     */
    public static LedgerDBlayer getInstance() throws LedgerDBlayerException
    {
        if (instance == null)
        {
            switch (dbType) {
                case IN_MEMORY:
                    instance = LedgerDBinMemory.getInstance();
                    break;
            }
        }

        return instance;
    }


    /**
	 * Creates a new account.
	 *
	 * @param accountId account id
     * 
     * @return The account object.
	 */
    public abstract Result<Account> createAccount(AccountId accountId);

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
	 * @param id account id
     * @param value value to be loaded
	 */
    public abstract Result<Integer> loadMoney(AccountId id, int value);

    /**
	 * Transfers money from an origin to a destination.
	 *
	 * @param origin origin account id
     * @param dest destination account id
     * @param value value to be transfered
	 */
    public abstract Result<Void> sendTransaction(AccountId origin, AccountId dest, int value);

    /**
     * Obtains the current Ledger.
     * @return The current Ledger.
     */
    //public abstract Result<Ledger> getLedger();
}
