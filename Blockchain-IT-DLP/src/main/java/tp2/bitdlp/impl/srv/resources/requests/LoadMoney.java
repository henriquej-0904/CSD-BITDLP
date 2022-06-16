package tp2.bitdlp.impl.srv.resources.requests;

public class LoadMoney extends Request {

    private static final long serialVersionUID = 4L;

    private byte[] accountId;
    private int value;
    private String accountSignature;

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

    
}
