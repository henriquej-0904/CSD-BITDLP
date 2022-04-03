package itdlp.api;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

public class User {

    byte[] id;
    String email;
    PublicKey key;


    public User(String email, PublicKey key) throws NoSuchAlgorithmException{

        this.email = email;
        this.key = key;

        //id creation
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] aux = digest.digest(email.getBytes(StandardCharsets.UTF_8));
        
        id = new byte[ aux.length + key.getEncoded().length];
        System.arraycopy(aux, 0, id, 0, aux.length);
        System.arraycopy(key.getEncoded(), 0, id, aux.length, key.getEncoded().length);
    }

    public String getEmail(){
        return email;
    }

    public byte[] getId(){
        return id;
    }

    public PublicKey getKey(){
        return key;
    }
    
}
