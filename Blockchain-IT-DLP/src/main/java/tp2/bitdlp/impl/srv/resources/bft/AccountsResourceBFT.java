package tp2.bitdlp.impl.srv.resources.bft;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import tp2.bitdlp.api.AccountId;
import tp2.bitdlp.api.service.Accounts;
import tp2.bitdlp.api.service.AccountsWithBFTOps;
import tp2.bitdlp.impl.srv.config.ServerConfig;
import tp2.bitdlp.impl.srv.resources.AccountsResource;
import tp2.bitdlp.impl.srv.resources.requests.GetBalance;
import tp2.bitdlp.impl.srv.resources.requests.ProposeMinedBlock;
import tp2.bitdlp.impl.srv.resources.requests.SendTransaction;
import tp2.bitdlp.impl.srv.resources.requests.SmartContractValidation;
import tp2.bitdlp.pow.block.BCBlock;
import tp2.bitdlp.pow.transaction.LedgerTransaction;
import tp2.bitdlp.smartcontract.SmartContract;
import tp2.bitdlp.util.Pair;
import tp2.bitdlp.util.Utils;
import tp2.bitdlp.util.reply.ReplyWithSignatures;
import tp2.bitdlp.util.result.Result;

/**
 * An abstract class that defines methods and operations for implementing a BFT service.
 * This service also supports asynchronous operations.
 */
public abstract class AccountsResourceBFT extends AccountsResource implements AccountsWithBFTOps
{
    @Override
    public ReplyWithSignatures getBalanceBFT(String accountId) {
        GetBalance clientParams;

        try {
            init();

            clientParams = new GetBalance(accountId);
            clientParams.async();
            verifyGetBalance(clientParams);
        } catch (WebApplicationException e) {
            LOG.info(e.getMessage());
            throw e;
        }

        ReplyWithSignatures reply;
        String signature;

        try {
            reply = getBalanceAsync(clientParams);

            // LOG.info(String.format("Balance - %d, %s\n", result, accountId));

            signature = signReplyWithSignatures(reply);

        } catch (WebApplicationException e) {
            if (e.getResponse().getStatus() == 500)
                Utils.logError(e, LOG);

            throw e;
        }
        

        throw new WebApplicationException(
                Response.status(Status.fromStatusCode(reply.getStatusCode()))
                        .type(MediaType.APPLICATION_JSON)
                        .entity(reply)
                        .header(Accounts.SERVER_SIG, signature)
                        .build());
    }

    /**
	 * Returns the balance of an account.
	 *
     * @param clientParams
     * 
     * @return The balance of the account and a set of 2f + 1 signatures of the replicas,
     * all signed by the replica that responds to the client.
	 */
    public abstract ReplyWithSignatures getBalanceAsync(GetBalance clientParams);


    @Override
    public ReplyWithSignatures sendTransactionBFT(SendTransaction params)
    {
        try {
            init();

            params.async();
        } catch (WebApplicationException e) {
            LOG.info(e.getMessage());
            throw e;
        }

        ReplyWithSignatures reply;
        String signature;

        try {
            reply = sendTransactionAsync(params);
            signature = signReplyWithSignatures(reply);
        } catch (WebApplicationException e) {
            if (e.getResponse().getStatus() == 500)
                Utils.logError(e, LOG);

            throw e;
        }
        

        // log operation if successful
        // LOG.info(String.format("ORIGIN: %s, DEST: %s, TYPE: %s, VALUE: %d",
        // originId, destId, transaction.getType(), value));

        Status replyStatus = Status.fromStatusCode(reply.getStatusCode());
        throw new WebApplicationException(
                Response.status(replyStatus == Status.NO_CONTENT ? Status.OK : replyStatus)
                .type(MediaType.APPLICATION_JSON)
                .entity(reply)
                .header(Accounts.SERVER_SIG, signature)
                .build());
    }

    /**
	 * Transfers money from an origin to a destination.
	 *
     * @param clientParams
     * 
     * @return the transaction and a set of 2f + 1 signatures of the replicas,
     * all signed by the replica that responds to the client.
	 */
    public abstract ReplyWithSignatures sendTransactionAsync(SendTransaction clientParams);


    @Override
    public ReplyWithSignatures proposeMinedBlockBFT(Pair<String, BCBlock> pairMinerIdBlock, String signature) {
        ProposeMinedBlock clientParams;

        try {
            init();

            clientParams = new ProposeMinedBlock(pairMinerIdBlock.getLeft(), signature, pairMinerIdBlock.getRight());
            verifyMinedBlockIntegrity(clientParams);
        } catch (WebApplicationException e) {
            Utils.logError(e, LOG);
            throw e;
        }

        ReplyWithSignatures reply;
        String serverSig;

        try {
            reply = proposeMinedBlockAsync(clientParams);
            serverSig = signReplyWithSignatures(reply);
        } catch (WebApplicationException e) {
            if (e.getResponse().getStatus() == 500)
                Utils.logError(e, LOG);

            throw e;
        }

        Status replyStatus = Status.fromStatusCode(reply.getStatusCode());
        throw new WebApplicationException(
                Response.status(replyStatus)
                .type(MediaType.APPLICATION_JSON)
                .entity(reply)
                .header(Accounts.SERVER_SIG, serverSig)
                .build());
    }

    /**
	 * Propose a mined block
	 *
     * @param clientParams
     * 
     * @return the hash of the block if success and a set of 2f + 1 signatures of the replicas,
     * all signed by the replica that responds to the client.
	 */
    public abstract ReplyWithSignatures proposeMinedBlockAsync(ProposeMinedBlock clientParams);

    @Override
    public ReplyWithSignatures smartContractValidationBFT(SmartContractValidation param)
    {
        try {
            init();
        } catch (WebApplicationException e) {
            Utils.logError(e, LOG);
            throw e;
        }

        ReplyWithSignatures reply;
        String serverSig;

        try {
            reply = smartContractValidationAsync(param);
            serverSig = signReplyWithSignatures(reply);
        } catch (WebApplicationException e) {
            if (e.getResponse().getStatus() == 500)
                Utils.logError(e, LOG);

            throw e;
        }

        Status replyStatus = Status.fromStatusCode(reply.getStatusCode());
        throw new WebApplicationException(
                Response.status(replyStatus)
                .type(MediaType.APPLICATION_JSON)
                .entity(reply)
                .header(Accounts.SERVER_SIG, serverSig)
                .build());
    }

    protected abstract ReplyWithSignatures smartContractValidationAsync(SmartContractValidation param);

    protected Result<byte[]> smartContractValidation(SmartContractValidation param)
    {
        try {
            AccountId origin = getAccountId(param.getOriginDestPair().getLeft());
            AccountId dest = getAccountId(param.getOriginDestPair().getRight());
            
            // check signature
            byte[] digest = param.digest();

            if (!verifySignature(origin, param.getSignature(), digest))
                throw new ForbiddenException("Invalid signature");

            // run smart-contract and validate transaction
            SmartContract smartContract = new SmartContract(param.getName(), param.getCode());
            smartContract.compile().resultOrThrow();

            LedgerTransaction transaction = LedgerTransaction.newTransaction(origin, dest, param.getValue(), param.getNonce());
            transaction.setClientSignature(param.getSignature());
            boolean validated = smartContract.execute(transaction).resultOrThrow();

            if (!validated)
                throw new WebApplicationException("The smart-contract did not validate the transaction");

            return Result.ok(digest);

        } catch (WebApplicationException e) {
            LOG.info(e.getMessage());
            return Result.error(e);
        }
    }

    /**
     * Sign a reply with signatures.
     * @param reply
     * @return The generated signature.
     */
    protected String signReplyWithSignatures(ReplyWithSignatures reply)
    {
        try {
            return reply.sign(ServerConfig.getKeyPair().getPrivate());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage(), e);
        }
    }
}
