package itdlp.tp1.util;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class Crypto {
    
    static{
        Security.addProvider(new BouncyCastleProvider());
    }

    public static final String DEFAULT_SIGNATURE_TRANSFORMATION = "SHA256withECDSA";
	public static final String DEFAULT_SIGNATURE_PROVIDER = "BC";

    public static final String DEFAULT_ASYMMETRIC_ALGORITHM = "EC";
    public static final AlgorithmParameterSpec DEFAULT_ASYMMETRIC_GEN_KEY_SPEC = new ECGenParameterSpec("secp256r1");


    public static KeyPair createKeyPairForEcc256bits(SecureRandom random)
    {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(DEFAULT_ASYMMETRIC_ALGORITHM);
            generator.initialize(DEFAULT_ASYMMETRIC_GEN_KEY_SPEC, random);
            return generator.generateKeyPair();
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public static Signature createSignatureInstance()
    {
        try {
            return Signature.getInstance(DEFAULT_SIGNATURE_TRANSFORMATION, DEFAULT_SIGNATURE_PROVIDER);
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new Error(e);
        }
    }

    public static PublicKey getPublicKey(byte[] publicKey) throws InvalidKeySpecException
    {
        try {
            X509EncodedKeySpec encodedCoinKey = new X509EncodedKeySpec(publicKey,
                DEFAULT_ASYMMETRIC_ALGORITHM);
            KeyFactory keyFactory = KeyFactory.getInstance(DEFAULT_ASYMMETRIC_ALGORITHM);

            return keyFactory.generatePublic(encodedCoinKey);

        } catch (NoSuchAlgorithmException e) {
            throw new Error(e);
        }
    }

    public static MessageDigest getSha256Digest()
    {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new Error(e);
        }
    }

}
