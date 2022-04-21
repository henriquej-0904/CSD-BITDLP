package itdlp.tp1.impl.srv.resources.requests;

import java.io.Serializable;
import itdlp.tp1.api.AccountId;

public class GetTotalValue implements Serializable{

    private AccountId[] accounts;
    
    private static final long serialVersionUID = 3L;

    public GetTotalValue(AccountId[] accounts){
        this.accounts = accounts;
    }

    public AccountId[] getAccounts() {
        return accounts;
    }

    public void setAccounts(AccountId[] accounts) {
        this.accounts = accounts;
    }
}
