package itdlp.tp1.impl.srv.resources.requests;

import itdlp.tp1.api.AccountId;

public class GetBalance extends Request {
    private AccountId accountId;

    private static final long serialVersionUID = 1243244654L;

    public GetBalance(AccountId accountId){
        super(Operation.GET_BALANCE);
        this.accountId = accountId;
    }

    public AccountId getAccount() {
        return accountId;
    }

    public void setAccount(AccountId accountId) {
        this.accountId = accountId;
    }
}
