package itdlp.api;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PublicKey;

import itdlp.util.Crypto;

public class User {

    private final UserId id;

    public User(UserId id) {
        this.id = id;
    }

    public UserId getId() {
        return id;
    }

    public static class UserId {

        private final String email;
        private final PublicKey key;

        /**
         * @param id
         */
        public UserId(String email, PublicKey key) {
            this.email = email;
            this.key = key;
        }

        /**
         * @return the id
         */
        public byte[] getId() {
            // id creation
            MessageDigest digest = Crypto.getSha256Digest();
            byte[] aux = digest.digest(email.getBytes(StandardCharsets.UTF_8));

            byte[] id = new byte[aux.length + key.getEncoded().length];
            System.arraycopy(aux, 0, id, 0, aux.length);
            System.arraycopy(key.getEncoded(), 0, id, aux.length, key.getEncoded().length);

            return id;
        }

        public String getEmail() {
            return email;
        }

        public PublicKey getKey() {
            return key;
        }
        
    }

}
