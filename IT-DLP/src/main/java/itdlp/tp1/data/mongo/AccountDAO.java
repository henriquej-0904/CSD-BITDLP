package itdlp.tp1.data.mongo;

import java.util.LinkedList;
import java.util.List;

import org.bson.types.ObjectId;

import itdlp.tp1.api.AccountId;
import itdlp.tp1.api.UserId;

/**
 * Represents an Account in the system.
 */
public class AccountDAO {

    private AccountId id;
    private UserId owner;

    private List<ObjectId> operations;

    private int balance;

    /**
     * Create an account from the specified id and owner.
     * @param id
     * @param owner
     */
    public AccountDAO(AccountId id, UserId owner){
        this.owner = owner;
        this.id = id;
        this.operations = new LinkedList<>();
        this.balance = 0;
    }

    /**
     * 
     */
    public AccountDAO() {
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
    public List<ObjectId> getOperations() {
        return operations;
    }

    /**
     * @param operations the operations to set
     */
    public void setOperations(List<ObjectId> operations) {
        this.operations = operations;
    }
}
