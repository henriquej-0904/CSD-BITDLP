package tp2.bitdlp.impl.srv.resources.bft;

import java.nio.ByteBuffer;
import java.security.Signature;

import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import tp2.bitdlp.api.AccountId;
import tp2.bitdlp.api.service.Accounts;
import tp2.bitdlp.api.service.AccountsWithAsyncOps;
import tp2.bitdlp.impl.srv.config.ServerConfig;
import tp2.bitdlp.impl.srv.resources.AccountsResource;
import tp2.bitdlp.impl.srv.resources.requests.GetBalance;
import tp2.bitdlp.util.Crypto;
import tp2.bitdlp.util.Pair;
import tp2.bitdlp.util.Utils;

/**
 * An abstract class that defines methods and operations for implementing a BFT service.
 * This service also supports asynchronous operations.
 */
public abstract class AccountsResourceBFT extends AccountsResource implements AccountsWithAsyncOps
{
    
    @Override
    public ReplyWithSignatures<byte[]> getBalanceAsync(String accountId) {
        GetBalance clientParams;
        AccountId id;

        try {
            init();

            clientParams = new GetBalance(accountId);
            clientParams.async();
            id = verifyGetBalance(clientParams);
        } catch (WebApplicationException e) {
            LOG.info(e.getMessage());
            throw e;
        }

        ReplyWithSignatures<byte[]> reply = getBalanceAsync(clientParams, id);

        // LOG.info(String.format("Balance - %d, %s\n", result, accountId));

        String signature = signReplyWithSignatures(reply);

        throw new WebApplicationException(
                Response.status(Status.OK)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(new String(toJson(reply)))
                        .header(Accounts.SERVER_SIG, signature)
                        .build());
    }

    /**
	 * Returns the balance of an account.
	 *
     * @param clientParams
	 * @param accountId account id
     * 
     * @return The balance of the account.
	 */
    public abstract ReplyWithSignatures<byte[]> getBalanceAsync(GetBalance clientParams, AccountId accountId);



    @Override
    public ReplyWithSignatures<byte[]> sendTransactionAsync(Pair<byte[], byte[]> originDestPair, int value,
            String accountSignature, int nonce) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Sign a reply with signatures.
     * @param reply
     * @return The generated signature.
     */
    protected String signReplyWithSignatures(ReplyWithSignatures<byte[]> reply)
    {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(reply.getStatusCode());

        try {
            Signature signature = Crypto.createSignatureInstance();
            signature.initSign(ServerConfig.getKeyPair().getPrivate());
        
            signature.update(buffer.array());
            signature.update(reply.getReply());

            for (String sig : reply.getSignatures()) {
                signature.update(sig.getBytes());
            }

            return Utils.toHex(signature.sign());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage(), e);
        }
    }
}
