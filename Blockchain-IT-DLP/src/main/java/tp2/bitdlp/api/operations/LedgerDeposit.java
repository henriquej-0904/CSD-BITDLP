package tp2.bitdlp.api.operations;

import java.security.MessageDigest;

import tp2.bitdlp.api.AccountId;

public class LedgerDeposit extends LedgerOperation
{
    private static final long serialVersionUID = 44444451312312L;

    private AccountId accountId;

    /**
     * @throws InvalidOperationException
     * 
     */
    public LedgerDeposit(int value, AccountId id, byte[] clientSignature) throws InvalidOperationException {
        super(value, Type.DEPOSIT);
        this.accountId = id;
        this.clientSignature = clientSignature;
    }

    /**
     * @throws InvalidOperationException
     * 
     */
    public LedgerDeposit(int value, String date, AccountId id, byte[] clientSignature) throws InvalidOperationException {
        super(value, Type.DEPOSIT, date);
        this.accountId = id;
        this.clientSignature = clientSignature;
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

    @Override
    public byte[] digest() {
        return computeDigest().digest();
    }

    @Override
    protected MessageDigest computeDigest() {
        MessageDigest digest = super.computeDigest();
        digest.update(accountId.getObjectId());
        return digest;
    }
}
