package itdlp.api;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PublicKey;

import itdlp.util.Crypto;

public class UserId {

    private byte[] id;

    /**
     * Create a UserId object from the specified email and public key.
     * 
     * @param email The user email.
     * @param key The user public key.
     */
    public UserId(String email, PublicKey key) {
        this.id = generateId(email, key);
    }

    /**
     * Create a UserId object from the specified id.
     * 
     * @param id The user id.
     */
    public UserId(byte[] id) {
        this.id = id;
    }

    /**
     * Generate the user id from the specified email and public key.
     * 
     * @param email The user email.
     * @param key The user public key.
     * 
     * @return The generated id.
     */
    private static byte[] generateId(String email, PublicKey key) {
        // id creation
        MessageDigest digest = Crypto.getSha256Digest();
        byte[] aux = digest.digest(email.getBytes(StandardCharsets.UTF_8));

        byte[] id = new byte[aux.length + key.getEncoded().length];
        System.arraycopy(aux, 0, id, 0, aux.length);
        System.arraycopy(key.getEncoded(), 0, id, aux.length, key.getEncoded().length);

        return id;
    }

    /**
     * @return the id
     */
    public byte[] getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(byte[] id) {
        this.id = id;
    }
    
}

