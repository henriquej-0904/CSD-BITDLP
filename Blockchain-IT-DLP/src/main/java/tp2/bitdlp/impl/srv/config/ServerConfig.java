package tp2.bitdlp.impl.srv.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.net.ssl.SSLContext;

import tp2.bitdlp.data.LedgerDBlayer.DBtype;
import tp2.bitdlp.util.Crypto;

public class ServerConfig
{
    public static final String SERVER_CONFIG_FILE_NAME = "server-config.properties";

    private static enum Settings
    {
        DB_TYPE ((v) -> DBtype.valueOf(v.toUpperCase()), DBtype.MONGO.toString()),
        BC_Version ((v) -> Integer.decode(v), "0x0001"),
        BC_DifficultyTarget ((v) -> Integer.decode(v), "0x0000FFFF"),
        BC_NumTransactionsInBlock ((v) -> Integer.parseInt(v), "128"),
        BC_GenerationTransactionValue ((v) -> Integer.parseInt(v), "1000");

        private Function<String, Object> parseFunc;

        private String defaultValue;

        /**
         * @param parseFunc
         */
        private Settings(Function<String, Object> parseFunc, String defaultValue) {
            this.parseFunc = parseFunc;
            this.defaultValue = defaultValue;
        }

        /**
         * @return the parseFunc
         */
        public Function<String, Object> getParseFunc() {
            return parseFunc;
        }

        /**
         * @return the defaultValue
         */
        public String getDefaultValue() {
            return defaultValue;
        }  
    }

    private static Map<Settings, Object> settings;

    private static KeyPair keyPair;
    private static KeyStore keyStore;
    private static KeyStore trustStore;

    private static String replicaId;
    private static File replicaConfigFolder;

    public static void init(int replicaId) throws FileNotFoundException, IOException
    {
        ServerConfig.replicaId = "replica-" + replicaId;
        replicaConfigFolder = new File(Crypto.CONFIG_FOLDER, ServerConfig.replicaId);

        try (InputStream input = new FileInputStream(SERVER_CONFIG_FILE_NAME))
        {
            Properties props = new Properties();
            props.load(input);

            if (!props.keySet().containsAll(
                Stream.of(Settings.values()).map((v) -> v.toString())
                    .collect(Collectors.toList())))
                throw new RuntimeException(
                "There are missing settings.\nList of settings that must be defined: " + Arrays.toString(Settings.values()));

            settings = props.entrySet().stream()
            .map((entry) -> Map.entry(Settings.valueOf((String)entry.getKey()), (String)entry.getValue()))
            .collect(Collectors.toUnmodifiableMap(
                (entry) -> entry.getKey(),
                (entry) -> entry.getKey().getParseFunc().apply(entry.getValue())
            ));
        }
    }

    public static String getReplicaId() {
        return replicaId;
    }

    public static KeyPair getKeyPair() {
        if(keyPair == null){
            try {
                keyPair = new KeyPair(getKeyStore().getCertificate(replicaId).getPublicKey(), 
                                     (PrivateKey) getKeyStore().getKey(replicaId, Crypto.KEYSTORE_PWD.toCharArray()));

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

    public static SSLContext getSSLContext()
	{
		return Crypto.getSSLContext(getKeyStore(), getTrustStore(), Crypto.KEYSTORE_PWD);
	}

    // DB settings

    public static DBtype getDBtype()
    {
        return (DBtype)settings.get(Settings.DB_TYPE);
    }

    // BC settings

    /**
     * Get the current version of BC blocks.
     * @return the current version of BC blocks.
     */
    public static int getCurrentVersion()
    {
        return (int)settings.get(Settings.BC_Version);
    }

    /**
     * Get the difficulty target for the PoW.
     * The returned number represents the upper bound for the
     * valid hash found through PoW. The most significant 32 bits of
     * that hash must be less than this difficulty target.
     * 
     * @return the difficulty target for the PoW.
     */
    public static int getDifficultyTarget()
    {
        return (int)settings.get(Settings.BC_DifficultyTarget);
    }

    /**
     * Get the valid number of transaction in a block.
     * @return the valid number of transaction in a block.
     */
    public static int getValidNumberTransactionsInBlock()
    {
        return (int)settings.get(Settings.BC_NumTransactionsInBlock);
    }

    public static int getGenerationTransactionValue()
    {
        return (int)settings.get(Settings.BC_GenerationTransactionValue);
    }

    /**
     * Create a default server-config file
     * @param args
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException, IOException
    {
        try (OutputStream output = new FileOutputStream(SERVER_CONFIG_FILE_NAME))
        {
            Properties props = new Properties();

            Map<String, String> settings = Stream.of(Settings.values())
            .collect(Collectors.toUnmodifiableMap(
            (v) -> v.toString(), (v) -> v.getDefaultValue()));

            props.putAll(settings);
            props.store(output, "Server-config");
        }
    }
}
