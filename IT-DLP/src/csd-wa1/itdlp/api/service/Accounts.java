package itdlp.api.service;

import java.util.List;

import itdlp.api.Account;
import itdlp.api.AccountId;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path(Accounts.PATH)
public interface Accounts {

    static final String PATH="/account";

    /**
	 * Creates a new account.
	 *
	 * @param id account id
	 */
	@POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Account createAccount(AccountId id);

    /**
	 * Returns an account with the extract.
	 *
	 * @param id account id
	 */
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Account getAccount(AccountId id);

    /**
	 * Returns the balance of an account.
	 *
	 * @param id account id
	 */
    @Path("/balance")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    int getBalance(AccountId id);

    
    /**
     * Return total balance of account list
     * @param accs
     * @return total balance
     */
    @Path("/balance/sum")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    int getTotalValue(List<AccountId> accounts);

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
	 * @param id account id
     * @param value value to be loaded
	 */
    @Path("/balance")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    int loadMoney(AccountId id, int value);

    /**
	 * Transfers money from an origin to a destination.
	 *
	 * @param origin origin account id
     * @param dest destination account id
     * @param value value to be transfered
	 */
    @Path("/transaction")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    void sendTransaction(AccountId origin, AccountId dest, int value);

    /**
     * Obtains the current Ledger.
     * @return The current Ledger.
     */
    //Ledger getLedger();

}
