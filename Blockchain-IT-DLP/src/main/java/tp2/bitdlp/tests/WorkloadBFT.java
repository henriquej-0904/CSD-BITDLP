package tp2.bitdlp.tests;

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

    public WorkloadBFT(String replicaId, URI endpoint, int nUsers, int nAccounts,
        int fReplicas) throws MalformedURLException
    {
        super(replicaId, endpoint, nUsers, nAccounts);
        this.fReplicas = fReplicas;
    }

    public static void main(String[] args) throws MalformedURLException
    {
        if (args.length < 5)
        {
            System.err.println("Usage: <endpoint> <replicaID> <nUsers> <nAccounts> <fReplicas>");
            System.exit(1);
        }

        URI endpoint = URI.create(args[0]);
        String replicaId = args[1];
        int nUsers = Integer.parseInt(args[2]);
        int nAccounts = Integer.parseInt(args[3]);
        int fReplicas = Integer.parseInt(args[4]);

        WorkloadBFT workload = new WorkloadBFT(replicaId, endpoint, nUsers, nAccounts, fReplicas);
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
            LedgerClientBFT client =
                new LedgerClientBFT(replicaId, endpoint, random, getSSLContext(), fReplicas)
        )
        {
            createAccounts(client);
            loadMoney(client);
            getAccounts(client);

            //sendTransaction(client);
            sendTransactionBFT(client);

            //getBalance(client);
            getBalanceBFT(client);

            getTotalValue(client);
            getGlobalLedgerValue(client);
            getLedger(client);
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
                    55, originAccount.getValue(), nonce),
                    tp2.bitdlp.impl.srv.resources.requests.Request.Operation.SEND_TRANSACTION_ASYNC);

                // expected to fail with 403
                requestBFTOp(() -> client.sendTransactionBFT(originAccount.getKey(), destAccountId,
                    55, originAccount.getValue(), nonce),
                    tp2.bitdlp.impl.srv.resources.requests.Request.Operation.SEND_TRANSACTION_ASYNC);
            }
        }
    }

    protected <T> Result<T> requestBFTOp(BFTRequest<T> request, tp2.bitdlp.impl.srv.resources.requests.Request.Operation operation) throws InvalidServerSignatureException
    {
        long before = System.currentTimeMillis();
        Pair<Result<T>, ReplyWithSignatures> result = request.request();
        long after = System.currentTimeMillis();

        this.latencies.get(operation).add(after - before);
        
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
