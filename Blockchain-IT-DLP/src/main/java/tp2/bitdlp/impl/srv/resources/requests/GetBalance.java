package tp2.bitdlp.impl.srv.resources.requests;

public class GetBalance extends Request {
    private String id;

    public GetBalance(){
        super(Operation.GET_BALANCE);
    }

    public GetBalance(String accountId){
        super(Operation.GET_BALANCE);
        this.id = accountId;
    }

    public String getId() {
        return id;
    }

    public void setId(String accountId) {
        this.id = accountId;
    }

    public void async()
    {
        this.setOperation(Operation.GET_BALANCE_ASYNC);
    }
}
