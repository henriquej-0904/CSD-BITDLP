package tp2.bitdlp.api.service;

import tp2.bitdlp.impl.srv.resources.bft.ReplyWithSignatures;
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
 * An extension of Accounts API with support for asynchronous operations.
 */
@Path(AccountsWithAsyncOps.PATH)
public interface AccountsWithAsyncOps extends Accounts
{
    static final String PATH=Accounts.PATH + "/async";

    /**
	 * Returns the balance of an account.
	 *
	 * @param accountId account id
     * 
     * @return The balance of the account and a set of
     * 2f + 1 signatures of the replicas, all signed by the replica that responds to the client.
	 */
    @Path("/balance/{accountId}")
    @GET
    ReplyWithSignatures<byte[]> getBalanceAsync(@PathParam("accountId") String accountId);


    /**
	 * Transfers money from an origin to a destination.
	 *
     * @param originDestPair A pair of origin and destination accounts.
     * @param value value to be transfered
     * @param accountSignature The signature of the account.
     * @param nonce The nonce.
     * 
     * @return the transaction and a set of
     * 2f + 1 signatures of the replicas, all signed by the replica that responds to the client.
	 */
    @Path("/transaction/{value}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    ReplyWithSignatures<byte[]> sendTransactionAsync(Pair<byte[],byte[]> originDestPair, @PathParam("value") int value,
        @HeaderParam(ACC_SIG) String accountSignature, @HeaderParam(NONCE) int nonce);
}
