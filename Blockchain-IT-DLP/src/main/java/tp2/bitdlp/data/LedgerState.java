package tp2.bitdlp.data;

import java.util.List;

import tp2.bitdlp.api.AccountId;
import tp2.bitdlp.api.UserId;
import tp2.bitdlp.pow.transaction.LedgerTransaction;
import tp2.bitdlp.util.Pair;
import tp2.bitdlp.util.Utils;

public class LedgerState {

    private List<Pair<AccountId, UserId>> accounts;
    private List<LedgerTransaction> transactions;

    private byte[] serializedState;

    
    /**
     * @param accounts
     * @param transactions
     */
    public LedgerState(List<Pair<AccountId, UserId>> accounts, List<LedgerTransaction> transactions) {
        this.accounts = accounts;
        this.transactions = transactions;
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
    public List<LedgerTransaction> getTransactions() {
        return transactions;
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
        this.transactions = (List<LedgerTransaction>) state[1];
    }

    private void toSerializedState()
    {
        Object[] state = new Object[2];
        state[0] = this.accounts;
        state[1] = this.transactions;

        this.serializedState = Utils.writeObject(state);
    }

}
