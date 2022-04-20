package itdlp.tp1.api;

import java.util.LinkedList;
import java.util.List;

import itdlp.tp1.api.operations.InvalidOperationException;
import itdlp.tp1.api.operations.LedgerOperation;
import itdlp.tp1.api.operations.LedgerTransaction;

/**
 * Represents an Account in the system.
 */
public class Account {

    private AccountId id;
    private UserId owner;

    private List<LedgerOperation> operations;

    private int balance;

    /**
     * Create an account from the specified id and owner.
     * @param id
     * @param owner
     */
    public Account(AccountId id, UserId owner){
        this.owner = owner;
        this.id = id;
        this.operations = new LinkedList<>();
        this.balance = 0;
    }

    /**
     * 
     */
    public Account() {
    }

    /**
     * Get the account id.
     * @return The account id.
     */
    public AccountId getId() {
        return id;
    }

    /**
     * Get the account owner.
     * @return The owner.
     */
    public UserId getOwner(){
        return owner;
    }

    /**
     * @param id the id to set
     */
    public void setId(AccountId id) {
        this.id = id;
    }

    /**
     * @param owner the owner to set
     */
    public void setOwner(UserId owner) {
        this.owner = owner;
    }

    /**
     * @return the balance
     */
    public int getBalance() {
        return balance;
    }

    /**
     * @param balance the balance to set
     */
    public void setBalance(int balance) {
        this.balance = balance;
    }


    /**
     * @return the operations
     */
    public List<LedgerOperation> getOperations() {
        return operations;
    }

    /**
     * @param operations the operations to set
     */
    public void setOperations(List<LedgerOperation> operations) {
        this.operations = operations;
    }

    /**
     * Process an operation in this account.
     * 
     * @param operation The operation to process.
     * @throws InvalidOperationException if the operation is invalid in this account.
     */
    public void processOperation(LedgerOperation operation) throws InvalidOperationException
    {
        int value = 0;
        switch (operation.getType()) {
            case DEPOSIT:
                value = operation.getValue();
                break;
            case TRANSACTION:
                value = processTransaction((LedgerTransaction)operation);
                break;
        }

        this.operations.add(operation);
        this.balance += value;
    }


    private int processTransaction(LedgerTransaction transaction) throws InvalidOperationException
    {
        if (getId().equals(transaction.getDest()))
            return transaction.getValue();
        
        assert getId().equals(transaction.getOrigin());

        if (getBalance() - transaction.getValue() < 0)
            throw new InvalidOperationException("Transaction over the balance limit.");

        return -transaction.getValue();
    }
}
