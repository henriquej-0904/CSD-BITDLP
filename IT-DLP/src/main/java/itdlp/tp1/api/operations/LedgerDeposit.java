package itdlp.tp1.api.operations;

public class LedgerDeposit extends LedgerOperation
{

    /**
     * @throws InvalidOperationException
     * 
     */
    public LedgerDeposit(int value) throws InvalidOperationException {
        super(value, Type.DEPOSIT);
    }

    /**
     * @throws InvalidOperationException
     * 
     */
    public LedgerDeposit(int value, String date) throws InvalidOperationException {
        super(value, Type.DEPOSIT, date);
    }

    /**
     * 
     */
    public LedgerDeposit() {
    }

    @Override
    public String toString() {
        return super.toString() + getValue();
    }
}
