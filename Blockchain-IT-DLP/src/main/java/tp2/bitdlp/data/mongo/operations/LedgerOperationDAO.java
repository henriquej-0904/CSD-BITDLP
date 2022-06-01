package tp2.bitdlp.data.mongo.operations;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.types.ObjectId;

import tp2.bitdlp.api.operations.InvalidOperationException;
import tp2.bitdlp.api.operations.LedgerOperation;
import tp2.bitdlp.util.Utils;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
    @JsonSubTypes.Type(value = LedgerDepositDAO.class, name = "LedgerDepositDAO"),

    @JsonSubTypes.Type(value = LedgerTransactionDAO.class, name = "LedgerTransactionDAO") }
)

@BsonDiscriminator(key = "_cls")
public abstract class LedgerOperationDAO {
    
    public static enum Type
    {
        DEPOSIT,
        TRANSACTION;
    }

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");

    private ObjectId id;

    private int value;
    private Type type;
    private String date;

    protected byte[] clientSignature;

    /**
     * @param value
     * @param type
     * @param date
     */
    protected LedgerOperationDAO(int value, Type type, String date) throws InvalidOperationException {
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
    protected LedgerOperationDAO(int value, Type type) throws InvalidOperationException {
        this(value, type, Utils.printDate(DATE_FORMAT, Calendar.getInstance()));
    }

    /**
     * 
     */
    protected LedgerOperationDAO() {
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
     * @return the id
     */
    public ObjectId getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(ObjectId id) {
        this.id = id;
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

    @Override
    public String toString() {
        return date + " " + type.toString() + ": ";
    }

    public LedgerOperation toLedgerOperation() {
        
        switch (type) {
            case DEPOSIT:
                return ((LedgerDepositDAO) this).toLedgerDeposit();
            case TRANSACTION:
                return ((LedgerTransactionDAO) this).toLedgerTransaction();
        }
        return null;
    }
}
