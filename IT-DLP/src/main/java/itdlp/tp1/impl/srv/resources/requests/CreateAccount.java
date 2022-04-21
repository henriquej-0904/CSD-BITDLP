package itdlp.tp1.impl.srv.resources.requests;

import java.io.Serializable;

import itdlp.tp1.api.Account;

public class CreateAccount implements Serializable {

    private  Account account;

    private static final long serialVersionUID = 1L;

    public CreateAccount(Account account){
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    
}


