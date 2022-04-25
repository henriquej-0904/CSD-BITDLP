package itdlp.tp1.data.mongo;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import static com.mongodb.client.model.Filters.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import itdlp.tp1.data.LedgerDBlayer;
import itdlp.tp1.data.LedgerState;
import itdlp.tp1.data.mongo.operations.LedgerDepositDAO;
import itdlp.tp1.data.mongo.operations.LedgerOperationDAO;
import itdlp.tp1.data.mongo.operations.LedgerTransactionDAO;
import itdlp.tp1.api.Account;
import itdlp.tp1.api.AccountId;
import itdlp.tp1.api.UserId;
import itdlp.tp1.api.operations.InvalidOperationException;
import itdlp.tp1.api.operations.LedgerDeposit;
import itdlp.tp1.api.operations.LedgerOperation;
import itdlp.tp1.api.operations.LedgerTransaction;
import itdlp.tp1.util.Pair;
import itdlp.tp1.util.Result;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;

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

        try {
            PojoCodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true)
                .conventions(Conventions.DEFAULT_CONVENTIONS).build();

            CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(),
                    fromProviders(pojoCodecProvider));

            this.db = this.client.getDatabase(MONGO_DB_DATABASE).withCodecRegistry(pojoCodecRegistry);

            this.accounts = this.db.getCollection("Accounts", AccountDAO.class);
            this.ledger = this.db.getCollection("Ledger", LedgerOperationDAO.class);
            this.nonces = this.db.getCollection("Nonces", Nonces.class);

            // Create indexes for id field
            IndexOptions indexOptions = new IndexOptions().unique(true);

            this.accounts.createIndex(Indexes.ascending("accountId"), indexOptions);
            this.nonces.createIndex(Indexes.ascending("left"), indexOptions);

        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage(), e);
        }
	}

    @Override
    public Result<Account> createAccount(Account account) {
        init();

        try {
            this.accounts.insertOne(new AccountDAO(account));
            return Result.ok(account);
        } catch (MongoException e) {
            return accountAlreadyExistsConflict(account.getId());
        }
    }

    @Override
    public Result<Account> getAccount(AccountId accountId) {
        init();

        try {

            FindIterable<AccountDAO> result = this.accounts.find(eq("accountId", accountId));
            if (result == null)
                return accountNotFound(accountId);

            AccountDAO accountDAO = result.first();
            
            FindIterable<LedgerOperationDAO> operations =
                this.ledger.find(in("id", accountDAO.getOperations()));

            Account account = accountDAO.toAccount();
            List<LedgerOperation> accountOps = account.getOperations();

            if (operations != null)
            {
                for (LedgerOperationDAO ledgerOperationDAO : operations) {
                    accountOps.add(ledgerOperationDAO.toLedgerOperation());
                }
            }

            return Result.ok(account);
        } catch (MongoException e) {
            return accountNotFound(accountId);
        }
    }

    @Override
    public Result<Integer> getBalance(AccountId accountId) {
        init();

        AggregateIterable<AccountDAO> result = this.accounts.aggregate(Arrays.asList(
			Aggregates.match(eq("accountId", accountId)),
            Aggregates.project(Projections.include("balance"))
			));

        if (result.first() == null)
            return accountNotFound(accountId);

        return Result.ok(result.first().getBalance());
    }

    @Override
    public Result<Integer> getTotalValue(AccountId[] accounts) {
        init();

        MongoCollection<Document> collection = this.db.getCollection("Accounts");
		AggregateIterable<Document> result = collection.aggregate(Arrays.asList(
    		Aggregates.match(in("accountId", accounts)),
            Aggregates.project(Projections.include("balance")),
			Aggregates.group(null, Accumulators.sum("total_balance", "$balance"))
			));

        if (result.first() == null)
            return Result.ok(0);

        return Result.ok(result.first().getInteger("total_balance"));
    }

    @Override
    public Result<Integer> getGlobalLedgerValue() {
        init();

        MongoCollection<Document> collection = this.db.getCollection("Accounts");
		AggregateIterable<Document> result = collection.aggregate(Arrays.asList(
            Aggregates.project(Projections.include("balance")),
			Aggregates.group(null, Accumulators.sum("total_balance", "$balance"))
			));

        if (result.first() == null)
            return Result.ok(0);

        return Result.ok(result.first().getInteger("total_balance"));
    }

    @Override
    public Result<Void> loadMoney(LedgerDeposit deposit) {
        init();

        try {
            UpdateOptions op = new UpdateOptions();
            op.upsert(false);

            this.accounts.updateOne(eq("accountId", deposit.getAccountId()),
                Updates.inc("balance", deposit.getValue()), op);

        } catch (MongoException e) {
            return accountNotFound(deposit.getAccountId());
        }

        try {
            ObjectId operationId = this.ledger.insertOne(toDAO(deposit)).getInsertedId().asObjectId().getValue();

            UpdateOptions op = new UpdateOptions();
            op.upsert(false);

            this.accounts.updateOne(eq("accountId", deposit.getAccountId()),
                Updates.push("operations", operationId), op);

            return Result.ok();

        }catch (MongoException e) {
            return Result.error(new InternalServerErrorException(e.getMessage(), e));
        }
    }

    @Override
    public Result<Void> sendTransaction(LedgerTransaction transaction) {
        init();

        UpdateOptions updateOptions = new UpdateOptions();
        updateOptions.upsert(false);

        try {
            Bson originFilter = and(
                eq("accountId", transaction.getOrigin()),
                gte("balance", transaction.getValue())
            );

            Bson destFilter = eq("accountId", transaction.getDest());

            this.accounts.bulkWrite(Arrays.asList(
                new UpdateOneModel<>(originFilter,
                    Updates.inc("balance", -transaction.getValue()),
                    updateOptions),
                
                new UpdateOneModel<>(destFilter,
                    Updates.inc("balance", transaction.getValue()),
                    updateOptions)
            ));

        } catch (MongoException e) {
            return Result.error(new WebApplicationException(e.getMessage(), e, Status.CONFLICT.getStatusCode()));
        }

        try {
            ObjectId operationId = this.ledger.insertOne(toDAO(transaction)).getInsertedId().asObjectId().getValue();

            this.accounts.bulkWrite(Arrays.asList(
                new UpdateOneModel<>(eq("accountId", transaction.getOrigin()),
                    Updates.push("operations", operationId),
                    updateOptions),
                
                new UpdateOneModel<>(eq("accountId", transaction.getDest()),
                    Updates.push("operations", operationId),
                    updateOptions)
            ));

            return Result.ok();

        }catch (MongoException e) {
            return Result.error(new InternalServerErrorException(e.getMessage(), e));
        }
    }

    @Override
    public Result<LedgerOperation[]> getLedger() {
        init();

        AggregateIterable<LedgerOperationDAO> result = this.ledger.aggregate(Arrays.asList(
			Aggregates.sort(Sorts.ascending("ts"))
			));

        if (result.first() == null)
            return Result.error(new NotFoundException("The ledger is empty."));

        List<LedgerOperation> list = new LinkedList<>();

        for (LedgerOperationDAO lO : result) {
            list.add(lO.toLedgerOperation());
        }

        return Result.ok(list.toArray(new LedgerOperation[0]));
    }

    @Override
    public Result<Void> loadState(LedgerState state) {
        init();

        return Result.error(500);
    }

    @Override
    public Result<LedgerState> getState() {
        init();
        
        List<Pair<AccountId,UserId>> resAccounts = new LinkedList<>();
        List<LedgerOperation> resOperations = new LinkedList<>();

        for (AccountDAO account : this.accounts.find()) {
            resAccounts.add(new Pair<>(account.getAccountId(), account.getOwner()));
        }
        
        AggregateIterable<LedgerOperationDAO> ledgerIt = this.ledger.aggregate(Arrays.asList(
		    Aggregates.sort(Sorts.ascending("ts"))
			));

        for (LedgerOperationDAO operation : ledgerIt) {
            resOperations.add(operation.toLedgerOperation());
        }

        return Result.ok(new LedgerState(resAccounts, resOperations));
    }

    
    protected LedgerDepositDAO toDAO(LedgerDeposit deposit)
    {
        try {
            return new LedgerDepositDAO(deposit);
        } catch (InvalidOperationException e) {
            throw new Error(e.getMessage(), e);
        }
    }

    protected LedgerTransactionDAO toDAO(LedgerTransaction transaction)
    {
        try {
            return new LedgerTransactionDAO(transaction);
        } catch (InvalidOperationException e) {
            throw new Error(e.getMessage(), e);
        }
    }

    
    protected static class Nonces extends Pair<byte[], List<Integer>> {}

}
