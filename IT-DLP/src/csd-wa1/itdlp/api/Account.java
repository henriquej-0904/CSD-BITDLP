package itdlp.api;

/**
 * Represents an Account in the system.
 */
public class Account {

    private final AccountId id;
    private final UserId owner;

    /**
     * Create an account from the specified id and owner.
     * @param id
     * @param owner
     */
    public Account(AccountId id, UserId owner){
        this.owner = owner;
        this.id = id;
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

}
