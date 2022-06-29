package tp2.bitdlp.data.mongo;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

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
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

import tp2.bitdlp.api.Account;
import tp2.bitdlp.api.AccountId;
import tp2.bitdlp.api.UserId;
import tp2.bitdlp.data.LedgerDBlayer;
import tp2.bitdlp.data.LedgerState;
import tp2.bitdlp.pow.block.BCBlock;
import tp2.bitdlp.pow.transaction.LedgerTransaction;
import tp2.bitdlp.util.Pair;
import tp2.bitdlp.util.Utils;
import tp2.bitdlp.util.result.Result;
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
    private MongoCollection<BCBlock> ledger;
    private MongoCollection<Nonce> nonces;

    private boolean isLedgerEmpty;
    private String previousBlockHash;


    /**
     * @param client
     */
    public LedgerDBWithMongo(MongoClient client) {
        this.client = client;
        this.isLedgerEmpty = true;
        this.previousBlockHash = null;
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
        this.ledger = this.db.getCollection("Ledger", BCBlock.class);
        this.nonces = this.db.getCollection("Nonce", Nonce.class);

        // Create indexes for id field
        IndexOptions indexOptions = new IndexOptions().unique(true);

        this.accounts.createIndex(Indexes.ascending("accountId"), indexOptions);
        this.nonces.createIndex(Indexes.ascending("left", "right"), indexOptions);
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

            return Result.ok(accountDAO.toAccount());
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

    /* @Override
    public Result<Void> sendTransaction(LedgerTransaction transaction) {
        init();

        try {
            // add nonce
            this.nonces.insertOne(new Nonce(transaction.digest(), transaction.getNonce()));
        } catch (Exception e) {
            return Result.error(new ForbiddenException("Invalid nonce."));
        }

        try {
            // apply operation
            if (!checkAccountExists(transaction.getOrigin()))
                return accountNotFound(transaction.getOrigin());

            if (!checkAccountExists(transaction.getDest()))
                return accountNotFound(transaction.getDest());

            UpdateOptions options = new UpdateOptions();
            options.upsert(false);

            Bson originFilter = and(
                    eq("accountId", transaction.getOrigin()),
                    gte("balance", transaction.getValue()));

            Bson destFilter = eq("accountId", transaction.getDest());

            UpdateResult result = this.accounts.updateOne(originFilter,
                Updates.inc("balance", -transaction.getValue()), options);
            
            if (result.getMatchedCount() == 0)
                return Result.error(new WebApplicationException("Not enough money.", Status.CONFLICT));

            this.accounts.updateOne(destFilter, Updates.inc("balance", transaction.getValue()), options);
            
            // add operation to ledger
            ObjectId operationId = this.ledger.insertOne(toDAO(transaction)).getInsertedId().asObjectId()
                    .getValue();

            Bson filter = or(
                    eq("accountId", transaction.getOrigin()),
                    eq("accountId", transaction.getDest()));

            this.accounts.updateMany(filter,
                    Updates.push("operations", operationId), options);

            return Result.ok();

        } catch (Exception e) {
            return Result.error(new InternalServerErrorException(e.getMessage(), e));
        }
    } */

    @Override
    public Result<BCBlock[]> getLedger() {
        init();

        AggregateIterable<BCBlock> result = this.ledger.aggregate(Arrays.asList(
			Aggregates.sort(Sorts.ascending("ts"))
			));

        if (result.first() == null)
            return Result.error(new NotFoundException("The ledger is empty."));

        List<BCBlock> list = new LinkedList<>();

        for (BCBlock block : result) {
            list.add(block);
        }

        return Result.ok(list.toArray(new BCBlock[0]));
    }

    @Override
    public Result<Void> loadState(LedgerState state) {
        init();

        try {
            dropCollections();
            createCollections();

            this.ledger.insertMany(state.getLedger());

            Map<AccountId, Account> accounts = state.getAccounts().stream()
                .map((pairAccountUser) -> new Account(pairAccountUser.getLeft(), pairAccountUser.getRight()))
                .collect(Collectors.toMap(Account::getId, (acc) -> acc, (acc1, acc2) -> acc1, () -> new TreeMap<>()));

            this.accounts.insertMany(accounts.values().stream()
                .map((t) -> new AccountDAO(t))
                .collect(Collectors.toList()));

            for (BCBlock block : state.getLedger())
                processBlockTransactions(block.getTransactions().getTransactions());

            this.isLedgerEmpty = state.getLedger().isEmpty();

            this.previousBlockHash = this.isLedgerEmpty ? null :
                Utils.toHex(state.getLedger().get(state.getLedger().size() - 1).digest());

            return Result.ok();
            
        } catch (Exception e) {
            return Result.error(new InternalServerErrorException(e.getMessage(), e));
        }
    }


    @Override
    public Result<LedgerState> getState() {
        init();
        
        SortedMap<AccountId, UserId> sortedAccounts = new TreeMap<>();

        for (AccountDAO account : this.accounts.find()) {
            sortedAccounts.put(account.getAccountId(), account.getOwner());
        }

        List<Pair<AccountId, UserId>> resAccounts = sortedAccounts.entrySet().stream()
            .map((entry) -> new Pair<>(entry.getKey(), entry.getValue()))
            .toList();
        
        AggregateIterable<BCBlock> ledgerIt = this.ledger.aggregate(Arrays.asList(
		    Aggregates.sort(Sorts.ascending("ts"))
			));

        List<BCBlock> blockchain = new LinkedList<>();
        ledgerIt.into(blockchain);

        return Result.ok(new LedgerState(resAccounts, blockchain));
    }

    protected boolean checkAccountExists(AccountId accountId)
    {
        AggregateIterable<AccountDAO> result = this.accounts.aggregate(Arrays.asList(
			Aggregates.match(eq("accountId", accountId)),
            Aggregates.project(Projections.include("accountId"))
			));

        return result.first() != null;
    }

    @Override
    public Result<String> addBlock(BCBlock block) {
        // Add block to ledger
        // update aux data structures.

        try
        {
            this.ledger.insertOne(block);

            this.isLedgerEmpty = false;
            this.previousBlockHash = Utils.toHex(block.digest());

            processBlockTransactions(block.getTransactions().getTransactions());

            return Result.ok(this.previousBlockHash);
            
        } catch (Exception e){
            return Result.error(new InternalServerErrorException(e.getMessage(), e));
        }
    }

    @Override
    public Result<Void> verifySendTransaction(LedgerTransaction transaction) {
        Result<Integer> originBalance = getBalance(transaction.getOrigin());
        Result<Integer> destBalance = getBalance(transaction.getDest());

        if (!originBalance.isOK())
            return Result.error(originBalance.errorException());
        if (!destBalance.isOK())
            return Result.error(destBalance.errorException());

        if (!verifyNonce(transaction.digest(), transaction.getNonce()))
            return Result.error(new ForbiddenException("Invalid Nonce."));

        // verify balance
        if (originBalance.value() - transaction.getValue() < 0)
            return Result.error(new WebApplicationException("Transaction over the balance limit.", Status.CONFLICT));
        
        return Result.ok();
    }

    @Override
    public synchronized Result<Boolean> emptyBlockchain() {
        return Result.ok(this.isLedgerEmpty);  
    }

    @Override
    public Result<String> getPreviousBlockHash()
    {
        if (this.previousBlockHash == null)
                    return Result.error(new NotFoundException("The ledger is empty"));
                else
                    return Result.ok(this.previousBlockHash);
    }

    /**
     * Process the transactions of a block.
     * The first transaction is a generation transaction.
     * @param transactions
     */
    protected void processBlockTransactions(List<LedgerTransaction> transactions)
    {
        if (transactions.isEmpty())
            return;

        UpdateOptions options = new UpdateOptions();
        options.upsert(false);
        
        Iterator<LedgerTransaction> it = transactions.iterator();

        LedgerTransaction transaction = it.next();

        // process generation transaction
        Bson originFilter;
        Bson destFilter = eq("accountId", transaction.getDest());
        this.accounts.updateOne(destFilter,
            Updates.combine(
                Updates.inc("balance", transaction.getValue()),
                Updates.push("operations", new LedgerTransactionDAO(transaction))
            ), options);

        // process the rest of the transactions.
        while (it.hasNext())
        {
            transaction = it.next();

            originFilter = eq("accountId", transaction.getOrigin());
            destFilter = eq("accountId", transaction.getDest());

            addNonce(transaction.getHash(), transaction.getNonce());

            this.accounts.updateOne(originFilter,
            Updates.combine(
                Updates.inc("balance", -transaction.getValue()),
                Updates.push("operations", new LedgerTransactionDAO(transaction))
            ), options);

            this.accounts.updateOne(destFilter,
            Updates.combine(
                Updates.inc("balance", transaction.getValue()),
                Updates.push("operations", new LedgerTransactionDAO(transaction))
            ), options);
        }
    }

    /**
     * Verifies if the specified nonce is valid.
     * 
     * @param digest
     * @param nonce
     * @return true if it is valid, false otherwise.
     */
    protected boolean verifyNonce(byte[] digest, int nonce)
    {
        return this.nonces.find(
            and(eq("left", digest), eq("right", nonce)))
            .first() == null;
    }

    protected void addNonce(byte[] digest, int nonce){
        // add nonce
        this.nonces.insertOne(new Nonce(digest, nonce));
    }

    protected static class Nonce extends Pair<byte[], Integer>
    {

        /**
         * @param left
         * @param right
         */
        public Nonce(byte[] left, Integer right) {
            super(left, right);
        }
        
    }

}
