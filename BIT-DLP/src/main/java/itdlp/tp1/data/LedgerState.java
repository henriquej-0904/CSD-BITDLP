package itdlp.tp1.data;

import java.util.List;

import itdlp.tp1.api.AccountId;
import itdlp.tp1.api.UserId;
import itdlp.tp1.api.operations.LedgerOperation;
import itdlp.tp1.util.Pair;
import itdlp.tp1.util.Utils;

public class LedgerState {

    private List<Pair<AccountId, UserId>> accounts;
    private List<LedgerOperation> operations;

    private byte[] serializedState;

    
    /**
     * @param accounts
     * @param operations
     */
    public LedgerState(List<Pair<AccountId, UserId>> accounts, List<LedgerOperation> operations) {
        this.accounts = accounts;
        this.operations = operations;
        toSerializedState();
    }

    /**
     * @param serializedState
     * @throws ClassNotFoundException
     */
    public LedgerState(byte[] serializedState) throws ClassNotFoundException {
        this.serializedState = serializedState;
        fromSerializedState();
    }


    /**
     * @return the accounts
     */
    public List<Pair<AccountId, UserId>> getAccounts() {
        return accounts;
    }
    

    /**
     * @return the operations
     */
    public List<LedgerOperation> getOperations() {
        return operations;
    }


    /**
     * @return the serializedState
     */
    public byte[] getSerializedState() {
        return serializedState;
    }


    @SuppressWarnings("unchecked")
    private void fromSerializedState() throws ClassNotFoundException
    {
        Object[] state = (Object[]) Utils.readObject(this.serializedState);
        this.accounts = (List<Pair<AccountId, UserId>>) state[0];
        this.operations = (List<LedgerOperation>) state[1];
    }

    private void toSerializedState()
    {
        Object[] state = new Object[2];
        state[0] = this.accounts;
        state[1] = this.operations;

        this.serializedState = Utils.writeObject(state);
    }

}
