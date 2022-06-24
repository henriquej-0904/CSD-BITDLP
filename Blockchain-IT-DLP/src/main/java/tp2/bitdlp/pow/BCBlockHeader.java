package tp2.bitdlp.pow;

public class BCBlockHeader {

    private String previousHash;

    private int version;

    private String merkelRoot;

    private int nonce;

    private int timeStamp;

    private int difTarget;

    public BCBlockHeader() {
    }

    public BCBlockHeader(String previousHash, int version, String merkelRoot, int nonce, int timeStamp, int difTarget) {
        this.previousHash = previousHash;
        this.version = version;
        this.merkelRoot = merkelRoot;
        this.nonce = nonce;
        this.timeStamp = timeStamp;
        this.difTarget = difTarget;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getMerkelRoot() {
        return merkelRoot;
    }

    public void setMerkelRoot(String merkelRoot) {
        this.merkelRoot = merkelRoot;
    }

    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    public int getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(int timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getDifTarget() {
        return difTarget;
    }

    public void setDifTarget(int difTarget) {
        this.difTarget = difTarget;
    }

}
