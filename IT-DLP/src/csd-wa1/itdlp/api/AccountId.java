package itdlp.api;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PublicKey;

import org.apache.commons.lang3.NotImplementedException;

import itdlp.util.Crypto;

/**
 * Represents an AccountId = SHA256(email || timestamp) || public key
 */
public class AccountId extends ObjectId
{
    private static final int HASH_BYTES_LENGTH = 256/8;

    /**
     * Create an AccountId object from the specified email and public key.
     * 
     * @param email The user email.
     * @param key The account public key.
     */
    public AccountId(String email, PublicKey key) {
        super(generateId(email, key));
    }

    /**
     * Create a AccountId object from the specified id.
     * 
     * @param id The user id.
     */
    public AccountId(byte[] id) {
        super(id);
        
        if (id.length <= HASH_BYTES_LENGTH)
            throw new IllegalArgumentException("Invalid Account Id.");
    }

    /**
     * Generate the account id from the specified email and public key.
     * 
     * @param email The user email.
     * @param key The account public key.
     * 
     * @return The generated id.
     */
    private static byte[] generateId(String email, PublicKey key) {
        // id creation
        MessageDigest digest = Crypto.getSha256Digest();

        digest.update(email.getBytes(StandardCharsets.UTF_8));
        //TODO: VER NA SEXTA
        //digest.update(SRN.getBytes(StandardCharsets.UTF_8));

        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(System.currentTimeMillis());

        byte[] aux = digest.digest(buffer.array());

        byte[] id = new byte[aux.length + key.getEncoded().length];
        System.arraycopy(aux, 0, id, 0, aux.length);
        System.arraycopy(key.getEncoded(), 0, id, aux.length, key.getEncoded().length);

        return id;
    }

    @Override
    public PublicKey getPublicKey() {
        throw new NotImplementedException();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof AccountId) && super.equals(obj);
    }
    
}

