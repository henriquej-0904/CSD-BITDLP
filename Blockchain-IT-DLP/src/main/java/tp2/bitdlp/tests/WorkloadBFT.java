package tp2.bitdlp.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.security.KeyPair;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.ws.rs.core.Response.Status;
import tp2.bitdlp.api.AccountId;
import tp2.bitdlp.api.UserId;
import tp2.bitdlp.impl.client.InvalidReplyWithSignaturesException;
import tp2.bitdlp.impl.client.InvalidServerSignatureException;
import tp2.bitdlp.impl.client.LedgerClientBFT;
import tp2.bitdlp.impl.srv.resources.requests.SendTransaction;
import tp2.bitdlp.impl.srv.resources.requests.SmartContractValidation;
import tp2.bitdlp.pow.block.BCBlock;
import tp2.bitdlp.pow.transaction.SmartContract;
import tp2.bitdlp.util.Pair;
import tp2.bitdlp.util.reply.ReplyWithSignatures;
import tp2.bitdlp.util.result.Result;

public class WorkloadBFT extends Workload
{
    protected static interface BFTRequest<T>
    {
        Pair<Result<T>, ReplyWithSignatures> request()
            throws InvalidServerSignatureException, InvalidReplyWithSignaturesException;
    }


    protected int fReplicas;

    protected Entry<AccountId, KeyPair> miner;

    protected LatencyThroughputCalc mineStats;

    protected Map<String, byte[]> smartContracts;

    protected boolean executeReads;

    public WorkloadBFT(String replicaId, URI endpoint, int nUsers, int nAccounts,
        int fReplicas, boolean executeReads) throws MalformedURLException
    {
        super(replicaId, endpoint, nUsers, nAccounts);
        this.fReplicas = fReplicas;
        this.mineStats = new LatencyThroughputCalc();
        this.smartContracts = loadSmartContracts();
        this.executeReads = executeReads;
    }

    protected static Map<String, byte[]> loadSmartContracts()
    {
        File folder = new File("smart-contracts");
        File[] files = folder.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".java");
            }
            
        });

        if (files == null)
        {
            System.err.println("There are no smart contracts to load");
            return null;
        }

        Map<String, byte[]> smartContracts = new HashMap<>();

        for (File file : files)
        {
            try {
                String fileName = file.getName();
                String name = fileName.substring(0, fileName.length() - ".java".length());
                smartContracts.put(name, Files.readAllBytes(file.toPath()));
            } catch (Exception e) {
                
            }
        }

        System.out.println("Loaded " + smartContracts.size() + " smart-contracts");
        return smartContracts;
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

        int fReplicas = Integer.parseInt(config.getProperty("NUM_F"));

        WorkloadBFT workload = new WorkloadBFT(replicaId, endpoint, nUsers, nAccounts, fReplicas, executeReads);
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
            LedgerClientBFT client =
                new LedgerClientBFT(replicaId, endpoint, random, getSSLContext(), fReplicas)
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
                            getBalanceBFT(client);
                        
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

    protected void getBalanceBFT(LedgerClientBFT client) throws InvalidServerSignatureException,
        InvalidReplyWithSignaturesException
    {
        // send get balance requests
        for (Entry<UserId, Map<AccountId, KeyPair>> entry : this.accounts.entrySet())
        {
            for (AccountId accountId : entry.getValue().keySet())
                requestBFTOp(() -> client.getBalanceBFT(accountId),
                tp2.bitdlp.impl.srv.resources.requests.Request.Operation.GET_BALANCE_ASYNC);
        }
    }

    protected void sendTransactionBFT(LedgerClientBFT client) throws InvalidServerSignatureException,
        InvalidReplyWithSignaturesException
    {
        // sendTransaction requests

        Iterator<Map<AccountId, KeyPair>> accountsIt = this.accounts.values().iterator();
        Map<AccountId, KeyPair> accounts1 = accountsIt.next();
        Map<AccountId, KeyPair> accounts2 = accountsIt.next();

        for (Entry<AccountId, KeyPair> originAccount : accounts1.entrySet()) {
            
            for (AccountId destAccountId : accounts2.keySet()) {
                int nonce = this.random.nextInt();

                requestBFTOp(() -> client.sendTransactionBFT(originAccount.getKey(), destAccountId,
                    5, originAccount.getValue(), nonce),
                    tp2.bitdlp.impl.srv.resources.requests.Request.Operation.SEND_TRANSACTION_ASYNC);

                // expected to fail with 403
                requestBFTOp(() -> client.sendTransactionBFT(originAccount.getKey(), destAccountId,
                    5, originAccount.getValue(), nonce),
                    tp2.bitdlp.impl.srv.resources.requests.Request.Operation.SEND_TRANSACTION_ASYNC);
            }
        }
    }

    protected boolean proposeBlockBFT(LedgerClientBFT client, BCBlock block) throws InvalidServerSignatureException,
        InvalidReplyWithSignaturesException
    {
        var res = requestBFTOp(() -> client.proposeMinedBlockBFT(this.miner.getKey(), block, this.miner.getValue()),
                tp2.bitdlp.impl.srv.resources.requests.Request.Operation.PROPOSE_BLOCK_ASYNC);
        
        if (res.isOK())
            return true;
        else
        {
            System.err.println(res.errorException().getMessage());
            return false;
        }
    }

    protected void mineBlocksAndSendTransactions(LedgerClientBFT client)
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
                    
                    if (!smartContracts.isEmpty() && random.nextDouble() < 0.33)
                    {
                        // send transaction with smart contract validation

                        SmartContractValidation param =
                            new SmartContractValidation("ValueMultiple5",
                            smartContracts.get("ValueMultiple5"),
                            new Pair<>(this.miner.getKey().getObjectId(), account.getKey().getObjectId()), 5, this.random.nextInt(), null);
                        
                        SmartContract smartContract = smartContractValidation(client, param, this.miner.getValue());
                        SendTransaction sendTransaction =
                            new SendTransaction(param.getOriginDestPair(), param.getValue(),
                            null, param.getNonce(), smartContract);
                        requestBFTOp(() -> client.sendTransactionBFT(sendTransaction, this.miner.getValue()),
                        tp2.bitdlp.impl.srv.resources.requests.Request.Operation.SEND_TRANSACTION_ASYNC);  
                    }
                    else
                    {
                        requestBFTOp(() -> client.sendTransactionBFT(this.miner.getKey(), account.getKey(),
                        1, this.miner.getValue()),
                        tp2.bitdlp.impl.srv.resources.requests.Request.Operation.SEND_TRANSACTION_ASYNC);  
                    }              
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
    protected boolean mineBlock(LedgerClientBFT client)
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
        if (proposeBlockBFT(client, block))
        {
            System.err.println("Block accepted and added to the blockchain!");

            Result<Integer> balanceRes = requestBFTOp(() -> client.getBalanceBFT(this.miner.getKey()),
                tp2.bitdlp.impl.srv.resources.requests.Request.Operation.GET_BALANCE_ASYNC);

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

    protected SmartContract smartContractValidation(LedgerClientBFT client,
        SmartContractValidation param, KeyPair keypair)
    {
        try {
            var result = 
                requestBFTOpWithSignatures(() -> client.smartContractValidationBFT(param, keypair),
                        tp2.bitdlp.impl.srv.resources.requests.Request.Operation.SMART_CONTRACT_VALIDATION_ASYNC);  
            return new SmartContract(param.getName(), param.getCode(), result.getRight().getSignatures());
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    protected <T> Result<T> requestBFTOp(BFTRequest<T> request, tp2.bitdlp.impl.srv.resources.requests.Request.Operation operation) throws InvalidServerSignatureException
    {
        long before = System.currentTimeMillis();
        Pair<Result<T>, ReplyWithSignatures> result = request.request();
        long after = System.currentTimeMillis();

        getStats(operation).addLatency(after - before);
        
        Map<Status, Integer> statusCodes = this.statusCodes.get(operation);
        Status status = Status.fromStatusCode(result.getLeft().error());
        Integer statusCodeCounter = statusCodes.get(status);

        if (statusCodeCounter == null)
            statusCodeCounter = 0;

        statusCodeCounter++;
        statusCodes.put(status, statusCodeCounter);

        return result.getLeft();
    }

    protected <T> Pair<Result<T>, ReplyWithSignatures> requestBFTOpWithSignatures(BFTRequest<T> request, tp2.bitdlp.impl.srv.resources.requests.Request.Operation operation) throws InvalidServerSignatureException
    {
        long before = System.currentTimeMillis();
        Pair<Result<T>, ReplyWithSignatures> result = request.request();
        long after = System.currentTimeMillis();

        getStats(operation).addLatency(after - before);
        
        Map<Status, Integer> statusCodes = this.statusCodes.get(operation);
        Status status = Status.fromStatusCode(result.getLeft().error());
        Integer statusCodeCounter = statusCodes.get(status);

        if (statusCodeCounter == null)
            statusCodeCounter = 0;

        statusCodeCounter++;
        statusCodes.put(status, statusCodeCounter);

        return result;
    }
    
}
