package itdlp.tp1.impl.srv.resources.requests;

import itdlp.tp1.api.operations.LedgerTransaction;

public class SendTransaction extends Request {
    private static final long serialVersionUID = 9032490L;

    private LedgerTransaction transaction;

    /**
     * @param transaction
     */
    public SendTransaction(LedgerTransaction transaction) {
        super(Operation.SEND_TRANSACTION);
        this.transaction = transaction;
    }

    /**
     * @return the transaction
     */
    public LedgerTransaction getTransaction() {
        return transaction;
    }

    /**
     * @param transaction the transaction to set
     */
    public void setTransaction(LedgerTransaction transaction) {
        this.transaction = transaction;
    }
}
