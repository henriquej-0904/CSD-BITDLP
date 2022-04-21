package itdlp.tp1.impl.srv.resources.requests;

import itdlp.tp1.api.AccountId;

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
