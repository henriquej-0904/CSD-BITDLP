package itdlp.tp1.api.operations;

import itdlp.tp1.api.AccountId;

public class LedgerDeposit extends LedgerOperation
{
    private static final long serialVersionUID = 44444451312312L;

    private AccountId accountId;

    /**
     * @throws InvalidOperationException
     * 
     */
    public LedgerDeposit(int value, AccountId id) throws InvalidOperationException {
        super(value, Type.DEPOSIT);
        this.accountId = id;
    }

    /**
     * @throws InvalidOperationException
     * 
     */
    public LedgerDeposit(int value, String date, AccountId id) throws InvalidOperationException {
        super(value, Type.DEPOSIT, date);
        this.accountId = id;
    }

    /**
     * 
     */
    public LedgerDeposit() {
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
