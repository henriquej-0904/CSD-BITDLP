package tp2.bitdlp.util.reply;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonIgnore;

import tp2.bitdlp.util.Crypto;
import tp2.bitdlp.util.Utils;

public class ReplyWithSignatures
{
    private int statusCode;

    private byte[] reply;

    private SortedMap<String, String> signatures;

    /**
     * 
     */
    public ReplyWithSignatures() {
    }

    /**
     * @param maxReplies
     */
    public ReplyWithSignatures(int statusCode, byte[] reply)
    {
        this.statusCode = statusCode;
        this.reply = reply;
    }

    public void addSignature(String replicaId, String signature)
    {
        if (this.signatures == null)
            this.signatures = new TreeMap<>();
        
        this.signatures.put(replicaId, signature);
    }

    /**
     * @return the numReplies
     */
    @JsonIgnore
    public int getNumReplies() {
        return signatures.size();
    }

    /**
     * @return the reply
     */
    public byte[] getReply() {
        return reply;
    }

    /**
     * @return the signatures
     */
    public SortedMap<String, String> getSignatures() {
        return signatures;
    }

    /**
     * @return the statusCode
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * @param statusCode the statusCode to set
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * @param reply the reply to set
     */
    public void setReply(byte[] reply) {
        this.reply = reply;
    }

    /**
     * @param signatures the signatures to set
     */
    public void setSignatures(SortedMap<String, String> signatures) {
        this.signatures = signatures;
    }

    /**
     * Sign this object.
     * 
     * @param key The private key
     * @return The signature in Hex.
     * @throws InvalidKeyException
     * @throws SignatureException
     */
    public String sign(PrivateKey key) throws InvalidKeyException, SignatureException
    {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(getStatusCode());

        Signature signature = Crypto.createSignatureInstance();
        signature.initSign(key);
        
        signature.update(buffer.array());

        if (getReply() != null)
            signature.update(getReply());

        for (Entry<String, String> replicaSig : getSignatures().entrySet())
        {
            signature.update(replicaSig.getKey().getBytes());
            signature.update(replicaSig.getValue().getBytes());
        }

        return Utils.toHex(signature.sign());
    }

    /**
     * Verify signature.
     * 
     * @param key The public key
     * @param signature The signature to verify
     * @return True if the signature is valid, false otherwise.
     * @throws InvalidKeyException
     * @throws SignatureException
     */
    public boolean verifySignature(PublicKey key, String signature) throws InvalidKeyException, SignatureException
    {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(getStatusCode());

        Signature sig = Crypto.createSignatureInstance();
        sig.initVerify(key);
        
        sig.update(buffer.array());

        if (getReply() != null)
            sig.update(getReply());

        for (Entry<String, String> replicaSig : getSignatures().entrySet())
        {
            sig.update(replicaSig.getKey().getBytes());
            sig.update(replicaSig.getValue().getBytes());
        }

        return sig.verify(Utils.fromHex(signature));
    }

    /**
     * Get the number of valid signatures in this reply.
     * 
     * @param key
     * @return
     */
    public int getNumValidSignatures(Map<String, PublicKey> publicKeys)
    {
        int validSigs = 0;

        String replicaId;
        PublicKey replicaKey;
        ReplyWithSignature replyWithSignature;

        for (Entry<String, String> replicaSignature : getSignatures().entrySet())
        {
            replicaId = replicaSignature.getKey();
            replicaKey = publicKeys.get(replicaId);

            if (replicaKey == null)
                continue;

            replyWithSignature = new ReplyWithSignature(replicaId, statusCode,
                reply, replicaSignature.getValue());

            try {
                if (replyWithSignature.verifySignature(replicaKey))
                    validSigs++;
            } catch (Exception e) {}
        }

        return validSigs;
    }
}
