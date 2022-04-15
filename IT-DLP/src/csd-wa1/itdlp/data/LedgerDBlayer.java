package itdlp.data;

import java.util.List;

import itdlp.api.Account;
import itdlp.api.AccountId;

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
    public static LedgerDBlayer getInstance()
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
	 * @param id account id
	 */
    public abstract Account createAccount(AccountId id);

    /**
	 * Returns an account with the extract.
	 *
	 * @param id account id
	 */
    public abstract Account getAccount(AccountId id);

    /**
	 * Returns the balance of an account.
	 *
	 * @param id account id
	 */
    public abstract int getBalance(AccountId id);

    
    /**
     * Return total balance of account list
     * @param accs
     * @return total balance
     */
    public abstract int getTotalValue(List<AccountId> accounts);

    /**
     * Return total amount of value registered in the ledger
     * @return total balance
     */
    public abstract int getGlobalLedgerValue();

    /**
	 * Loads money into an account.
	 *
	 * @param id account id
     * @param value value to be loaded
	 */
    public abstract int loadMoney(AccountId id, int value);

    /**
	 * Transfers money from an origin to a destination.
	 *
	 * @param origin origin account id
     * @param dest destination account id
     * @param value value to be transfered
	 */
    public abstract void sendTransaction(AccountId origin, AccountId dest, int value);

    /**
     * Obtains the current Ledger.
     * @return The current Ledger.
     */
    //public abstract Ledger getLedger();
}
