package itdlp.tp1.data.mongo.operations;

import org.bson.codecs.pojo.annotations.BsonDiscriminator;

import itdlp.tp1.api.AccountId;
import itdlp.tp1.api.operations.InvalidOperationException;
import itdlp.tp1.api.operations.LedgerDeposit;
import itdlp.tp1.api.operations.LedgerOperation;

@BsonDiscriminator(value = "LedgerDepositDAO", key = "_cls")
public class LedgerDepositDAO extends LedgerOperationDAO
{
    private AccountId accountId;

    /**
     * @throws InvalidOperationException
     * 
     */
    public LedgerDepositDAO(int value, AccountId id, byte[] clientSignature) throws InvalidOperationException {
        super(value, Type.DEPOSIT);
        this.accountId = id;
        this.clientSignature = clientSignature;
    }

    /**
     * @throws InvalidOperationException
     * 
     */
    public LedgerDepositDAO(int value, String date, AccountId id, byte[] clientSignature) throws InvalidOperationException {
        super(value, Type.DEPOSIT, date);
        this.accountId = id;
        this.clientSignature = clientSignature;
    }

    /**
     * @throws InvalidOperationException
     * 
     */
    public LedgerDepositDAO(LedgerDeposit deposit) throws InvalidOperationException {
        this(deposit.getValue(), deposit.getDate(), deposit.getAccountId(), deposit.getClientSignature());
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

    public LedgerOperation toLedgerDeposit() {
        try {
            LedgerDeposit deposit = new LedgerDeposit(getValue(), getDate(), getAccountId(), getClientSignature());
            return deposit;
        } catch (Exception e) {
            throw new Error(e.getMessage(), e);
        }
    }
}
