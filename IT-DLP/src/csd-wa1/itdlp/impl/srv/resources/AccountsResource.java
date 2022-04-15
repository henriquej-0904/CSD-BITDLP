package itdlp.impl.srv.resources;

import java.util.stream.Stream;

import itdlp.api.Account;
import itdlp.api.AccountId;
import itdlp.api.service.Accounts;
import itdlp.data.LedgerDBlayer;
import itdlp.data.LedgerDBlayerException;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;

public abstract class AccountsResource implements Accounts
{
    protected LedgerDBlayer db;

    /**
     * Init the db layer instance.
     */
    protected void init()
    {
        try {
            this.db = LedgerDBlayer.getInstance();
        } catch (LedgerDBlayerException e) {
            throw new InternalServerErrorException(e);
        }
    }

    /**
     * Get the accountId from the specified id.
     * @param accountId
     * @return The AccountId object.
     * @throws BadRequestException if the id is not valid.
     */
    protected AccountId getAccountId(byte[] accountId) throws BadRequestException
    {
        try {
            return new AccountId(accountId);
        } catch (Exception e) {
            throw new BadRequestException(e);
        }
    }
    

    @Override
    public final Account createAccount(byte[] accountId) {
        init();
        return createAccount(getAccountId(accountId));
    }

    /**
	 * Creates a new account.
	 *
	 * @param accountId account id
     * 
     * @return The account object.
	 */
    public abstract Account createAccount(AccountId accountId);



    @Override
    public final Account getAccount(byte[] accountId) {
        init();
        return getAccount(getAccountId(accountId));
    }

    /**
	 * Returns an account with the extract.
	 *
	 * @param accountId account id
     * 
     * @return The account object.
	 */
    public abstract Account getAccount(AccountId accountId);



    @Override
    public final int getBalance(byte[] accountId) {
        init();
        return getBalance(getAccountId(accountId));
    }

    /**
	 * Returns the balance of an account.
	 *
	 * @param accountId account id
     * 
     * @return The balance of the account.
	 */
    public abstract int getBalance(AccountId accountId);



    @Override
    public final int getTotalValue(byte[][] accounts) {
        init();
        
        if (accounts == null || accounts.length == 0)
            throw new BadRequestException();

        AccountId[] accountIds = (AccountId[]) Stream.of(accounts)
            .map(this::getAccountId)
            .toArray();

        return getTotalValue(accountIds);
    }

    /**
     * Return total balance of account list
     * @param accounts
     * @return total balance
     */
    public abstract int getTotalValue(AccountId[] accounts);



    @Override
    public final void loadMoney(byte[] accountId, int value) {
        init();
        loadMoney(getAccountId(accountId), value);
    }

    /**
	 * Loads money into an account.
	 *
	 * @param accountId account id
     * @param value value to be loaded
	 */
    public abstract void loadMoney(AccountId accountId, int value);



    @Override
    public final void sendTransaction(byte[] origin, byte[] dest, int value) {
        init();
        sendTransaction(getAccountId(origin), getAccountId(dest), value);
    }

    /**
	 * Transfers money from an origin to a destination.
	 *
	 * @param origin origin account id
     * @param dest destination account id
     * @param value value to be transfered
	 */
    public abstract void sendTransaction(AccountId origin, AccountId dest, int value);
    
}
