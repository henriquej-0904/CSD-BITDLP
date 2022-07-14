package tp2.bitdlp.pow.transaction;

import java.security.MessageDigest;
import java.security.PublicKey;
import java.util.Map;
import java.util.SortedMap;
import java.util.Map.Entry;

import tp2.bitdlp.impl.srv.config.ServerConfig;
import tp2.bitdlp.util.Crypto;

public class SmartContract
{
    private String name;

    private byte[] code;

    private SortedMap<String, String> signatures;

    /**
     * 
     */
    public SmartContract() {
    }

    /**
     * @param name
     * @param code
     * @param signatures
     */
    public SmartContract(String name, byte[] code, SortedMap<String, String> signatures) {
        this.name = name;
        this.code = code;
        this.signatures = signatures;
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
     * @return the signatures
     */
    public SortedMap<String, String> getSignatures() {
        return signatures;
    }

    /**
     * @param signatures the signatures to set
     */
    public void setSignatures(SortedMap<String, String> signatures) {
        this.signatures = signatures;
    }

    public boolean verifySignatures(byte[] transactionDigest)
    {
        if (signatures == null || signatures.isEmpty())
            return false;
        
        Map<String, PublicKey> keys = ServerConfig.getAllReplicaKeys();

        for (Entry<String, String> sig : signatures.entrySet()) {
            PublicKey key = keys.get(sig.getKey());

            if (key == null)
                return false;

            if (!Crypto.verifySignature(key, sig.getValue(), transactionDigest))
                return false;
        }

        // all signatures are valid
        return true;
    }

    public MessageDigest digest(MessageDigest digest)
    {
        digest.update(name.getBytes());
        digest.update(code);
        
        for (Entry<String, String> sig : signatures.entrySet()) {
            digest.update(sig.getKey().getBytes());
            digest.update(sig.getValue().getBytes());
        }

        return digest;
    }
}
