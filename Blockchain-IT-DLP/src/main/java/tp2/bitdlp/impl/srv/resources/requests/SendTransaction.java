package tp2.bitdlp.impl.srv.resources.requests;

import tp2.bitdlp.util.Pair;

public class SendTransaction extends Request {

    private Pair<byte[],byte[]> originDestPair;
    private int value;
    private String accountSignature;
    private int nonce;

    public SendTransaction() {
        super(Operation.SEND_TRANSACTION);
    }

    /**
     * @param transaction
     */
    public SendTransaction(Pair<byte[],byte[]> originDestPair, int value,
    String accountSignature, int nonce) {
        super(Operation.SEND_TRANSACTION);
        this.originDestPair = originDestPair;
        this.value = value;
        this.accountSignature = accountSignature;
        this.nonce = nonce;
    }

    /**
     * @return the originDestPair
     */
    public Pair<byte[], byte[]> getOriginDestPair() {
        return originDestPair;
    }

    /**
     * @param originDestPair the originDestPair to set
     */
    public void setOriginDestPair(Pair<byte[], byte[]> originDestPair) {
        this.originDestPair = originDestPair;
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
     * @return the accountSignature
     */
    public String getAccountSignature() {
        return accountSignature;
    }

    /**
     * @param accountSignature the accountSignature to set
     */
    public void setAccountSignature(String accountSignature) {
        this.accountSignature = accountSignature;
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

    
}
