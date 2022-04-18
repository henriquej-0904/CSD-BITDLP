package itdlp.api.service;

import java.util.Map;

import itdlp.api.Account;
import itdlp.api.AccountId;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * The Accounts API
 */
@Path(Accounts.PATH)
public interface Accounts {

    static final String PATH="/account";

    /**
	 * Creates a new account.
	 *
	 * @param accountId account id
     * @param ownerId the owner of the account
     * 
     * @return The account object.
	 */
	@POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Account createAccount(byte[] accountId, byte[] ownerId);

    /**
	 * Returns an account with the extract.
	 *
	 * @param accountId account id
     * 
     * @return The account object.
	 */
    @GET
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    Account getAccount(byte[] accountId);

    /**
	 * Returns the balance of an account.
	 *
	 * @param accountId account id
     * 
     * @return The balance of the account.
	 */
    @Path("/balance")
    @GET
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    int getBalance(byte[] accountId);

    
    /**
     * Return total balance of account list
     * @param accounts
     * @return total balance
     */
    @Path("/balance/sum")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    int getTotalValue(byte[][] accounts);

    /**
     * Return total amount of value registered in the ledger
     * @return total balance
     */
    @Path("/balance/ledger")
    @GET
    int getGlobalLedgerValue();

    /**
	 * Loads money into an account.
	 *
	 * @param accountId account id
     * @param value value to be loaded
	 */
    @Path("/balance/{value}")
    @POST
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    void loadMoney(byte[] accountId, @PathParam("value") int value);

    /**
	 * Transfers money from an origin to a destination.
	 *
	 * @param origin origin account id
     * @param dest destination account id
     * @param value value to be transfered
	 */
    @Path("/transaction/{value}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    void sendTransaction(byte[] origin, byte[] dest, @PathParam("value") int value);

    /**
     * Obtains the current Ledger.
     * @return The current Ledger.
     */
    Map<AccountId,Account> getLedger();

}
