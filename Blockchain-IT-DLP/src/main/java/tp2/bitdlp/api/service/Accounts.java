package tp2.bitdlp.api.service;

import tp2.bitdlp.api.Account;
import tp2.bitdlp.pow.block.BCBlock;
import tp2.bitdlp.pow.transaction.LedgerTransaction;
import tp2.bitdlp.util.Pair;
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
    static final String SERVER_SIG = "Server-signature";
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
    @Produces(MediaType.APPLICATION_JSON)
    LedgerTransaction sendTransaction(Pair<byte[],byte[]> originDestPair, @PathParam("value") int value,
        @HeaderParam(ACC_SIG) String accountSignature, @HeaderParam(NONCE) int nonce);

    /**
     * Obtains the current Ledger.
     * @return The current Ledger.
     */
    @Path("/ledger")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    LedgerTransaction[] getLedger();

    /**
     * Get a block with transactions to mine.
     * 
     * @param minerAccountId The account id of the miner.
     * 
     * @return A block with transactions to mine.
     */
    @Path("/block")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    BCBlock getBlockToMine(String minerAccountId);

}
