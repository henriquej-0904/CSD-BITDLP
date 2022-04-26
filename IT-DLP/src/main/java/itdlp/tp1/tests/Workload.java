package itdlp.tp1.tests;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.net.ssl.SSLContext;

import itdlp.tp1.api.AccountId;
import itdlp.tp1.api.UserId;
import itdlp.tp1.impl.client.InvalidServerSignatureException;
import itdlp.tp1.impl.client.LedgerClient;
import itdlp.tp1.util.Crypto;
import itdlp.tp1.util.Result;
import jakarta.ws.rs.core.Response.Status;

public class Workload implements Runnable
{
    private static interface Request<T>
    {
        Result<T> request() throws InvalidServerSignatureException;
    }

    // statistics
    private Map<itdlp.tp1.impl.srv.resources.requests.Request.Operation, List<Long>> latencies;
    private Map<itdlp.tp1.impl.srv.resources.requests.Request.Operation, Map<Status, Integer>> statusCodes;
    private int nUsers, nAccounts;

    // state
    private Map<UserId, KeyPair> users;
    private Map<UserId, Map<AccountId, KeyPair>> accounts;

    private SecureRandom random;

    private URI endpoint;
    private boolean isHttps;

    private String replicaId;

    public Workload(String replicaId, URI endpoint, int nUsers, int nAccounts) throws MalformedURLException
    {
        this.latencies = Stream.of(itdlp.tp1.impl.srv.resources.requests.Request.Operation.values())
            .collect(Collectors.toUnmodifiableMap((op) -> op, (op) -> new LinkedList<>()));
        
        this.statusCodes = Stream.of(itdlp.tp1.impl.srv.resources.requests.Request.Operation.values())
            .collect(Collectors.toUnmodifiableMap((op) -> op, (op) -> new HashMap<>()));

        this.nUsers = nUsers;
        this.nAccounts = nAccounts;

        this.random = new SecureRandom();

        this.endpoint = endpoint;
        this.isHttps = this.endpoint.toURL().getProtocol().equals("https");
        this.replicaId = replicaId;
    }

    public static void main(String[] args) throws MalformedURLException
    {
        URI endpoint = URI.create(args[0]);
        String replicaId = args[1];
        int nUsers = Integer.parseInt(args[2]);
        int nAccounts = Integer.parseInt(args[3]);

        Workload workload = new Workload(replicaId, endpoint, nUsers, nAccounts);
        workload.run();

        /* System.out.println("Latencies:");
        System.out.println(workload.latencies);
        System.out.println(); */

        System.out.println("Status Codes:");
        System.out.println(workload.statusCodes);
        System.out.println();
    }

    @Override
    public void run()
    {
        init();

        try
        (
            LedgerClient client = isHttps
                ? new LedgerClient(this.replicaId, this.endpoint, this.random, getSSLContext())
                : new LedgerClient(this.endpoint, this.random);
        )
        {
            createAccounts(client);
            loadMoney(client);
            getAccounts(client);
            sendTransaction(client);
            getBalance(client);
            getTotalValue(client);
            getGlobalLedgerValue(client);
            getLedger(client);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static SSLContext getSSLContext()
	{
		File configFolder = new File("tls-config");

		KeyStore truststore = Crypto.getKeyStorePkcs12(new File(configFolder, "truststore.pkcs12"), Crypto.KEYSTORE_PWD);
		return Crypto.getSSLContext(null, truststore, Crypto.KEYSTORE_PWD);
	}

    private void createAccounts(LedgerClient client) throws InvalidServerSignatureException
    {
        // send create account requests
        for (Entry<UserId, Map<AccountId, KeyPair>> entry : this.accounts.entrySet())
        {
            UserId userId = entry.getKey();
            KeyPair userKeys = this.users.get(userId);

            for (AccountId accountId : entry.getValue().keySet())
                request(() -> client.createAccount(accountId, userId, userKeys),
                    itdlp.tp1.impl.srv.resources.requests.Request.Operation.CREATE_ACCOUNT);
        }

        // expected to fail!!!
        Entry<UserId, Map<AccountId, KeyPair>> entry = this.accounts.entrySet().iterator().next();
        UserId userId = entry.getKey();
        KeyPair userKeys = this.users.get(userId);

        for (AccountId accountId : entry.getValue().keySet())
            request(() -> client.createAccount(accountId, userId, userKeys),
                itdlp.tp1.impl.srv.resources.requests.Request.Operation.CREATE_ACCOUNT);
    }

    
    private void getAccounts(LedgerClient client) throws InvalidServerSignatureException
    {
        // send get account requests
        for (Entry<UserId, Map<AccountId, KeyPair>> entry : this.accounts.entrySet())
        {
            for (AccountId accountId : entry.getValue().keySet())
                request(() -> client.getAccount(accountId),
                itdlp.tp1.impl.srv.resources.requests.Request.Operation.GET_ACCOUNT);
        }

        // expected to fail with 404
        byte[] randomBytes = new byte[60];
        for (int i = 0; i < 10; i++)
        {
            this.random.nextBytes(randomBytes);
            AccountId accountId = new AccountId(randomBytes);
            request(() -> client.getAccount(accountId),
                itdlp.tp1.impl.srv.resources.requests.Request.Operation.GET_ACCOUNT);
        }
        
    }

    private void getBalance(LedgerClient client) throws InvalidServerSignatureException
    {
        // send get balance requests
        for (Entry<UserId, Map<AccountId, KeyPair>> entry : this.accounts.entrySet())
        {
            for (AccountId accountId : entry.getValue().keySet())
                request(() -> client.getBalance(accountId),
                itdlp.tp1.impl.srv.resources.requests.Request.Operation.GET_BALANCE);
        }
    }

    private void getTotalValue(LedgerClient client) throws InvalidServerSignatureException
    {
        // send create account requests
        for (Entry<UserId, Map<AccountId, KeyPair>> entry : this.accounts.entrySet())
        {
            request(() -> client.getTotalValue( entry.getValue().keySet().toArray(new AccountId[0])),
            itdlp.tp1.impl.srv.resources.requests.Request.Operation.GET_TOTAL_VALUE);
        }
    }

    private void getGlobalLedgerValue(LedgerClient client) throws InvalidServerSignatureException
    {
        // send getGlovalLedgerValue requests
        request(() -> client.getGlobalLedgerValue(),
        itdlp.tp1.impl.srv.resources.requests.Request.Operation.GET_GLOBAL_LEDGER_VALUE);
        
    }

    private void loadMoney(LedgerClient client) throws InvalidServerSignatureException
    {
        // send loadMoney requests
        for (Map<AccountId, KeyPair> accounts : this.accounts.values())
        {
            for (Entry<AccountId, KeyPair> account : accounts.entrySet())
            {
                request(() -> client.loadMoney(account.getKey(), 100, account.getValue()),
                itdlp.tp1.impl.srv.resources.requests.Request.Operation.LOAD_MONEY);
            }
        }
    }

    private void sendTransaction(LedgerClient client) throws InvalidServerSignatureException
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
                    itdlp.tp1.impl.srv.resources.requests.Request.Operation.SEND_TRANSACTION);

                // expected to fail with 403
                request(() -> client.sendTransaction(originAccount.getKey(), destAccountId,
                    55, originAccount.getValue(), nonce),
                    itdlp.tp1.impl.srv.resources.requests.Request.Operation.SEND_TRANSACTION);
            }
        }
    }

    private void getLedger(LedgerClient client) throws InvalidServerSignatureException
    {
        request(() -> client.getLedger(), itdlp.tp1.impl.srv.resources.requests.Request.Operation.GET_LEDGER);
    }

    private <T> Result<T> request(Request<T> request, itdlp.tp1.impl.srv.resources.requests.Request.Operation operation) throws InvalidServerSignatureException
    {
        long before = System.currentTimeMillis();
        Result<T> result = request.request();
        long after = System.currentTimeMillis();

        this.latencies.get(operation).add(after - before);
        
        Map<Status, Integer> statusCodes = this.statusCodes.get(operation);
        Status status = Status.fromStatusCode(result.error());
        Integer statusCodeCounter = statusCodes.get(status);

        if (statusCodeCounter == null)
            statusCodeCounter = 0;

        statusCodeCounter++;
        statusCodes.put(status, statusCodeCounter);

        return result;
    }

    //#region random users & accounts

    private void init()
    {
        // create users & accounts in memory
        this.users = createUsers(this.random, this.nUsers);

        this.accounts = this.users.keySet().stream()
            .collect(Collectors.toUnmodifiableMap
            (   (user) -> user,
                (user) -> createAccounts(this.random, this.nAccounts / this.nUsers))
            );
    }

    private static Stream<KeyPair> randomKeyPairStream(SecureRandom random)
    {
        return Stream.generate(() -> Crypto.createKeyPairForEcc256bits(random));
    }

    private static Map<UserId, KeyPair> createUsers(SecureRandom random, int n)
    {
        return randomKeyPairStream(random)
            .limit(n)
            .collect(Collectors.toUnmodifiableMap
                (   (keyPair) -> new UserId("workload.test@test.com", keyPair.getPublic() ),
                    (keyPair) -> keyPair)
            );
    }

    private static Map<AccountId, KeyPair> createAccounts(SecureRandom random, int n)
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
