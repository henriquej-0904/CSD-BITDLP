package itdlp.tp1.data.mongo;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import java.util.List;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;

import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import itdlp.tp1.data.LedgerDBlayer;
import itdlp.tp1.data.LedgerState;
import itdlp.tp1.data.mongo.operations.LedgerOperationDAO;
import itdlp.tp1.api.Account;
import itdlp.tp1.api.AccountId;
import itdlp.tp1.api.operations.LedgerDeposit;
import itdlp.tp1.api.operations.LedgerOperation;
import itdlp.tp1.api.operations.LedgerTransaction;
import itdlp.tp1.util.Pair;
import itdlp.tp1.util.Result;

/**
 * Impl of LegerDB with Mongo-DB.
 */
public class LedgerDBWithMongo extends LedgerDBlayer
{
    private static LedgerDBWithMongo instance;

    private static final String MONGO_DB_DATABASE = "ledgerDB";

    /**
     * Get the current instance of the Ledger DB.
     * @return The Ledger DB.
     */
    public static synchronized LedgerDBWithMongo getInstance()
	{
		if( instance != null)
			return instance;

		MongoClientSettings settings = MongoClientSettings.builder()
			.applyConnectionString(new ConnectionString(System.getenv("MONGO_DB_CONNECTION_STRING")))
			.build();

		MongoClient client = MongoClients.create(settings);
        instance = new LedgerDBWithMongo(client);
		return instance;
	}

    private MongoClient client;
	private MongoDatabase db;

    private MongoCollection<AccountDAO> accounts;
    private MongoCollection<LedgerOperationDAO> ledger;
    private MongoCollection<Nonces> nonces;


    /**
     * @param client
     */
    public LedgerDBWithMongo(MongoClient client) {
        this.client = client;
    }

    private synchronized void init()
    {
		if( this.db != null)
			return;

        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));

        this.db = this.client.getDatabase(MONGO_DB_DATABASE).withCodecRegistry(pojoCodecRegistry);

        this.accounts = this.db.getCollection("Accounts", AccountDAO.class);
        this.ledger = this.db.getCollection("Ledger", LedgerOperationDAO.class);
        this.nonces = this.db.getCollection("Nonces", Nonces.class);

		// Create indexes for id field
		IndexOptions indexOptions = new IndexOptions().unique(true);

		this.accounts.createIndex(Indexes.ascending("accountId"), indexOptions);
		this.nonces.createIndex(Indexes.ascending("left"), indexOptions);
	}

    @Override
    public Result<Account> createAccount(Account account) {
        init();
        return null;
    }

    @Override
    public Result<Account> getAccount(AccountId accountId) {
        init();
        return null;
    }

    @Override
    public Result<Integer> getBalance(AccountId accountId) {
        init();
        return null;
    }

    @Override
    public Result<Integer> getTotalValue(AccountId[] accounts) {
        init();
        return null;
    }

    @Override
    public Result<Integer> getGlobalLedgerValue() {
        init();
        return null;
    }

    @Override
    public Result<Void> loadMoney(LedgerDeposit deposit) {
        init();
        return null;
    }

    @Override
    public Result<Void> sendTransaction(LedgerTransaction transaction) {
        init();
        return null;
    }

    @Override
    public Result<LedgerOperation[]> getLedger() {
        init();
        return null;
    }

    @Override
    public Result<Void> loadState(LedgerState state) {
        init();
        return null;
    }

    @Override
    public Result<LedgerState> getState() {
        init();
        return null;
    }
    
    protected static class Nonces extends Pair<byte[], List<Integer>> {}

}
