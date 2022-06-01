package tp2.bitdlp.impl.srv.config;

import java.io.File;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.SSLContext;

import tp2.bitdlp.util.Crypto;

public class ServerConfig {

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
            trustStore = Crypto.getTrustStore();
        }
        return trustStore;
    }

    public static String getReplicaId() {
        return replicaId;
    }

    public static void setReplicaId(int replicaId) {
        ServerConfig.replicaId = "replica-" + replicaId;
        replicaConfigFolder = new File(Crypto.CONFIG_FOLDER, ServerConfig.replicaId);
    }

    public static SSLContext getSSLContext()
	{
		return Crypto.getSSLContext(getKeyStore(), getTrustStore(), Crypto.KEYSTORE_PWD);
	}
}
