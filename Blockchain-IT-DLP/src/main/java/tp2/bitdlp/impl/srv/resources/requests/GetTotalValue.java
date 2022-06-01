package tp2.bitdlp.impl.srv.resources.requests;

import tp2.bitdlp.api.AccountId;

public class GetTotalValue extends Request {

    private AccountId[] accounts;
    
    private static final long serialVersionUID = 3L;

    public GetTotalValue(AccountId[] accounts){
        super(Operation.GET_TOTAL_VALUE);
        this.accounts = accounts;
    }

    public AccountId[] getAccounts() {
        return accounts;
    }

    public void setAccounts(AccountId[] accounts) {
        this.accounts = accounts;
    }
}
