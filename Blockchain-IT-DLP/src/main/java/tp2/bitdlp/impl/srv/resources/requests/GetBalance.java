package tp2.bitdlp.impl.srv.resources.requests;

public class GetBalance extends Request {
    private String accountId;

    private static final long serialVersionUID = 1243244654L;

    public GetBalance(String accountId){
        super(Operation.GET_BALANCE);
        this.accountId = accountId;
    }

    public String getId() {
        return accountId;
    }

    public void setAccount(String accountId) {
        this.accountId = accountId;
    }
}
