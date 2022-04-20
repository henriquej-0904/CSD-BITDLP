package itdlp.tp1.api.service;

import itdlp.tp1.api.Account;
import itdlp.tp1.util.Pair;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * The Accounts API
 */
@Path(Accounts.PATH)
public interface Accounts {

    static final String PATH="/account";

    static final String USER_SIG = "User-signature";
    static final String ACC_SIG = "Account-signature";
    static final String NONCE = "Nonce";

    /**
	 * Creates a new account.
	 *
     * @param accountUserPair - A pair of accountId and ownerId.
     * @param userSignature The signature of the user
     * 
     * @return The account object.
	 */
	@POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Account createAccount(Pair<byte[],byte[]> accountUserPair, @HeaderParam(USER_SIG) String userSignature);

    /**
	 * Returns an account with the extract.
	 *
	 * @param accountId account id
     * 
     * @return The account object.
	 */
    @GET
    @Path("/{accountId}")
    @Produces(MediaType.APPLICATION_JSON)
    Account getAccount(@PathParam("accountId") String accountId);

    /**
	 * Returns the balance of an account.
	 *
	 * @param accountId account id
     * 
     * @return The balance of the account.
	 */
    @Path("/balance/{accountId}")
    @GET
    int getBalance(@PathParam("accountId") String accountId);

    
    /**
     * Return total balance of account list
     * @param accounts
     * @return total balance
     */
    @Path("/balance/sum")
    @POST
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
     * @param accountSignature The signature of the account.
	 */
    @Path("/balance/{value}")
    @POST
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    void loadMoney(byte[] accountId, @PathParam("value") int value, @HeaderParam(ACC_SIG) String accountSignature);

    /**
	 * Transfers money from an origin to a destination.
	 *
     * @param originDestPair A pair of origin and destination accounts.
     * @param value value to be transfered
     * @param accountSignature The signature of the account.
     * @param nonce The nonce.
	 */
    @Path("/transaction/{value}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    void sendTransaction(Pair<byte[],byte[]> originDestPair, @PathParam("value") int value,
        @HeaderParam(ACC_SIG) String accountSignature, @HeaderParam(NONCE) int nonce);

    /**
     * Obtains the current Ledger.
     * @return The current Ledger.
     */
    @Path("/ledger")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Account[] getLedger();

}
