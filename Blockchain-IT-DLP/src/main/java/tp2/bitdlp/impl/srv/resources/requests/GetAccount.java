package tp2.bitdlp.impl.srv.resources.requests;

import tp2.bitdlp.api.AccountId;

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
