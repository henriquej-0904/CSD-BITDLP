package tp2.bitdlp.tests;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.security.KeyPair;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.ws.rs.core.Response.Status;
import tp2.bitdlp.api.AccountId;
import tp2.bitdlp.api.UserId;
import tp2.bitdlp.impl.client.InvalidReplyWithSignaturesException;
import tp2.bitdlp.impl.client.InvalidServerSignatureException;
import tp2.bitdlp.impl.client.LedgerClientBFT;
import tp2.bitdlp.pow.block.BCBlock;
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

    protected int perReads, perWrites;

    public WorkloadBFT(String replicaId, URI endpoint, int nUsers, int nAccounts,
        int fReplicas, int perReads) throws MalformedURLException
    {
        super(replicaId, endpoint, nUsers, nAccounts);
        this.fReplicas = fReplicas;
        this.mineStats = new LatencyThroughputCalc();
        this.perReads = perReads;
        this.perWrites = 100 - perReads;
    }

    public static void main(String[] args) throws MalformedURLException
    {
        if (args.length < 4)
        {
            System.err.println("Usage: <endpoint> <replicaID> <fReplicas> <% reads>");
            System.exit(1);
        }

        URI endpoint = URI.create(args[0]);
        String replicaId = args[1];
        int nUsers = 2;
        int nAccounts = 1000;
        int fReplicas = Integer.parseInt(args[2]);
        int perReads = Integer.parseInt(args[3]);

        WorkloadBFT workload = new WorkloadBFT(replicaId, endpoint, nUsers, nAccounts, fReplicas, perReads);
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

            // writes
            int nWrites = (int)((double)5 * ((double)perWrites / (double)100));
            for (int i = 0; i < nWrites; i++)
            {
                // writes
                mineBlocksAndSendTransactions(client);
            }

            int nReads = 5 - nWrites;
            for (int i = 0; i < nReads; i++)
            {
                // reads
                getAccounts(client);
                getBalanceBFT(client);

                getTotalValue(client);
                getGlobalLedgerValue(client);
                getLedger(client);
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
                for (Entry<AccountId, KeyPair> account : m.entrySet()) {
                    if (account.getKey().equals(this.miner.getKey()))
                        continue;
                    
                    requestBFTOp(() -> client.sendTransactionBFT(this.miner.getKey(), account.getKey(),
                    1, this.miner.getValue()),
                    tp2.bitdlp.impl.srv.resources.requests.Request.Operation.SEND_TRANSACTION_ASYNC);             
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
    
}
