package tp2.bitdlp.impl.srv.resources.requests;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
    @JsonSubTypes.Type(value = CreateAccount.class, name = "CreateAccount"),
    @JsonSubTypes.Type(value = SendTransaction.class, name = "SendTransaction"),
    @JsonSubTypes.Type(value = ProposeMinedBlock.class, name = "ProposeMinedBlock")
 }
)
public abstract class Request
{
    public static enum Operation
    {
        CREATE_ACCOUNT,
        SEND_TRANSACTION,
        PROPOSE_BLOCK
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
