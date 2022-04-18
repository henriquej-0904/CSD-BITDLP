package itdlp.api.operations;

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

    @Override
    public String toString() {
        return super.toString() + getValue();
    }
}
