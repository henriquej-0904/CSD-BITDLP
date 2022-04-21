package itdlp.tp1.api.operations;

import itdlp.tp1.api.AccountId;

public class LedgerTransaction extends LedgerOperation {

    private static final long serialVersionUID = 232326588562L;

    private AccountId origin, dest;

    public LedgerTransaction(AccountId origin, AccountId dest, int value) throws InvalidOperationException {
        super(value, Type.TRANSACTION);
        this.origin = origin;
        this.dest = dest;
    }

    public LedgerTransaction(AccountId origin, AccountId dest, int value, String date) throws InvalidOperationException {
        super(value, Type.TRANSACTION, date);
        this.origin = origin;
        this.dest = dest;
    }

    /**
     * 
     */
    public LedgerTransaction() {
    }

    /**
     * @return the origin
     */
    public AccountId getOrigin() {
        return origin;
    }

    /**
     * @param origin the origin to set
     */
    public void setOrigin(AccountId origin) {
        this.origin = origin;
    }

    /**
     * @return the dest
     */
    public AccountId getDest() {
        return dest;
    }

    /**
     * @param dest the dest to set
     */
    public void setDest(AccountId dest) {
        this.dest = dest;
    }

    @Override
    public String toString() {
        return super.toString() + String.format("%s -> %s, value = %d", origin, dest, getValue());
    }
}
