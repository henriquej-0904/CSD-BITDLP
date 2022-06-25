package tp2.bitdlp.pow.transaction;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

import tp2.bitdlp.api.AccountId;
import tp2.bitdlp.util.Crypto;
import tp2.bitdlp.util.Utils;

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

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");

    protected int value;
    protected Type type;
    protected String date;

    protected String clientSignature;

    protected AccountId origin, dest;
    
    protected int nonce;

    /**
     * 
     */
    public LedgerTransaction() {
    }

    public static LedgerTransaction newTransaction(AccountId origin,
        AccountId dest, int value, int nonce, String date, String clientSignature)
    {
        Objects.requireNonNull(origin);

        LedgerTransaction t =
            newTransaction(Type.TRANSACTION, dest, value, nonce, date, clientSignature);

        t.origin = origin;

        return t;
    }

    public static LedgerTransaction newTransaction(AccountId origin,
        AccountId dest, int value, int nonce)
    {
        return newTransaction(origin, dest, value, nonce, currentDate(), null);
    }

    public static LedgerTransaction newGenerationTransaction(AccountId dest, int value, String date)
    {
        return newTransaction(Type.GENERATION_TRANSACTION, dest, value, 0, date, null);
    }

    public static LedgerTransaction newGenerationTransaction(AccountId dest, int value)
    {
        return newGenerationTransaction(dest, value, currentDate());
    }

    protected static LedgerTransaction newTransaction(Type type, AccountId dest,
        int value, int nonce, String date, String clientSignature)
    {
        Objects.requireNonNull(dest);
        Objects.requireNonNull(date);

        if(value <= 0)
            throw new InvalidTransactionException("Transaction value must be positive.");
        
        LedgerTransaction t = new LedgerTransaction();
        t.type = type;
        t.dest = dest;
        t.value = value;
        t.nonce = nonce;
        t.date = date;
        t.clientSignature = clientSignature;

        return t;
    }

    protected static String currentDate()
    {
        return Utils.printDate(DATE_FORMAT, Calendar.getInstance());
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
     * @return the date
     */
    public String getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(String date) {
        this.date = date;
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

    @Override
    public String toString()
    {
        String res = date + " " + type.toString() + ": ";

        if (type == Type.GENERATION_TRANSACTION)
            res += String.format("%s, value = %d", dest, value);
        else
            res += String.format("%s -> %s, value = %d", origin, dest, value);

        return res;
    }

    public byte[] digest() {
        return computeDigest(true).digest();
    }

    protected MessageDigest computeDigest(boolean includeClientSig)
    {
        MessageDigest digest = Crypto.getSha256Digest();

        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(getValue());
        buffer.putInt(getNonce());

        digest.update(buffer.array());
        digest.update(type.name().getBytes());
        digest.update(date.getBytes());

        if (origin != null)
            digest.update(origin.getObjectId());

        digest.update(dest.getObjectId());

        if (includeClientSig && clientSignature != null)
            digest.update(clientSignature.getBytes());

        return digest;
    }
}
