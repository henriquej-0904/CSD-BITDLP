package itdlp.tp1.api.operations;

import itdlp.tp1.api.AccountId;

public class LedgerDeposit extends LedgerOperation
{
    private static final long serialVersionUID = 44444451312312L;

    private AccountId id;

    /**
     * @throws InvalidOperationException
     * 
     */
    public LedgerDeposit(int value, AccountId id) throws InvalidOperationException {
        super(value, Type.DEPOSIT);
        this.id = id;
    }

    /**
     * @throws InvalidOperationException
     * 
     */
    public LedgerDeposit(int value, String date, AccountId id) throws InvalidOperationException {
        super(value, Type.DEPOSIT, date);
        this.id = id;
    }

    /**
     * 
     */
    public LedgerDeposit() {
    }

    public AccountId getId() {
        return id;
    }

    public void setId(AccountId id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return super.toString() + getValue();
    }
}
