package itdlp.impl.srv.resources;

import java.util.logging.Logger;
import java.util.stream.Stream;

import itdlp.api.Account;
import itdlp.api.AccountId;
import itdlp.api.UserId;
import itdlp.api.service.Accounts;
import itdlp.data.LedgerDBlayer;
import itdlp.data.LedgerDBlayerException;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.WebApplicationException;

public abstract class AccountsResource implements Accounts
{
    protected static final Logger LOG = Logger.getLogger(AccountsResource.class.getSimpleName());

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

    /**
     * Get the userId from the specified id.
     * @param userId
     * @return The UserId object.
     * @throws BadRequestException if the id is not valid.
     */
    protected UserId getUserId(byte[] userId) throws BadRequestException
    {
        try {
            return new UserId(userId);
        } catch (Exception e) {
            throw new BadRequestException(e);
        }
    }
    

    @Override
    public final Account createAccount(byte[] accountId, byte[] ownerId) {
        try {
            init();

            AccountId account = getAccountId(accountId);
            UserId owner = getUserId(ownerId);

            LOG.info(String.format("accountId=%s, ownerId=%s", account, owner));

            return createAccount(new Account(account, owner));
        } catch (WebApplicationException e) {
            LOG.info(e.getMessage());
            throw e;
        }
    }

    /**
	 * Creates a new account.
	 *
	 * @param account The new account
     * 
     * @return The created account object.
	 */
    public abstract Account createAccount(Account account);



    @Override
    public final Account getAccount(byte[] accountId) {
        try {
            init();
            
            AccountId id = getAccountId(accountId);
            LOG.info(id.toString());

            return getAccount(id);
        } catch (WebApplicationException e) {
            LOG.info(e.getMessage());
            throw e;
        }
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
        try {
            init();
            
            AccountId id = getAccountId(accountId);
            LOG.info(id.toString());

            return getBalance(id);
        } catch (WebApplicationException e) {
            LOG.info(e.getMessage());
            throw e;
        }
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
        try {
            init();
            
            if (accounts == null || accounts.length == 0)
                throw new BadRequestException();

            AccountId[] accountIds = (AccountId[]) Stream.of(accounts)
                .map(this::getAccountId)
                .toArray();

            return getTotalValue(accountIds);
        } catch (WebApplicationException e) {
            LOG.info(e.getMessage());
            throw e;
        }
    }

    /**
     * Return total balance of account list
     * @param accounts
     * @return total balance
     */
    public abstract int getTotalValue(AccountId[] accounts);



    @Override
    public final void loadMoney(byte[] accountId, int value) {
        try {
            init();
            
            AccountId id = getAccountId(accountId);
            LOG.info(String.format("%s, value=%d", id, value));

            loadMoney(id, value);
        } catch (WebApplicationException e) {
            LOG.info(e.getMessage());
            throw e;
        }
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
        try {
            init();
            
            AccountId originId = getAccountId(origin);
            AccountId destId = getAccountId(dest);
            LOG.info(String.format("Origin %s, Dest %s, value=%d",
                originId, destId, value));

            sendTransaction(originId, destId, value);
        } catch (WebApplicationException e) {
            LOG.info(e.getMessage());
            throw e;
        }
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
