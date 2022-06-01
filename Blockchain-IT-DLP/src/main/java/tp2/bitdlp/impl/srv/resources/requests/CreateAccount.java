package tp2.bitdlp.impl.srv.resources.requests;

import tp2.bitdlp.api.Account;

public class CreateAccount extends Request {

    private Account account;

    private static final long serialVersionUID = 1L;

    public CreateAccount(Account account){
        super(Operation.CREATE_ACCOUNT);
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}


