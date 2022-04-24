package itdlp.tp1.data.mongo.operations;

import itdlp.tp1.api.AccountId;
import itdlp.tp1.api.operations.InvalidOperationException;

public class LedgerDepositDAO extends LedgerOperationDAO
{
    private AccountId accountId;

    /**
     * @throws InvalidOperationException
     * 
     */
    public LedgerDepositDAO(int value, AccountId id) throws InvalidOperationException {
        super(value, Type.DEPOSIT);
        this.accountId = id;
    }

    /**
     * @throws InvalidOperationException
     * 
     */
    public LedgerDepositDAO(int value, String date, AccountId id) throws InvalidOperationException {
        super(value, Type.DEPOSIT, date);
        this.accountId = id;
    }

    /**
     * 
     */
    public LedgerDepositDAO() {
    }

    public AccountId getAccountId() {
        return accountId;
    }

    public void setAccountId(AccountId id) {
        this.accountId = id;
    }

    @Override
    public String toString() {
        return super.toString() + getValue();
    }
}
