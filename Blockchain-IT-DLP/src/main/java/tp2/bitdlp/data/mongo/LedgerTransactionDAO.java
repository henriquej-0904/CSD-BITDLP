package tp2.bitdlp.data.mongo;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.core.JsonProcessingException;

import tp2.bitdlp.api.AccountId;
import tp2.bitdlp.pow.transaction.LedgerTransaction;
import tp2.bitdlp.pow.transaction.SmartContract;
import tp2.bitdlp.util.Utils;

public class LedgerTransactionDAO
{
    private ObjectId id;

    protected int value;
    protected LedgerTransaction.Type type;

    protected String clientSignature;

    protected AccountId origin, dest;
    
    protected int nonce;

    protected String smartContract;

    public LedgerTransactionDAO(LedgerTransaction transaction)
    {
        this.value = transaction.getValue();
        this.type = transaction.getType();
        this.clientSignature = transaction.getClientSignature();
        this.origin = transaction.getOrigin();
        this.dest = transaction.getDest();
        this.nonce = transaction.getNonce();
        try {
            this.smartContract = transaction.getSmartContract() == null ? null
                : Utils.json.writeValueAsString(transaction.getSmartContract());
        } catch (JsonProcessingException e) {}
    }

    /**
     * 
     */
    public LedgerTransactionDAO() {
    }

    /**
     * @return the id
     */
    public ObjectId getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(ObjectId id) {
        this.id = id;
    }

    /**
     * @return the value
     */
    public int getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(int value) {
        this.value = value;
    }

    /**
     * @return the type
     */
    public LedgerTransaction.Type getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(LedgerTransaction.Type type) {
        this.type = type;
    }

    /**
     * @return the clientSignature
     */
    public String getClientSignature() {
        return clientSignature;
    }

    /**
     * @param clientSignature the clientSignature to set
     */
    public void setClientSignature(String clientSignature) {
        this.clientSignature = clientSignature;
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

    /**
     * @return the nonce
     */
    public int getNonce() {
        return nonce;
    }

    /**
     * @param nonce the nonce to set
     */
    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    /**
     * @return the smartContract
     */
    public String getSmartContract() {
        return smartContract;
    }

    /**
     * @param smartContract the smartContract to set
     */
    public void setSmartContract(String smartContract) {
        this.smartContract = smartContract;
    }

    @Override
    public String toString() {
        return toLedgerTransaction().toString();
    }

    public LedgerTransaction toLedgerTransaction()
    {
        LedgerTransaction t = new LedgerTransaction();
        
        t.setValue(this.value);
        t.setType(this.type);
        t.setClientSignature(this.clientSignature);
        t.setOrigin(this.origin);
        t.setDest(this.dest);
        t.setNonce(this.nonce);

        if (this.smartContract != null)
            try {
                t.setSmartContract(Utils.json.readValue(this.smartContract, SmartContract.class));
            } catch (Exception e) {}

        return t;
    }
}
