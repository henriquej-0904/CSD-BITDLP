package tp2.bitdlp.impl.srv.resources.requests;

public class GetTotalValue extends Request {

    private byte[][] accounts;
    
    private static final long serialVersionUID = 3L;

    public GetTotalValue(byte[][] accounts){
        super(Operation.GET_TOTAL_VALUE);
        this.accounts = accounts;
    }

    public byte[][] getAccounts() {
        return accounts;
    }

    public void setAccounts(byte[][] accounts) {
        this.accounts = accounts;
    }
}
