package itdlp.tp1.api.operations;

import java.nio.ByteBuffer;
import java.security.MessageDigest;

import itdlp.tp1.api.AccountId;
import itdlp.tp1.util.Crypto;

public class LedgerTransaction extends LedgerOperation {

    private static final long serialVersionUID = 232326588562L;

    private AccountId origin, dest;
    
    private int nonce;

    public LedgerTransaction(AccountId origin, AccountId dest, int value, int nonce) throws InvalidOperationException {
        super(value, Type.TRANSACTION);
        this.origin = origin;
        this.dest = dest;
        this.nonce = nonce;
    }

    public LedgerTransaction(AccountId origin, AccountId dest, int value, String date, int nonce) throws InvalidOperationException {
        super(value, Type.TRANSACTION, date);
        this.origin = origin;
        this.dest = dest;
        this.nonce = nonce;
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

    public byte[] digest() {

        MessageDigest digest = Crypto.getSha256Digest();

        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(getValue());

        digest.update(origin.getObjectId());
        digest.update(dest.getObjectId());
        digest.update(buffer.array());

        return digest.digest();
    }
}
