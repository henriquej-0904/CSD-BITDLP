package itdlp.tp1.data.mongo.operations;

import java.nio.ByteBuffer;
import java.security.MessageDigest;

import org.bson.codecs.pojo.annotations.BsonDiscriminator;

import itdlp.tp1.api.AccountId;
import itdlp.tp1.api.operations.InvalidOperationException;
import itdlp.tp1.api.operations.LedgerOperation;
import itdlp.tp1.api.operations.LedgerTransaction;
import itdlp.tp1.util.Crypto;

@BsonDiscriminator(value = "LedgerTransactionDAO", key = "_cls")
public class LedgerTransactionDAO extends LedgerOperationDAO {

    private AccountId origin, dest;
    
    private int nonce;

    public LedgerTransactionDAO(AccountId origin, AccountId dest, int value, int nonce, byte[] clientSignature) throws InvalidOperationException {
        super(value, Type.TRANSACTION);
        this.origin = origin;
        this.dest = dest;
        this.nonce = nonce;
        this.clientSignature = clientSignature;
    }

    public LedgerTransactionDAO(AccountId origin, AccountId dest, int value, String date, int nonce, byte[] clientSignature) throws InvalidOperationException {
        super(value, Type.TRANSACTION, date);
        this.origin = origin;
        this.dest = dest;
        this.nonce = nonce;
        this.clientSignature = clientSignature;
    }

    public LedgerTransactionDAO(LedgerTransaction transaction) throws InvalidOperationException
    {
        this(transaction.getOrigin(), transaction.getDest(), transaction.getValue(), transaction.getDate(), transaction.getNonce(), transaction.getClientSignature());
    }

    /**
     * 
     */
    public LedgerTransactionDAO() {
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

    public LedgerOperation toLedgerTransaction() {
        try {
            LedgerTransaction transaction = new LedgerTransaction(origin, dest, getValue(), getDate(), nonce, getClientSignature());
            return transaction;
        } catch (Exception e) {
            throw new Error(e.getMessage(), e);
        }
    }
}
