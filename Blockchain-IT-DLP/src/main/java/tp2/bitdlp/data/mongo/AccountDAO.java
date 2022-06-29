package tp2.bitdlp.data.mongo;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import tp2.bitdlp.api.Account;
import tp2.bitdlp.api.AccountId;
import tp2.bitdlp.api.UserId;

/**
 * Represents an Account in the system.
 */
public class AccountDAO {

    private AccountId accountId;
    private UserId owner;

    private List<LedgerTransactionDAO> operations;

    private int balance;

    /**
     * Create an account from the specified id and owner.
     * @param id
     * @param owner
     */
    public AccountDAO(AccountId id, UserId owner){
        this.owner = owner;
        this.accountId = id;
        this.operations = new LinkedList<>();
        this.balance = 0;
    }

    public AccountDAO(Account account)
    {
        this(account.getId(), account.getOwner());
        this.balance = account.getBalance();
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
    public AccountId getAccountId() {
        return accountId;
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
    public void setAccountId(AccountId id) {
        this.accountId = id;
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
    public List<LedgerTransactionDAO> getOperations() {
        return operations;
    }

    /**
     * @param operations the operations to set
     */
    public void setOperations(List<LedgerTransactionDAO> operations) {
        this.operations = operations;
    }

    public Account toAccount()
    {
        Account account = new Account(this.accountId, this.owner);
        account.setBalance(this.balance);
        account.setOperations(this.operations.stream()
            .map(LedgerTransactionDAO::toLedgerTransaction)
            .collect(Collectors.toList()));

        return account;
    }
}
