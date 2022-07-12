package tp2.bitdlp.impl.srv.resources.requests;

import tp2.bitdlp.util.Pair;

public class SmartContractValidation extends Request {

    private String name;
    private byte[] code;

    private Pair<byte[],byte[]> originDestPair;
    private int value;
    private int nonce;

    private String signature;

    public SmartContractValidation() {
        super(Operation.SMART_CONTRACT_VALIDATION_ASYNC);
    }

    /**
     * @param transaction
     */
    public SmartContractValidation(String name, byte[] code,
        Pair<byte[],byte[]> originDestPair, int value, int nonce,
        String signature)
    {
        super(Operation.SMART_CONTRACT_VALIDATION_ASYNC);
        this.name = name;
        this.code = code;
        this.originDestPair = originDestPair;
        this.value = value;
        this.nonce = nonce;
        this.signature = signature;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the code
     */
    public byte[] getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(byte[] code) {
        this.code = code;
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
     * @return the signature
     */
    public String getSignature() {
        return signature;
    }

    /**
     * @param signature the signature to set
     */
    public void setSignature(String signature) {
        this.signature = signature;
    }

    
}
