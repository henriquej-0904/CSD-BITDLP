package itdlp.tp1.impl.srv.resources.requests;

import itdlp.tp1.api.AccountId;
import itdlp.tp1.api.operations.LedgerDeposit;

public class LoadMoney extends Request {

    private static final long serialVersionUID = 4L;

    private AccountId id;
    private LedgerDeposit value;

    public LoadMoney(AccountId id, LedgerDeposit value){
        super(Operation.LOAD_MONEY);
        this.id = id;
        this.value = value;
    }

    public AccountId getId() {
        return id;
    }

    public void setId(AccountId id) {
        this.id = id;
    }

    public LedgerDeposit getValue() {
        return value;
    }

    public void setValue(LedgerDeposit value) {
        this.value = value;
    }
}
