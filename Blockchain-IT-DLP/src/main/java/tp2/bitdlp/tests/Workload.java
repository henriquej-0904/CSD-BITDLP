package tp2.bitdlp.tests;

import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.net.ssl.SSLContext;

import tp2.bitdlp.api.AccountId;
import tp2.bitdlp.api.UserId;
import tp2.bitdlp.impl.client.InvalidServerSignatureException;
import tp2.bitdlp.impl.client.LedgerClient;
import tp2.bitdlp.impl.srv.resources.requests.Request.Operation;
import tp2.bitdlp.pow.block.BCBlock;
import tp2.bitdlp.util.Crypto;
import tp2.bitdlp.util.result.Result;
import jakarta.ws.rs.core.Response.Status;

public class Workload implements Runnable
{
    protected static interface Request<T>
    {
        Result<T> request() throws InvalidServerSignatureException;
    }

    // statistics
    protected Map<tp2.bitdlp.impl.srv.resources.requests.Request.Operation, LatencyThroughputCalc> stats;
    protected Map<tp2.bitdlp.impl.srv.resources.requests.Request.Operation, Map<Status, Integer>> statusCodes;
    protected int nUsers, nAccounts;

    // state
    protected Map<UserId, KeyPair> users;
    protected Map<UserId, Map<AccountId, KeyPair>> accounts;

    protected SecureRandom random;

    protected URI endpoint;
    protected boolean isHttps;

    protected String replicaId;

    protected Entry<AccountId, KeyPair> miner;

    protected LatencyThroughputCalc mineStats;

    protected boolean executeReads;

    public Workload(String replicaId, URI endpoint, int nUsers, int nAccounts,
        boolean executeReads) throws MalformedURLException
    {
        this.stats = new HashMap<>();
        
        this.statusCodes = Stream.of(tp2.bitdlp.impl.srv.resources.requests.Request.Operation.values())
            .collect(Collectors.toUnmodifiableMap((op) -> op, (op) -> new HashMap<>()));

        this.nUsers = nUsers;
        this.nAccounts = nAccounts;

        this.random = new SecureRandom();

        this.endpoint = endpoint;
        this.isHttps = this.endpoint.toURL().getProtocol().equals("https");
        this.replicaId = replicaId;

        this.mineStats = new LatencyThroughputCalc();
        this.executeReads = executeReads;
    }

    protected static Properties loadConfig(File configFile)
    {
        try (FileInputStream input = new FileInputStream(configFile)) {
            Properties config = new Properties();
            config.load(input);
            return config;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }

    public static void main(String[] args) throws  MalformedURLException
    {
        if (args.length < 2)
        {
            System.err.println("Usage: <workload-config> <replicaId> <OPTIONAL (defaults to false) - execute reads: true or false>");
            System.exit(1);
        }

        File configFile = new File(args[0]);
        String replicaId = args[1];
        boolean executeReads = args.length == 3 && Boolean.parseBoolean(args[2].toLowerCase());

        Properties config = loadConfig(configFile);

        int nUsers = Integer.parseInt(config.getProperty("N_USERS"));
        int nAccounts = Integer.parseInt(config.getProperty("N_ACCOUNTS"));

        URI endpoint;
        try {
            endpoint = new URI(config.getProperty(replicaId));
        } catch (Exception e) {
            System.err.println("Cannot read replica URI.");
            System.exit(1);
            endpoint = null;
        }

        Workload workload = new Workload(replicaId, endpoint, nUsers, nAccounts, executeReads);
        workload.run();

        System.out.println("Status Codes:");
        System.out.println(workload.statusCodes);
        System.out.println();

        System.out.println("Stats:");
        System.out.println(workload.stats);
        System.out.println();
        System.out.println("Mining stats: " + workload.mineStats);
    }

    @Override
    public void run()
    {
        init();

        try
        (
            LedgerClient client =
                new LedgerClient(replicaId, endpoint, random, getSSLContext())
        )
        {
            createAccounts(client);

            // choose an account to be the miner.
            this.miner = this.accounts.values().iterator().next().entrySet().iterator().next();

            AtomicBoolean stopReads = new AtomicBoolean(false);
            Thread readsThread = new Thread(() ->
                {
                    while(!stopReads.get())
                    {
                        // reads
                        if (random.nextBoolean())
                            getAccounts(client);
                        else
                            getBalance(client);
                        
                        getTotalValue(client);
                        getGlobalLedgerValue(client);
                        getLedger(client);
                    }
                });

            if (executeReads)
                readsThread.start();

            for (int i = 0; i < 2; i++)
            {
                // writes
                mineBlocksAndSendTransactions(client);
            }

            if (executeReads)
            {
                stopReads.set(true);
                readsThread.join();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    protected static SSLContext getSSLContext()
	{
		File configFolder = new File("tls-config");

		KeyStore truststore = Crypto.getKeyStorePkcs12(new File(configFolder, "truststore.pkcs12"), Crypto.KEYSTORE_PWD);
		return Crypto.getSSLContext(null, truststore, Crypto.KEYSTORE_PWD);
	}

    protected void createAccounts(LedgerClient client) throws InvalidServerSignatureException
    {
        // send create account requests
        for (Entry<UserId, Map<AccountId, KeyPair>> entry : this.accounts.entrySet())
        {
            UserId userId = entry.getKey();
            KeyPair userKeys = this.users.get(userId);

            for (AccountId accountId : entry.getValue().keySet())
                request(() -> client.createAccount(accountId, userId, userKeys),
                    tp2.bitdlp.impl.srv.resources.requests.Request.Operation.CREATE_ACCOUNT);
        }

        // expected to fail!!!
        Entry<UserId, Map<AccountId, KeyPair>> entry = this.accounts.entrySet().iterator().next();
        UserId userId = entry.getKey();
        KeyPair userKeys = this.users.get(userId);

        for (AccountId accountId : entry.getValue().keySet())
            request(() -> client.createAccount(accountId, userId, userKeys),
                tp2.bitdlp.impl.srv.resources.requests.Request.Operation.CREATE_ACCOUNT);
    }

    
    protected void getAccounts(LedgerClient client) throws InvalidServerSignatureException
    {
        // send get account requests
        for (Entry<UserId, Map<AccountId, KeyPair>> entry : this.accounts.entrySet())
        {
            for (AccountId accountId : entry.getValue().keySet())
                client.getAccount(accountId);
        }

        // expected to fail with 404
        byte[] randomBytes = new byte[60];
        for (int i = 0; i < 10; i++)
        {
            this.random.nextBytes(randomBytes);
            AccountId accountId = new AccountId(randomBytes);
            client.getAccount(accountId);
        }
        
    }

    protected void getBalance(LedgerClient client) throws InvalidServerSignatureException
    {
        // send get balance requests
        for (Entry<UserId, Map<AccountId, KeyPair>> entry : this.accounts.entrySet())
        {
            for (AccountId accountId : entry.getValue().keySet())
                client.getBalance(accountId);
        }
    }

    protected void getTotalValue(LedgerClient client) throws InvalidServerSignatureException
    {
        // send create account requests
        for (Entry<UserId, Map<AccountId, KeyPair>> entry : this.accounts.entrySet())
            client.getTotalValue( entry.getValue().keySet().toArray(new AccountId[0]));
    }

    protected void getGlobalLedgerValue(LedgerClient client) throws InvalidServerSignatureException
    {
        // send getGlovalLedgerValue requests
        client.getGlobalLedgerValue();
    }

    protected void sendTransaction(LedgerClient client) throws InvalidServerSignatureException
    {
        // sendTransaction requests

        Iterator<Map<AccountId, KeyPair>> accountsIt = this.accounts.values().iterator();
        Map<AccountId, KeyPair> accounts1 = accountsIt.next();
        Map<AccountId, KeyPair> accounts2 = accountsIt.next();

        for (Entry<AccountId, KeyPair> originAccount : accounts1.entrySet()) {
            
            for (AccountId destAccountId : accounts2.keySet()) {
                int nonce = this.random.nextInt();

                request(() -> client.sendTransaction(originAccount.getKey(), destAccountId,
                    55, originAccount.getValue(), nonce),
                    tp2.bitdlp.impl.srv.resources.requests.Request.Operation.SEND_TRANSACTION);

                // expected to fail with 403
                request(() -> client.sendTransaction(originAccount.getKey(), destAccountId,
                    55, originAccount.getValue(), nonce),
                    tp2.bitdlp.impl.srv.resources.requests.Request.Operation.SEND_TRANSACTION);
            }
        }
    }

    protected void getLedger(LedgerClient client) throws InvalidServerSignatureException
    {
        client.getLedger();
    }

    protected boolean proposeBlock(LedgerClient client, BCBlock block) throws InvalidServerSignatureException
    {
        var res = request(() -> client.proposeMinedBlock(this.miner.getKey(), block, this.miner.getValue()),
                tp2.bitdlp.impl.srv.resources.requests.Request.Operation.PROPOSE_BLOCK);
        
        if (res.isOK())
            return true;
        else
        {
            System.err.println(res.errorException().getMessage());
            return false;
        }
    }

    protected void mineBlocksAndSendTransactions(LedgerClient client)
    {
        while (mineBlock(client))
            {
                /* try {
                    System.out.println("Continue... Press!");
                    System.in.read();
                    
                } catch (IOException e) {
                    e.printStackTrace();
                } */
            };

            // send transactions

            for ( Map<AccountId, KeyPair> m : this.accounts.values())
            {
                for (Entry<AccountId, KeyPair> account : m.entrySet())
                {
                    if (account.getKey().equals(this.miner.getKey()))
                        continue;
                    
                    request(() -> client.sendTransaction(this.miner.getKey(), account.getKey(),
                        1, this.miner.getValue()),
                        tp2.bitdlp.impl.srv.resources.requests.Request.Operation.SEND_TRANSACTION);             
                }
            }

            while (mineBlock(client))
            {
                /* try {
                    System.out.println("Continue... Press!");
                    System.in.read();
                    
                } catch (IOException e) {
                    e.printStackTrace();
                } */
            };
    }

    /**
     * Mine a block.
     * @param client
     * @return true if there was a block to mine.
     */
    protected boolean mineBlock(LedgerClient client)
    {
        // get block
        Result<BCBlock> result = client.getBlockToMine(this.miner.getKey());
        if (!result.isOK())
        {
            System.out.println("There are no blocks to mine!");
            return false;
        }

        BCBlock block = result.value();

        // mine block
        System.err.println("Mining block...");

        long t1 = System.currentTimeMillis();

        /* try {
            Thread.sleep(this.random.nextLong(20000, 60000));
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } */

        while (!block.isBlockMined())
            block.getHeader().setNonce(this.random.nextInt());

        long t2 = System.currentTimeMillis();

        long time = (t2 - t1);

        this.mineStats.addLatency(time);

        System.err.println("Mined block in " + time + "ms");

        // propose block
        if (proposeBlock(client, block))
        {
            System.err.println("Block accepted and added to the blockchain!");

            Result<Integer> balanceRes = client.getBalance(this.miner.getKey());

            if (balanceRes.isOK())
            {
                System.err.println("Current miner balance: " + balanceRes.value());
            }

            return true;
        }
        else
        {
            System.err.println("The block was invalid \\:");
            return true;
        }
    }

    protected <T> Result<T> request(Request<T> request, tp2.bitdlp.impl.srv.resources.requests.Request.Operation operation) throws InvalidServerSignatureException
    {
        long before = System.currentTimeMillis();
        Result<T> result = request.request();
        long after = System.currentTimeMillis();

        getStats(operation).addLatency(after - before);
        
        Map<Status, Integer> statusCodes = this.statusCodes.get(operation);
        Status status = Status.fromStatusCode(result.error());
        Integer statusCodeCounter = statusCodes.get(status);

        if (statusCodeCounter == null)
            statusCodeCounter = 0;

        statusCodeCounter++;
        statusCodes.put(status, statusCodeCounter);

        return result;
    }

    protected LatencyThroughputCalc getStats(Operation operation)
    {
        return this.stats.computeIfAbsent(operation, (op) -> new LatencyThroughputCalc());
    }

    //#region random users & accounts

    protected void init()
    {
        // create users & accounts in memory
        this.users = createUsers(this.random, this.nUsers);

        this.accounts = this.users.keySet().stream()
            .collect(Collectors.toUnmodifiableMap
            (   (user) -> user,
                (user) -> createAccounts(this.random, this.nAccounts / this.nUsers))
            );
    }

    protected static Stream<KeyPair> randomKeyPairStream(SecureRandom random)
    {
        return Stream.generate(() -> Crypto.createKeyPairForEcc256bits(random));
    }

    protected static Map<UserId, KeyPair> createUsers(SecureRandom random, int n)
    {
        return randomKeyPairStream(random)
            .limit(n)
            .collect(Collectors.toUnmodifiableMap
                (   (keyPair) -> new UserId("workload.test@test.com", keyPair.getPublic() ),
                    (keyPair) -> keyPair)
            );
    }

    protected static Map<AccountId, KeyPair> createAccounts(SecureRandom random, int n)
    {
        return randomKeyPairStream(random)
            .limit(n)
            .collect(Collectors.toUnmodifiableMap
                (   (keyPair) -> new AccountId("workload.test@test.com", keyPair.getPublic(), random ),
                    (keyPair) -> keyPair)
            );
    }

    //#endregion
    
}
