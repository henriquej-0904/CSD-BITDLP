package itdlp.api.operations;

public class LedgerDeposit extends LedgerOperation
{

    /**
     * 
     */
    public LedgerDeposit(int value, String date) {
        super(value, Type.DEPOSIT, date);
    }

    @Override
    public String toString() {
        return super.toString() + getValue();
    }
}
