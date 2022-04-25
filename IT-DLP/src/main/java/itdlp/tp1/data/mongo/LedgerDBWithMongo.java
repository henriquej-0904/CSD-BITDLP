package itdlp.tp1.data.mongo;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import static com.mongodb.client.model.Filters.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

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
import com.mongodb.client.model.InsertManyOptions;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.WriteModel;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.UpdateResult;

import org.bson.BsonValue;
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
import jakarta.ws.rs.ForbiddenException;
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
            createCollections();
            
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage(), e);
        }
	}

    protected void createCollections() {
        this.accounts = this.db.getCollection("Accounts", AccountDAO.class);
        this.ledger = this.db.getCollection("Ledger", LedgerOperationDAO.class);
        this.nonces = this.db.getCollection("Nonces", Nonces.class);

        // Create indexes for id field
        IndexOptions indexOptions = new IndexOptions().unique(true);

        this.accounts.createIndex(Indexes.ascending("accountId"), indexOptions);
        this.nonces.createIndex(Indexes.ascending("left"), indexOptions);
    }

    protected void dropCollections() {
        this.accounts.drop();
        this.ledger.drop();
        this.nonces.drop();
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
            AccountDAO accountDAO = result.first();
            if (accountDAO == null)
                return accountNotFound(accountId);
            
            FindIterable<LedgerOperationDAO> operations =
                this.ledger.find(in("id", accountDAO.getOperations()));

            Account account = accountDAO.toAccount();
            List<LedgerOperation> accountOps = account.getOperations();

            for (LedgerOperationDAO ledgerOperationDAO : operations) {
                accountOps.add(ledgerOperationDAO.toLedgerOperation());
            }

            return Result.ok(account);
        } catch (MongoException e) {
            return Result.error(new InternalServerErrorException(e.getMessage(), e));
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

        UpdateOptions op = new UpdateOptions();
        op.upsert(false);

        try {
            UpdateResult result = this.accounts.updateOne(eq("accountId", deposit.getAccountId()),
                    Updates.inc("balance", deposit.getValue()), op);

            if (result.getMatchedCount() == 0)
                return accountNotFound(deposit.getAccountId());

            ObjectId operationId = this.ledger.insertOne(toDAO(deposit))
                    .getInsertedId().asObjectId().getValue();

            this.accounts.updateOne(eq("accountId", deposit.getAccountId()),
                    Updates.push("operations", operationId), op);
        }
        catch (Exception e) {
            return Result.error(new InternalServerErrorException(e.getMessage(), e));
        }

        return Result.ok();
    }

    @Override
    public Result<Void> sendTransaction(LedgerTransaction transaction) {
        init();

        try {
            // add nonce

            UpdateOptions options = new UpdateOptions();
            options.upsert(true);

            Bson filter = and(
                    eq("left", transaction.digest()),
                    not(in("right", transaction.getNonce())));

            UpdateResult result = this.nonces.updateOne(filter,
                    Updates.addToSet("right", transaction.getNonce()), options);

            if (!(result.getMatchedCount() > 0 || result.getUpsertedId() != null))
                return Result.error(new ForbiddenException("Invalid nonce."));

            // apply operation

            if (!checkAccountExists(transaction.getOrigin()))
                return accountNotFound(transaction.getOrigin());

            if (!checkAccountExists(transaction.getDest()))
                return accountNotFound(transaction.getDest());

            Bson originFilter = and(
                    eq("accountId", transaction.getOrigin()),
                    gte("balance", transaction.getValue()));

            Bson destFilter = eq("accountId", transaction.getDest());

            options.upsert(false);

            result = this.accounts.updateOne(originFilter,
                Updates.inc("balance", -transaction.getValue()), options);
            
            if (result.getMatchedCount() == 0)
                return Result.error(new WebApplicationException("Not enough money.", Status.CONFLICT));

            this.accounts.updateOne(destFilter, Updates.inc("balance", transaction.getValue()), options);
            
            // add operation to ledger
            ObjectId operationId = this.ledger.insertOne(toDAO(transaction)).getInsertedId().asObjectId()
                    .getValue();

            filter = or(
                    eq("accountId", transaction.getOrigin()),
                    eq("accountId", transaction.getDest()));

            this.accounts.updateMany(filter,
                    Updates.push("operations", operationId), options);

            return Result.ok();

        } catch (Exception e) {
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

        try {
            dropCollections();
            createCollections();

            List<LedgerOperation> operations = state.getOperations();
            List<LedgerOperationDAO> operationsDAO = loadOperations(state);

            Map<AccountId, Pair<Account, List<ObjectId>>> accounts = state.getAccounts().stream()
                .map((pairAccountUser) -> new Account(pairAccountUser.getLeft(), pairAccountUser.getRight()))
                .collect(Collectors.toUnmodifiableMap(Account::getId, (acc) -> new Pair<>(acc, new LinkedList<>())));  

            Iterator<LedgerOperation> operationsIt = operations.iterator();
            Iterator<LedgerOperationDAO> operationsDAOIt = operationsDAO.iterator();

            List<WriteModel<Nonces>> noncesOps = new LinkedList<>();

            for (int i = 0; i < operationsDAO.size(); i++)
            {
                LedgerOperation operation = operationsIt.next();
                LedgerOperationDAO operationDAO = operationsDAOIt.next();

                Pair<Account, List<ObjectId>> pairAccountOpsList;

                switch (operation.getType()) {
                    case DEPOSIT:
                        LedgerDeposit deposit = (LedgerDeposit) operation;
                        pairAccountOpsList = accounts.get(deposit.getAccountId());
                        pairAccountOpsList.getLeft().processOperation(deposit);
                        pairAccountOpsList.getRight().add(operationDAO.getId());
                        break;

                    case TRANSACTION:
                        LedgerTransaction transaction = (LedgerTransaction) operation;
                    
                        pairAccountOpsList = accounts.get(transaction.getOrigin());
                        pairAccountOpsList.getLeft().processOperation(transaction);
                        pairAccountOpsList.getRight().add(operationDAO.getId());

                        pairAccountOpsList = accounts.get(transaction.getDest());
                        pairAccountOpsList.getLeft().processOperation(transaction);
                        pairAccountOpsList.getRight().add(operationDAO.getId());

                        noncesOps.add(addNonceInBulkWrite(transaction.digest(), transaction.getNonce()));
                    break;
                }
            }

            List<WriteModel<AccountDAO>> accountsDAOmodel = accounts.values().stream()
                .map((pairAccountListOps) -> {
                    AccountDAO accountDAO = new AccountDAO(pairAccountListOps.getLeft());
                    accountDAO.setOperations(pairAccountListOps.getRight());
                    return accountDAO;
                })
                .map((accountDAO) -> (WriteModel<AccountDAO>) new InsertOneModel<>(accountDAO)).toList();

            this.accounts.bulkWrite(accountsDAOmodel);
            this.nonces.bulkWrite(noncesOps);

            return Result.ok();
            
        } catch (Exception e) {
            return Result.error(new InternalServerErrorException(e.getMessage(), e));
        }
    }

    protected List<LedgerOperationDAO> loadOperations(LedgerState state)
    {
        List<LedgerOperationDAO> insertOperations = state.getOperations().stream()
                .<LedgerOperationDAO>map(this::toDAO).toList();

        InsertManyResult insertManyResult = this.ledger
                .insertMany(insertOperations, new InsertManyOptions().ordered(true));

        Map<Integer, BsonValue> insertedIds = insertManyResult.getInsertedIds();

        Iterator<LedgerOperationDAO> operationsIt = insertOperations.iterator();
        for (int i = 0; i < insertOperations.size(); i++)
            operationsIt.next().setId(insertedIds.get(i).asObjectId().getValue());

        return insertOperations;
    }

    protected UpdateOneModel<Nonces> addNonceInBulkWrite(byte[] digest, int nonce)
    {
        UpdateOptions options = new UpdateOptions();
        options.upsert(true);

        Bson filter = eq("left", digest);

        return new UpdateOneModel<>(filter, Updates.addToSet("right", nonce), options);
    }


    @Override
    public Result<LedgerState> getState() {
        init();
        
        SortedMap<AccountId, UserId> sortedAccounts = new TreeMap<>();
        List<LedgerOperation> resOperations = new LinkedList<>();

        for (AccountDAO account : this.accounts.find()) {
            sortedAccounts.put(account.getAccountId(), account.getOwner());
        }

        List<Pair<AccountId, UserId>> resAccounts = sortedAccounts.entrySet().stream()
            .map((entry) -> new Pair<>(entry.getKey(), entry.getValue()))
            .toList();
        
        AggregateIterable<LedgerOperationDAO> ledgerIt = this.ledger.aggregate(Arrays.asList(
		    Aggregates.sort(Sorts.ascending("ts"))
			));

        if (ledgerIt.first() != null)
        {
            for (LedgerOperationDAO operation : ledgerIt) {
                resOperations.add(operation.toLedgerOperation());
            }
        }

        return Result.ok(new LedgerState(resAccounts, resOperations));
    }

    protected boolean checkAccountExists(AccountId accountId)
    {
        AggregateIterable<AccountDAO> result = this.accounts.aggregate(Arrays.asList(
			Aggregates.match(eq("accountId", accountId)),
            Aggregates.project(Projections.include("accountId"))
			));

        return result.first() != null;
    }
    
    protected LedgerOperationDAO toDAO(LedgerOperation operation)
    {
        switch (operation.getType()) {
            case DEPOSIT:
                return toDAO((LedgerDeposit)operation);
            case TRANSACTION:
                return toDAO((LedgerTransaction)operation);
            default:
                return null;
        }
    }

    protected LedgerDepositDAO toDAO(LedgerDeposit deposit)
    {
        try {
            return new LedgerDepositDAO(deposit);
        } catch (InvalidOperationException e) {
            throw new InternalServerErrorException(e.getMessage(), e);
        }
    }

    protected LedgerTransactionDAO toDAO(LedgerTransaction transaction)
    {
        try {
            return new LedgerTransactionDAO(transaction);
        } catch (InvalidOperationException e) {
            throw new InternalServerErrorException(e.getMessage(), e);
        }
    }

    
    protected static class Nonces extends Pair<byte[], List<Integer>> {}

}
