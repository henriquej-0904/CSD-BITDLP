package tp2.bitdlp.impl.srv.resources.requests;

public class GetTotalValue extends Request {

    private byte[][] accounts;

    public GetTotalValue(){
        super(Operation.GET_TOTAL_VALUE);
    }

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
