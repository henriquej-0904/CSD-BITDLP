package tp2.bitdlp.impl.srv.resources.requests;

public class LoadMoney extends Request {

    private byte[] accountId;
    private int value;
    private String accountSignature;

    public LoadMoney(){
        super(Operation.LOAD_MONEY);
    }

    public LoadMoney(byte[] accountId, int value, String accountSignature){
        super(Operation.LOAD_MONEY);
        this.accountId = accountId;
        this.value = value;
        this.accountSignature = accountSignature;
    }

    /**
     * @return the accountId
     */
    public byte[] getAccountId() {
        return accountId;
    }

    /**
     * @return the value
     */
    public int getValue() {
        return value;
    }

    /**
     * @return the accountSignature
     */
    public String getAccountSignature() {
        return accountSignature;
    }

    /**
     * @param accountId the accountId to set
     */
    public void setAccountId(byte[] accountId) {
        this.accountId = accountId;
    }

    /**
     * @param value the value to set
     */
    public void setValue(int value) {
        this.value = value;
    }

    /**
     * @param accountSignature the accountSignature to set
     */
    public void setAccountSignature(String accountSignature) {
        this.accountSignature = accountSignature;
    }
    
}
