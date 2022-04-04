package itdlp.api;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PublicKey;

import itdlp.api.User.UserId;
import itdlp.util.Crypto;

public class Account {

    private final AccountId id;
    private final UserId owner;

    public Account(AccountId id, UserId owner){
        this.owner = owner;
        this.id = id;
    }

    public AccountId getId() {
        return id;
    }

    public UserId getOwner(){
        return owner;
    }

    
    public static class AccountId{

        private final String email;
        private final PublicKey key;

        public AccountId(String email, PublicKey key){
            this.email = email;
            this.key = key;
        }

         /**
         * @return the id
         */
        public byte[] getId() {
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
        
        public String getEmail() {
            return email;
        }

        public PublicKey getKey() {
            return key;
        }

    }

}
