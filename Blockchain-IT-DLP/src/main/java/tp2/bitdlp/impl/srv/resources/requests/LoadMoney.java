package tp2.bitdlp.impl.srv.resources.requests;

import tp2.bitdlp.api.operations.LedgerDeposit;

public class LoadMoney extends Request {

    private static final long serialVersionUID = 4L;
    private LedgerDeposit value;

    public LoadMoney(LedgerDeposit value){
        super(Operation.LOAD_MONEY);
        this.value = value;
    }

    public LedgerDeposit getValue() {
        return value;
    }

    public void setValue(LedgerDeposit value) {
        this.value = value;
    }
}
