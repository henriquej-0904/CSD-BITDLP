package tp2.bitdlp.impl.srv.resources.bft;

import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import tp2.bitdlp.api.service.Accounts;
import tp2.bitdlp.api.service.AccountsWithAsyncOps;
import tp2.bitdlp.impl.srv.config.ServerConfig;
import tp2.bitdlp.impl.srv.resources.AccountsResource;
import tp2.bitdlp.impl.srv.resources.requests.GetBalance;
import tp2.bitdlp.impl.srv.resources.requests.SendTransaction;
import tp2.bitdlp.util.Pair;
import tp2.bitdlp.util.reply.ReplyWithSignatures;

/**
 * An abstract class that defines methods and operations for implementing a BFT service.
 * This service also supports asynchronous operations.
 */
public abstract class AccountsResourceBFT extends AccountsResource implements AccountsWithAsyncOps
{
    @Override
    public ReplyWithSignatures getBalanceAsync(String accountId) {
        GetBalance clientParams;

        try {
            init();

            clientParams = new GetBalance(accountId);
            clientParams.async();
            verifyGetBalance(clientParams);
        } catch (WebApplicationException e) {
            LOG.info(e.getMessage());
            throw e;
        }

        ReplyWithSignatures reply = getBalanceAsync(clientParams);

        // LOG.info(String.format("Balance - %d, %s\n", result, accountId));

        String signature = signReplyWithSignatures(reply);

        throw new WebApplicationException(
                Response.status(Status.fromStatusCode(reply.getStatusCode()))
                        .type(MediaType.APPLICATION_JSON)
                        .entity(new String(toJson(reply)))
                        .header(Accounts.SERVER_SIG, signature)
                        .build());
    }

    /**
	 * Returns the balance of an account.
	 *
     * @param clientParams
     * 
     * @return The balance of the account and a set of 2f + 1 signatures of the replicas,
     * all signed by the replica that responds to the client.
	 */
    public abstract ReplyWithSignatures getBalanceAsync(GetBalance clientParams);


    @Override
    public ReplyWithSignatures sendTransactionAsync(Pair<byte[], byte[]> originDestPair,
        int value, String accountSignature, int nonce)
    {
        SendTransaction clientParams;

        try {
            init();

            clientParams = new SendTransaction(originDestPair, value, accountSignature, nonce);
            clientParams.async();
            verifySendTransaction(clientParams);
        } catch (WebApplicationException e) {
            LOG.info(e.getMessage());
            throw e;
        }

        ReplyWithSignatures reply = sendTransactionAsync(clientParams);
        String signature = signReplyWithSignatures(reply);

        // log operation if successful
        // LOG.info(String.format("ORIGIN: %s, DEST: %s, TYPE: %s, VALUE: %d",
        // originId, destId, transaction.getType(), value));

        throw new WebApplicationException(
                Response.status(Status.fromStatusCode(reply.getStatusCode()))
                        .entity(reply)
                        .header(Accounts.SERVER_SIG, signature)
                        .build());
    }

    /**
	 * Transfers money from an origin to a destination.
	 *
     * @param clientParams
     * 
     * @return the transaction and a set of 2f + 1 signatures of the replicas,
     * all signed by the replica that responds to the client.
	 */
    public abstract ReplyWithSignatures sendTransactionAsync(SendTransaction clientParams);

    /**
     * Sign a reply with signatures.
     * @param reply
     * @return The generated signature.
     */
    protected String signReplyWithSignatures(ReplyWithSignatures reply)
    {
        try {
            return reply.sign(ServerConfig.getKeyPair().getPrivate());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage(), e);
        }
    }
}
