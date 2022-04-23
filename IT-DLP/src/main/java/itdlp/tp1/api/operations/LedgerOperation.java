package itdlp.tp1.api.operations;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import itdlp.tp1.util.Utils;

//@JsonDeserialize(using = LedgerOperationJsonDeserializer.class)
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
}
