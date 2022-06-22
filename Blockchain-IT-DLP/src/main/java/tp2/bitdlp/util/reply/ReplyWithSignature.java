package tp2.bitdlp.util.reply;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;

import tp2.bitdlp.util.Crypto;

public class ReplyWithSignature
{
    private int statusCode, replicaId;

    private byte[] reply;

    private String signature;

    /**
     * @param reply
     * @param signature
     */
    public ReplyWithSignature(int replicaId, int statusCode, byte[] reply, String signature) {
        this.replicaId = replicaId;
        this.statusCode = statusCode;
        this.reply = reply;
        this.signature = signature;
    }

    /**
     * 
     */
    public ReplyWithSignature() {
    }

    /**
     * @return the reply
     */
    public byte[] getReply() {
        return reply;
    }

    /**
     * @param reply the reply to set
     */
    public void setReply(byte[] reply) {
        this.reply = reply;
    }

    /**
     * @return the signature
     */
    public String getSignature() {
        return signature;
    }

    /**
     * @param signature the signature to set
     */
    public void setSignature(String signature) {
        this.signature = signature;
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
     * @return the replicaId
     */
    public int getReplicaId() {
        return replicaId;
    }

    /**
     * @param replicaId the replicaId to set
     */
    public void setReplicaId(int replicaId) {
        this.replicaId = replicaId;
    }

    
    public void sign(PrivateKey key) throws InvalidKeyException, SignatureException
    {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES * 2);
        buffer.putInt(getReplicaId());
        buffer.putInt(getStatusCode());
        setSignature(Crypto.sign(key, buffer.array(), getReply()));
    }

    public boolean verifySignature(PublicKey key) throws InvalidKeyException, SignatureException
    {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES * 2);
        buffer.putInt(getReplicaId());
        buffer.putInt(getStatusCode());
        return Crypto.verifySignature(key, getSignature(), buffer.array(), getReply());
    }
}
