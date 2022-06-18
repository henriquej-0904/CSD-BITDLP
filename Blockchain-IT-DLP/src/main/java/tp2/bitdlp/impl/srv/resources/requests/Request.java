package tp2.bitdlp.impl.srv.resources.requests;

import java.io.Serializable;

public abstract class Request implements Serializable
{
    public static enum Operation
    {
        CREATE_ACCOUNT,
        LOAD_MONEY,
        SEND_TRANSACTION,

        GET_ACCOUNT,
        GET_BALANCE,
        GET_TOTAL_VALUE,
        GET_GLOBAL_LEDGER_VALUE,
        GET_LEDGER,

        // ASYNC
        GET_BALANCE_ASYNC,
        SEND_TRANSACTION_ASYNC;
    }

    private Operation operation;

    /**
     * @param operation
     */
    protected Request(Operation operation) {
        this.operation = operation;
    }

    /**
     * @return the operation
     */
    public Operation getOperation() {
        return operation;
    }

    /**
     * @param operation the operation to set
     */
    public void setOperation(Operation operation) {
        this.operation = operation;
    }
}
