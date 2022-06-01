package itdlp.tp1.impl.srv.resources.requests;

import itdlp.tp1.api.AccountId;

public class GetAccount extends Request {

    private AccountId id;

    private static final long serialVersionUID = 2L;

    public GetAccount(AccountId id){
        super(Operation.GET_ACCOUNT);
        this.id = id;
    }

    public AccountId getId() {
        return id;
    }

    public void setId(AccountId id) {
        this.id = id;
    }   
}
