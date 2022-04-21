package itdlp.tp1.impl.srv.resources.requests;

import java.io.Serializable;

import itdlp.tp1.api.AccountId;

public class GetAccount implements Serializable {

    private  AccountId id;

    private static final long serialVersionUID = 1L;

    public GetAccount(AccountId id){
        this.id = id;
    }

    public AccountId getId() {
        return id;
    }

    public void setId(AccountId id) {
        this.id = id;
    }

    
}
