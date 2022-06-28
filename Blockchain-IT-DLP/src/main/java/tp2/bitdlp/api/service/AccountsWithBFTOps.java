package tp2.bitdlp.api.service;

import tp2.bitdlp.util.reply.ReplyWithSignatures;
import tp2.bitdlp.pow.block.BCBlock;
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
 * An extension of Accounts API with support for bft operations with confirmation from multiple replicas.
 */
public interface AccountsWithBFTOps extends Accounts
{
    static final String BFT_PATH = "/bft";
    static final String PATH=Accounts.PATH + BFT_PATH;

    /**
	 * Returns the balance of an account.
	 *
	 * @param accountId account id
     * 
     * @return The balance of the account and a set of
     * 2f + 1 signatures of the replicas, all signed by the replica that responds to the client.
	 */
    @Path(BFT_PATH + "/balance/{accountId}")
    @GET
    ReplyWithSignatures getBalanceBFT(@PathParam("accountId") String accountId);


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
    @Path(BFT_PATH + "/transaction/{value}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    ReplyWithSignatures sendTransactionBFT(Pair<byte[],byte[]> originDestPair, @PathParam("value") int value,
        @HeaderParam(ACC_SIG) String accountSignature, @HeaderParam(NONCE) int nonce);

    @Path(BFT_PATH + "/block")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    ReplyWithSignatures proposeMinedBlock(Pair<String, BCBlock> pairMinerIdBlock, @HeaderParam(ACC_SIG) String signature);
}
