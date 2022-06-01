package itdlp.tp1.api.operations;

import java.nio.ByteBuffer;
import java.security.MessageDigest;

import itdlp.tp1.api.AccountId;

public class LedgerTransaction extends LedgerOperation {

    private static final long serialVersionUID = 232326588562L;

    private AccountId origin, dest;
    
    private int nonce;

    public LedgerTransaction(AccountId origin, AccountId dest, int value, int nonce, byte[] clientSignature) throws InvalidOperationException {
        super(value, Type.TRANSACTION);
        this.origin = origin;
        this.dest = dest;
        this.nonce = nonce;
        this.clientSignature = clientSignature;
    }

    public LedgerTransaction(AccountId origin, AccountId dest, int value, String date, int nonce, byte[] clientSignature) throws InvalidOperationException {
        super(value, Type.TRANSACTION, date);
        this.origin = origin;
        this.dest = dest;
        this.nonce = nonce;
        this.clientSignature = clientSignature;
    }

    /**
     * 
     */
    public LedgerTransaction() {
    }

    /**
     * @return the origin
     */
    public AccountId getOrigin() {
        return origin;
    }

    /**
     * @param origin the origin to set
     */
    public void setOrigin(AccountId origin) {
        this.origin = origin;
    }

    /**
     * @return the dest
     */
    public AccountId getDest() {
        return dest;
    }

    /**
     * @param dest the dest to set
     */
    public void setDest(AccountId dest) {
        this.dest = dest;
    }

    
    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    @Override
    public String toString() {
        return super.toString() + String.format("%s -> %s, value = %d", origin, dest, getValue());
    }

    @Override
    public byte[] digest() {
        return computeDigest().digest();
    }

    @Override
    protected MessageDigest computeDigest() {
        MessageDigest digest = super.computeDigest();

        digest.update(origin.getObjectId());
        digest.update(dest.getObjectId());

        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(getNonce());

        return digest;
    }
}
