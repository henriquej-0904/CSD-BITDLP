package itdlp.data;

import java.util.List;

import itdlp.api.Account;
import itdlp.api.AccountId;

/**
 * The interface to the Ledger DB Layer.
 */
public interface LedgerDBlayer
{
    /**
	 * Creates a new account.
	 *
	 * @param id account id
	 */
    Account createAccount(AccountId id);

    /**
	 * Returns an account with the extract.
	 *
	 * @param id account id
	 */
    Account getAccount(AccountId id);

    /**
	 * Returns the balance of an account.
	 *
	 * @param id account id
	 */
    int getBalance(AccountId id);

    
    /**
     * Return total balance of account list
     * @param accs
     * @return total balance
     */
    int getTotalValue(List<AccountId> accounts);

    /**
     * Return total amount of value registered in the ledger
     * @return total balance
     */
    int getGlobalLedgerValue();

    /**
	 * Loads money into an account.
	 *
	 * @param id account id
     * @param value value to be loaded
	 */
    int loadMoney(AccountId id, int value);

    /**
	 * Transfers money from an origin to a destination.
	 *
	 * @param origin origin account id
     * @param dest destination account id
     * @param value value to be transfered
	 */
    void sendTransaction(AccountId origin, AccountId dest, int value);

    /**
     * Obtains the current Ledger.
     * @return The current Ledger.
     */
    //Ledger getLedger();
}
