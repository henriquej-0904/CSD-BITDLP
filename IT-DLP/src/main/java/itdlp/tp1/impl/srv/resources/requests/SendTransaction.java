package itdlp.tp1.impl.srv.resources.requests;

import java.io.Serializable;

import itdlp.tp1.api.operations.LedgerTransaction;

public class SendTransaction implements Serializable {
    private static final long serialVersionUID = 9032490L;

    private LedgerTransaction transaction;

    /**
     * @param transaction
     */
    public SendTransaction(LedgerTransaction transaction) {
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
