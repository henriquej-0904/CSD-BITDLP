package itdlp.api.operations;

import java.text.SimpleDateFormat;

public abstract class LedgerOperation {
    
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
     * @param accountId
     * @param type
     */
    protected LedgerOperation(int value, Type type, String date) {
        this.value = value;
        this.type = type;
        this.date = date;
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
