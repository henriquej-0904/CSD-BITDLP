package itdlp.api.operations;

import itdlp.api.AccountId;

public class LedgerTransaction extends LedgerOperation {

    private AccountId origin, dest;

    public LedgerTransaction(AccountId origin, AccountId dest, int value, String date) {
        super(value, Type.TRANSACTION, date);
        this.origin = origin;
        this.dest = dest;
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
