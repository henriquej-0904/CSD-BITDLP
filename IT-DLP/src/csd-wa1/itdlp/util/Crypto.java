package itdlp.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Crypto {
    
    public static MessageDigest getSha256Digest()
    {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new Error(e);
        }
    }

}
