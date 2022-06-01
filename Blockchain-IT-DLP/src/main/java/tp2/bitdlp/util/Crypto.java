package tp2.bitdlp.util;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class Crypto {

    public static final File CONFIG_FOLDER = new File("tls-config");

    public static final String DEFAULT_SIGNATURE_TRANSFORMATION = "SHA256withECDSA";
	public static final String DEFAULT_SIGNATURE_PROVIDER = "BC";

    public static final String DEFAULT_ASYMMETRIC_ALGORITHM = "EC";
    public static final AlgorithmParameterSpec DEFAULT_ASYMMETRIC_GEN_KEY_SPEC = new ECGenParameterSpec("secp256r1");


    public static final String KEYSTORE_PWD = "keystorepwd";

    public static String sign(KeyPair keys, byte[]... data)
    {
        try {
            Signature signature = Crypto.createSignatureInstance();
            signature.initSign(keys.getPrivate());
        
            for (byte[] buff : data) {
                signature.update(buff);
            }

            return Utils.toHex(signature.sign());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static boolean verifySignature(PublicKey publicKey, String signature, byte[]... data){
        try {
            Signature verify = Crypto.createSignatureInstance();
            verify.initVerify(publicKey);

            for (byte[] buff : data)
                verify.update(buff);

            return verify.verify(Utils.fromHex(signature));
        } catch (Exception e) {
            return false;
        }
    }

    public static KeyStore getKeyStorePkcs12(File keystoreFile, String password)
    {
        return getKeyStore(keystoreFile, password, "PKCS12");
    }

    public static KeyStore getKeyStore(File keystoreFile, String password, String keystoreType)
    {
        try (FileInputStream input = new FileInputStream(keystoreFile))
        {
            KeyStore keystore = KeyStore.getInstance(keystoreType);
            keystore.load(input, password.toCharArray());
            return keystore;
        } catch (Exception e)
        {
            throw new Error(e.getMessage(), e);
        }
    }

    public static KeyStore getTrustStore() {
        return Crypto.getKeyStorePkcs12(new File(CONFIG_FOLDER, "truststore.pkcs12"), Crypto.KEYSTORE_PWD);
    }

    public static SSLContext getSSLContext(KeyStore keystore, KeyStore truststore, String password)
	{
        try {
            SSLContext sslContext = SSLContext.getInstance("TLSv1.3");

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");

            kmf.init(keystore, password.toCharArray());
            tmf.init(truststore);
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            return sslContext;
        } catch (Exception e) {
            throw new Error(e.getMessage(), e);
        }
	}

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
