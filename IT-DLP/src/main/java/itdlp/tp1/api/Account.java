package itdlp.tp1.api;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import itdlp.tp1.api.operations.InvalidOperationException;
import itdlp.tp1.api.operations.LedgerDeposit;
import itdlp.tp1.api.operations.LedgerOperation;
import itdlp.tp1.api.operations.LedgerTransaction;

/**
 * Represents an Account in the system.
 */
public class Account implements Serializable {

    private static final long serialVersionUID = 1548556626L;

    private AccountId id;
    private UserId owner;

    private List<LedgerDeposit> deposits;
    private List<LedgerTransaction> transactions;

    private int balance;

    /**
     * Create an account from the specified id and owner.
     * @param id
     * @param owner
     */
    public Account(AccountId id, UserId owner){
        this.owner = owner;
        this.id = id;
        this.deposits = new LinkedList<>();
        this.transactions = new LinkedList<>();
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
   

    public List<LedgerDeposit> getDeposits() {
        return deposits;
    }

    public void setDeposits(List<LedgerDeposit> deposits) {
        this.deposits = deposits;
    }

    public List<LedgerTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<LedgerTransaction> transactions) {
        this.transactions = transactions;
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
                this.deposits.add((LedgerDeposit)operation);
                break;
            case TRANSACTION:
                value = processTransaction((LedgerTransaction)operation);
                this.transactions.add((LedgerTransaction)operation);
                break;
        }
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
