package itdlp.tp1.tests;

import java.net.URI;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import itdlp.tp1.api.AccountId;
import itdlp.tp1.api.UserId;
import itdlp.tp1.impl.client.LedgerClient;
import itdlp.tp1.util.Crypto;
import itdlp.tp1.util.Result;
import jakarta.ws.rs.core.Response.Status;

public class Workload implements Runnable
{
    public static enum Operation
    {
        CREATE_ACCOUNT,
        LOAD_MONEY,
        SEND_TRANSACTION,

        GET_ACCOUNT,
        GET_BALANCE,
        GET_TOTAL_VALUE,
        GET_GLOBAL_LEDGER_VALUE,
        GET_LEDGER
    }

    private static interface Request<T>
    {
        Result<T> request();
    }

    // statistics
    private Map<Operation, List<Long>> latencies;
    private Map<Operation, Map<Status, Integer>> statusCodes;
    private int nUsers, nAccounts;

    // state
    private Map<UserId, KeyPair> users;
    private Map<UserId, Map<AccountId, KeyPair>> accounts;

    private SecureRandom random;

    private URI endpoint;

    public Workload(URI endpoint, int nUsers, int nAccounts)
    {
        this.latencies = Stream.of(Operation.values())
            .collect(Collectors.toUnmodifiableMap((op) -> op, (op) -> new LinkedList<>()));
        
        this.statusCodes = Stream.of(Operation.values())
            .collect(Collectors.toUnmodifiableMap((op) -> op, (op) -> new HashMap<>()));

        this.nUsers = nUsers;
        this.nAccounts = nAccounts;

        this.random = new SecureRandom();

        this.endpoint = endpoint;
    }

    public static void main(String[] args)
    {
        URI endpoint = URI.create(args[0]);
        int nUsers = Integer.parseInt(args[1]);
        int nAccounts = Integer.parseInt(args[2]);

        Workload workload = new Workload(endpoint, nUsers, nAccounts);
        workload.run();

        System.out.println("Latencies:");
        System.out.println(workload.latencies);
        System.out.println();

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
            LedgerClient client = new LedgerClient(this.endpoint);
        )
        {
            createAccounts(client);
        }
    }

    private void createAccounts(LedgerClient client)
    {
        // send create account requests
        for (Entry<UserId, Map<AccountId, KeyPair>> entry : this.accounts.entrySet())
        {
            UserId userId = entry.getKey();
            KeyPair userKeys = this.users.get(userId);

            for (AccountId accountId : entry.getValue().keySet())
                request(() -> client.createAccount(accountId, userId, userKeys),
                    Operation.CREATE_ACCOUNT);
        }
    }

    private <T> Result<T> request(Request<T> request, Operation operation)
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
                (   (keyPair) -> new AccountId("workload.test@test.com", keyPair.getPublic() ),
                    (keyPair) -> keyPair)
            );
    }

    //#endregion
    
}
