package tp2.bitdlp.pow.transaction;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Objects;

import tp2.bitdlp.api.AccountId;
import tp2.bitdlp.util.Crypto;

public class LedgerTransaction
{
    public static enum Type
    {
        /**
         * A transaction between two parties.
         */
        TRANSACTION,

        /**
         * A transaction that represents a reward for the winner of the PoW challenge.
         */
        GENERATION_TRANSACTION;
    }

    protected int value;
    protected Type type;

    protected String clientSignature;

    protected AccountId origin, dest;
    
    protected int nonce;

    protected SmartContract smartContract;

    protected byte[] hash;

    /**
     * 
     */
    public LedgerTransaction() {
    }

    public static LedgerTransaction newTransaction(AccountId origin,
        AccountId dest, int value, int nonce, String clientSignature)
    {
        Objects.requireNonNull(origin);

        LedgerTransaction t =
            newTransaction(Type.TRANSACTION, dest, value, nonce, clientSignature);

        t.origin = origin;

        return t;
    }

    public static LedgerTransaction newTransaction(AccountId origin,
        AccountId dest, int value, int nonce)
    {
        return newTransaction(origin, dest, value, nonce, null);
    }

    public static LedgerTransaction newGenerationTransaction(AccountId dest, int value)
    {
        var transaction = newTransaction(Type.GENERATION_TRANSACTION, dest, value, 0, null);
        transaction.setHash(transaction.digest());
        return transaction;
    }

    protected static LedgerTransaction newTransaction(Type type, AccountId dest,
        int value, int nonce, String clientSignature)
    {
        Objects.requireNonNull(dest);

        if(value <= 0)
            throw new InvalidTransactionException("Transaction value must be positive.");
        
        LedgerTransaction t = new LedgerTransaction();
        t.type = type;
        t.dest = dest;
        t.value = value;
        t.nonce = nonce;
        t.clientSignature = clientSignature;

        return t;
    }

    protected static long currentTime()
    {
        return System.currentTimeMillis();
    }

    /**
     * @return the value
     */
    public int getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(int value) {
        this.value = value;
    }


    /**
     * @return the type
     */
    public Type getType() {
        return type;
    }


    /**
     * @param type the type to set
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * @return the clientSignature
     */
    public String getClientSignature() {
        return clientSignature;
    }

    /**
     * @param clientSignature the clientSignature to set
     */
    public void setClientSignature(String clientSignature) {
        this.clientSignature = clientSignature;
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

    /**
     * @return the hash
     */
    public byte[] getHash() {
        return hash;
    }

    /**
     * @param hash the hash to set
     */
    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    @Override
    public String toString()
    {
        String res = type.toString() + ": ";

        if (type == Type.GENERATION_TRANSACTION)
            res += String.format("%s, value = %d", dest, value);
        else
            res += String.format("%s -> %s, value = %d", origin, dest, value);

        return res;
    }

    public byte[] digest() {
        return computeDigest(true).digest();
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(hash);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof LedgerTransaction))
            return false;
        LedgerTransaction other = (LedgerTransaction) obj;
        return Arrays.equals(hash, other.hash);
    }

    protected MessageDigest computeDigest(boolean includeClientSig)
    {
        MessageDigest digest = Crypto.getSha256Digest();

        ByteBuffer buffer = ByteBuffer.allocate(2*Integer.BYTES);
        buffer.putInt(getValue());
        buffer.putInt(getNonce());

        digest.update(buffer.array());
        digest.update(type.name().getBytes());

        if (origin != null)
            digest.update(origin.getObjectId());

        digest.update(dest.getObjectId());

        if (smartContract != null)
            smartContract.digest(digest);

        if (includeClientSig && clientSignature != null)
            digest.update(clientSignature.getBytes());

        return digest;
    }

    /**
     * @return the smartContract
     */
    public SmartContract getSmartContract() {
        return smartContract;
    }

    /**
     * @param smartContract the smartContract to set
     */
    public void setSmartContract(SmartContract smartContract) {
        this.smartContract = smartContract;
    }
}
