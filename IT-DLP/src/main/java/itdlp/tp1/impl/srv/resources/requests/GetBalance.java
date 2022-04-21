package itdlp.tp1.impl.srv.resources.requests;

import java.io.Serializable;

import itdlp.tp1.api.AccountId;

public class GetBalance implements Serializable {
    private AccountId accountId;

    private static final long serialVersionUID = 1243244654L;

    public GetBalance(AccountId accountId){
        this.accountId = accountId;
    }

    public AccountId getAccount() {
        return accountId;
    }

    public void setAccount(AccountId accountId) {
        this.accountId = accountId;
    }
}
