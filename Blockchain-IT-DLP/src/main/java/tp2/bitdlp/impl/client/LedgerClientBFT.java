package tp2.bitdlp.impl.client;

import java.net.URI;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import tp2.bitdlp.api.AccountId;
import tp2.bitdlp.api.operations.LedgerTransaction;
import tp2.bitdlp.api.service.Accounts;
import tp2.bitdlp.api.service.AccountsWithBFTOps;
import tp2.bitdlp.util.Crypto;
import tp2.bitdlp.util.Pair;
import tp2.bitdlp.util.Utils;
import tp2.bitdlp.util.reply.ReplyWithSignatures;
import tp2.bitdlp.util.result.Result;

/**
 * Client with support for BFT operations with confirmation from multiple replicas.
 */
public class LedgerClientBFT extends LedgerClient
{
    protected int fReplicas;

    protected Map<String, PublicKey> replicasKeys;


    public LedgerClientBFT(String replicaId, URI endpoint, SecureRandom random, SSLContext sslContext,
        int fReplicas)
            throws KeyStoreException
    {
        super(replicaId, endpoint, random, sslContext);
        this.fReplicas = fReplicas;
        this.replicasKeys = getReplicasPublicKeys();
    }

    /**
     * @return the fReplicas
     */
    public int getfReplicas() {
        return fReplicas;
    }

    /**
     * @param fReplicas the fReplicas to set
     */
    public void setfReplicas(int fReplicas) {
        this.fReplicas = fReplicas;
    }

    public int getNumReplicasForConsensus()
    {
        return 2 * getfReplicas() + 1;
    }

    public Pair<Result<Integer>, ReplyWithSignatures> getBalanceBFT(AccountId accountId)
        throws InvalidServerSignatureException, InvalidReplyWithSignaturesException
    {
        return requestBFTOp(this.client.target(this.endpoint)
            .path(AccountsWithBFTOps.PATH).path("balance")
            .path(Utils.toHex(accountId.getObjectId()))
            .request()
            .buildGet(),
            Integer.class);
    }

    public Pair<Result<LedgerTransaction>, ReplyWithSignatures> sendTransactionBFT(AccountId originId,
        AccountId destId, int value, KeyPair originAccountKeys)
        throws InvalidServerSignatureException, InvalidReplyWithSignaturesException
    {
        return sendTransactionBFT(originId, destId, value, originAccountKeys, this.random.nextInt());
    }

    public Pair<Result<LedgerTransaction>, ReplyWithSignatures> sendTransactionBFT(AccountId originId, AccountId destId, int value, KeyPair originAccountKeys,
        int nonce) throws InvalidServerSignatureException, InvalidReplyWithSignaturesException
    {
        ByteBuffer buffer = ByteBuffer.allocate(2 * Integer.BYTES);
        buffer.putInt(value);
        buffer.putInt(nonce);

        String signature = sign(originAccountKeys.getPrivate(), originId.getObjectId(), destId.getObjectId(), buffer.array());

        return requestBFTOp(this.client.target(this.endpoint).path(AccountsWithBFTOps.PATH)
            .path("transaction").path(Integer.toString(value))
            .request()
            .header(Accounts.ACC_SIG, signature)
            .header(Accounts.NONCE, nonce)
            .buildPost(Entity.json(new Pair<>(originId.getObjectId(), destId.getObjectId()))),
            LedgerTransaction.class);
    }

    private <T> Pair<Result<T>, ReplyWithSignatures> requestBFTOp(Invocation invocation, Class<T> classType)
    {
        int numberTries = MAX_TRIES;
        RuntimeException error = null;

        while (numberTries > 0)
        {
            try (Response response = invocation.invoke();)
            {
                var reply = processReply(response, classType);
                return reply;
            }
            catch (ProcessingException e) {
                error = new RuntimeException(e.getMessage(), e);
            }

            if (error != null)
            {
                numberTries--;

                try {
                    Thread.sleep(TIMEOUT_MILLIS);
                } catch (Exception e) {}
            }
        }

        throw error;
    }

    protected <T> Pair<Result<T>, ReplyWithSignatures> processReply(Response response,
        Class<T> classType)
    {
        Status status = response.getStatusInfo().toEnum();
        ReplyWithSignatures reply = getAndVerifyReply(response);

        Result<T> result;

        if (status != Status.OK)
            result = Result.error(status.getStatusCode());
        else
        {
            if (reply == null)
                throw new InvalidReplyWithSignaturesException(
                    "Status code is OK but reply is not signed");
            
            // 200 -OK
            T replyValue;

            if (reply.getReply() == null)
                replyValue = null;
            else
            {
                try {
                    replyValue = Utils.json.readValue(reply.getReply(), classType);
                } catch (Exception e) {
                    replyValue = null;
                }
            }

            result = Result.ok(replyValue);
        }

        return new Pair<>(result, reply);
    }

    protected ReplyWithSignatures getAndVerifyReply(Response response) {
        ReplyWithSignatures reply;
        try {
            reply = response.readEntity(ReplyWithSignatures.class);

        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
        // verify server signature

        String serverSig = response.getHeaderString(Accounts.SERVER_SIG);
        if (serverSig == null || serverSig.equals(""))
            throw new InvalidServerSignatureException("Invalid server signature.");

        try {
            if (!reply.verifySignature(this.serverPublicKey, serverSig))
                throw new InvalidServerSignatureException("Invalid server signature.");
        } catch (InvalidKeyException | SignatureException e) {
            throw new InvalidServerSignatureException("Invalid server signature.");
        }

        // verify signatures from replicas
        int numValidSigs = reply.getNumValidSignatures(replicasKeys);
        if (numValidSigs < getNumReplicasForConsensus())
            throw new InvalidReplyWithSignaturesException(
                    String.format("Not enough number of valid signatures: %d < %d",
                            numValidSigs, getNumReplicasForConsensus()));

        return reply;
    }

    protected static Map<String, PublicKey> getReplicasPublicKeys()
    {
        try {
            Map<String, PublicKey> keys = new HashMap<>();

            Crypto.getTrustStore().aliases().asIterator()
            .forEachRemaining((replicaId) ->
            {
                try {
                    Certificate cert = Crypto.getTrustStore().getCertificate(replicaId);
                    if (cert != null)
                        keys.put(replicaId, cert.getPublicKey());

                } catch (Exception e) {}
            });
                
            return keys;
        } catch (KeyStoreException e) {
            throw new Error(e.getMessage(), e);
        }
    }

    
}
