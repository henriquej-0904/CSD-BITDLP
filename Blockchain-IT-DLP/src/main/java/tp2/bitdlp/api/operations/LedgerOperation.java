package tp2.bitdlp.api.operations;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import tp2.bitdlp.util.Crypto;
import tp2.bitdlp.util.Utils;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
    @JsonSubTypes.Type(value = LedgerDeposit.class, name = "LedgerDeposit"),

    @JsonSubTypes.Type(value = LedgerTransaction.class, name = "LedgerTransaction") }
)
public abstract class LedgerOperation implements Serializable {
    
    public static enum Type
    {
        DEPOSIT,
        TRANSACTION;
    }

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");

    private int value;
    private Type type;
    private String date;

    protected byte[] clientSignature;

    /**
     * @param value
     * @param type
     * @param date
     */
    protected LedgerOperation(int value, Type type, String date) throws InvalidOperationException {
        if(value <= 0)
            throw new InvalidOperationException("Transaction value must be positive.");

        this.value = value;
        this.type = type;
        this.date = date;
    }

    /**
     * @param value
     * @param type
     * @throws InvalidOperationException
     */
    protected LedgerOperation(int value, Type type) throws InvalidOperationException {
        this(value, type, Utils.printDate(DATE_FORMAT, Calendar.getInstance()));
    }

    /**
     * 
     */
    protected LedgerOperation() {
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

    @Override
    public String toString() {
        return date + " " + type.toString() + ": ";
    }

    /**
     * @return the clientSignature
     */
    public byte[] getClientSignature() {
        return clientSignature;
    }

    /**
     * @param clientSignature the clientSignature to set
     */
    public void setClientSignature(byte[] clientSignature) {
        this.clientSignature = clientSignature;
    }

    public abstract byte[] digest();

    protected MessageDigest computeDigest()
    {
        MessageDigest digest =  Crypto.getSha256Digest();

        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(getValue());

        digest.update(buffer.array());
        digest.update(type.name().getBytes());
        digest.update(date.getBytes());
        
        return digest;
    }
}
