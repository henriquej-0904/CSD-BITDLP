package tp2.bitdlp.impl.srv.resources.requests;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
    @JsonSubTypes.Type(value = CreateAccount.class, name = "CreateAccount"),

    @JsonSubTypes.Type(value = GetAccount.class, name = "GetAccount"),
    @JsonSubTypes.Type(value = GetBalance.class, name = "GetBalance"),
    @JsonSubTypes.Type(value = GetFullLedger.class, name = "GetFullLedger"),
    @JsonSubTypes.Type(value = GetGlobalValue.class, name = "GetGlobalValue"),
    @JsonSubTypes.Type(value = GetTotalValue.class, name = "GetTotalValue"),
    @JsonSubTypes.Type(value = LoadMoney.class, name = "LoadMoney"),
    @JsonSubTypes.Type(value = SendTransaction.class, name = "SendTransaction"),
    @JsonSubTypes.Type(value = ProposeMinedBlock.class, name = "ProposeMinedBlock"),
    @JsonSubTypes.Type(value = SmartContractValidation.class, name = "SmartContractValidation")
 }
)
public abstract class Request
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
        SEND_TRANSACTION_ASYNC,
        PROPOSE_BLOCK_ASYNC,
        SMART_CONTRACT_VALIDATION_ASYNC;
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
