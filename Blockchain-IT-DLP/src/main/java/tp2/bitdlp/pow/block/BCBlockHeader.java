package tp2.bitdlp.pow.block;

import java.nio.ByteBuffer;
import java.security.MessageDigest;

import tp2.bitdlp.impl.srv.config.ServerConfig;

public class BCBlockHeader
{
    private int version;

    private String previousHash;

    private String merkleRoot;

    private int nonce;

    private int timeStamp;

    private int diffTarget;

    public BCBlockHeader() {
    }

    /**
     * Create a BC block header with default version, difficulty target and initial nonce = 0.
     * 
     * @param previousHash
     * @param merkleRoot
     * @param timeStamp
     */
    public BCBlockHeader(String previousHash, String merkleRoot, int timeStamp)
    {
        this.version = ServerConfig.getCurrentVersion();
        this.diffTarget = ServerConfig.getDifficultyTarget();
        this.nonce = 0;

        this.previousHash = previousHash;
        this.merkleRoot = merkleRoot;
        this.timeStamp = timeStamp;
    }

    /**
     * Create a BC block header with default version, difficulty target, initial nonce = 0
     * and timeStamp set to the number of seconds since Unix Epoch.
     * 
     * @param previousHash
     * @param merkleRoot
     */
    public BCBlockHeader(String previousHash, String merkleRoot)
    {
        this(previousHash, merkleRoot, (int)System.currentTimeMillis() / 1000);
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

    public String getMerkleRoot() {
        return merkleRoot;
    }

    public void setMerkleRoot(String merkleRoot) {
        this.merkleRoot = merkleRoot;
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

    public int getDiffTarget() {
        return diffTarget;
    }

    public void setDiffTarget(int diffTarget) {
        this.diffTarget = diffTarget;
    }

    public MessageDigest digest(MessageDigest digest)
    {
        ByteBuffer buffer = ByteBuffer.allocate(4*Integer.BYTES);
        buffer.putInt(version);
        buffer.putInt(nonce);
        buffer.putInt(timeStamp);
        buffer.putInt(diffTarget);

        digest.update(buffer.array());
        digest.update(previousHash.getBytes());
        digest.update(merkleRoot.getBytes());
        
        return digest;
    }

}
