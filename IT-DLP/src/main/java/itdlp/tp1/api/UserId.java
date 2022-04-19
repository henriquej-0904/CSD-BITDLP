package itdlp.tp1.api;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import itdlp.tp1.util.Crypto;

/**
 * Represents a UserId = SHA256(email) || public key
 */
public class UserId extends ObjectId
{
    private static final int HASH_BYTES_LENGTH = 256/8;

    /**
     * Create a UserId object from the specified email and public key.
     * 
     * @param email The user email.
     * @param key The user public key.
     */
    public UserId(String email, PublicKey key) {
        super(generateId(email, key));
    }

    /**
     * Create a UserId object from the specified id.
     * 
     * @param id The user id.
     */
    public UserId(byte[] id) {
        super(id);

        if (id.length <= HASH_BYTES_LENGTH)
            throw new IllegalArgumentException("Invalid User Id.");
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

    @Override
    public PublicKey getPublicKey() throws InvalidKeySpecException {
        byte[] publicKeyBytes = Arrays.copyOfRange(this.id, HASH_BYTES_LENGTH, this.id.length);
        return Crypto.getPublicKey(publicKeyBytes);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof UserId) && super.equals(obj);
    }
    
}

