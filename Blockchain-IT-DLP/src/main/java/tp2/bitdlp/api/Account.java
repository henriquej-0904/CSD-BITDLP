package tp2.bitdlp.api;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.LinkedList;
import java.util.List;

import tp2.bitdlp.pow.transaction.InvalidTransactionException;
import tp2.bitdlp.pow.transaction.LedgerTransaction;
import tp2.bitdlp.util.Crypto;

/**
 * Represents an Account in the system.
 */
public class Account implements Serializable {

    private static final long serialVersionUID = 1548556626L;

    private AccountId id;
    private UserId owner;

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

    /**
     * @return the operations
     */
    public List<LedgerTransaction> getTransactions() {
        return transactions;
    }

    /**
     * @param operations the operations to set
     */
    public void setOperations(List<LedgerTransaction> operations) {
        this.transactions = operations;
    }

    /**
     * Process an operation in this account.
     * 
     * @param operation The operation to process.
     * @throws InvalidTransactionException if the operation is invalid in this account.
     */
    public void processOperation(LedgerTransaction transaction) throws InvalidTransactionException
    {
        int value = verifyTransaction(transaction);
        this.transactions.add(transaction);
        this.balance += value;
    }

    public byte[] digest()
    {
        MessageDigest digest =  Crypto.getSha256Digest();
        digest.update(getId().getObjectId());
        digest.update(getOwner().getObjectId());

        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(getBalance());

        digest.update(buffer.array());

        for (LedgerTransaction LedgerTransaction : transactions) {
            digest.update(LedgerTransaction.digest());
        }

        return digest.digest();
    }

    private int verifyTransaction(LedgerTransaction transaction)
    {
        if (getId().equals(transaction.getDest()))
            return transaction.getValue();
        
        assert getId().equals(transaction.getOrigin());

        if (getBalance() - transaction.getValue() < 0)
            throw new InvalidTransactionException("Transaction over the balance limit.");

        return -transaction.getValue();
    }
}
