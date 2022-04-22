package itdlp.tp1.impl.srv.config;

import java.io.File;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;

import itdlp.tp1.util.Crypto;

public class ServerConfig {
    
    private static final File CONFIG_FOLDER = new File("tls-config");

    private static KeyPair keyPair;
    private static KeyStore keyStore;
    private static KeyStore trustStore;

    private static String replicaId;
    private static File replicaConfigFolder;

    public static KeyPair getKeyPair() {
        if(keyPair == null){
            try {
                keyPair = new KeyPair(keyStore.getCertificate(replicaId).getPublicKey(), 
                                     (PrivateKey) keyStore.getKey(replicaId, Crypto.KEYSTORE_PWD.toCharArray()));

            } catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
                throw new Error(e.getMessage(), e);
            }
        }
        return keyPair;
    }

    public static KeyStore getKeyStore() {
        if(keyStore == null){
            keyStore = Crypto.getKeyStorePkcs12(new File(replicaConfigFolder, "keystore.pkcs12"), Crypto.KEYSTORE_PWD);
        }
        return keyStore;
    }

    public static KeyStore getTrustStore() {
        if(trustStore == null){
            trustStore = Crypto.getKeyStorePkcs12(new File(CONFIG_FOLDER, "truststore.pkcs12"), Crypto.KEYSTORE_PWD);
        }
        return trustStore;
    }

    public static String getReplicaId() {
        return replicaId;
    }

    public static void setReplicaId(int replicaId) {
        ServerConfig.replicaId = "replica-" + replicaId;
        replicaConfigFolder = new File(CONFIG_FOLDER, ServerConfig.replicaId);
    }
}
