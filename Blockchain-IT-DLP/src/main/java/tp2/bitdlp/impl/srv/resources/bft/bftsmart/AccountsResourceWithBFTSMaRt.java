package tp2.bitdlp.impl.srv.resources.bft.bftsmart;

import com.fasterxml.jackson.core.type.TypeReference;

import bftsmart.tom.AsynchServiceProxy;
import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceProxy;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.core.messages.TOMMessageType;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import tp2.bitdlp.api.Account;
import tp2.bitdlp.api.AccountId;
import tp2.bitdlp.data.LedgerState;
import tp2.bitdlp.impl.srv.config.ServerConfig;
import tp2.bitdlp.impl.srv.resources.bft.AccountsResourceBFT;
import tp2.bitdlp.util.reply.ReplyWithSignature;
import tp2.bitdlp.util.reply.ReplyWithSignatures;
import tp2.bitdlp.impl.srv.resources.requests.CreateAccount;
import tp2.bitdlp.impl.srv.resources.requests.GetBalance;
import tp2.bitdlp.impl.srv.resources.requests.GetFullLedger;
import tp2.bitdlp.impl.srv.resources.requests.GetGlobalValue;
import tp2.bitdlp.impl.srv.resources.requests.GetAccount;
import tp2.bitdlp.impl.srv.resources.requests.GetTotalValue;
import tp2.bitdlp.impl.srv.resources.requests.ProposeMinedBlock;
import tp2.bitdlp.impl.srv.resources.requests.SendTransaction;
import tp2.bitdlp.pow.block.BCBlock;
import tp2.bitdlp.pow.transaction.LedgerTransaction;
import tp2.bitdlp.impl.srv.resources.requests.Request;
import tp2.bitdlp.util.result.Result;
import tp2.bitdlp.util.Utils;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.WebApplicationException;

/**
 * An implementation of the AccountsResourceBFT API with BFT SMaRt
 */
public class AccountsResourceWithBFTSMaRt extends AccountsResourceBFT
{
    private static final long ASYNC_TIME_TO_WAIT_MILLIS = 20;
    private static final long ASYNC_MAX_WAIT_ITER = 2000/ASYNC_TIME_TO_WAIT_MILLIS;

    private static BFTSMaRtServerReplica replica;
    private static ServiceProxy proxy;
    private static AsynchServiceProxy asyncProxy;

    public static ServiceProxy getProxy() {
        return proxy;
    }

    public static void setProxy(ServiceProxy proxy) {
        AccountsResourceWithBFTSMaRt.proxy = proxy;
    }

    public static AsynchServiceProxy getAsyncProxy() {
        return asyncProxy;
    }

    public static void setAsyncProxy(AsynchServiceProxy asyncProxy) {
        AccountsResourceWithBFTSMaRt.asyncProxy = asyncProxy;
    }

    /**
     * @return the replica
     */
    public static BFTSMaRtServerReplica getReplica() {
        return replica;
    }

    /**
     * @param replica the replica to set
     */
    public static void setReplica(BFTSMaRtServerReplica replica) {
        AccountsResourceWithBFTSMaRt.replica = replica;
    }

    protected byte[] invokeOrdered(byte[] request)
    {
        byte[] result = proxy.invokeOrdered(request);
        if (result == null)
            throw new InternalServerErrorException("Invoke ordered returned null");

        return result;
    }

    protected byte[] invokeUnordered(byte[] request)
    {
        byte[] result = proxy.invokeUnordered(request);
        if (result == null)
            throw new InternalServerErrorException("Invoke unordered returned null");

        return result;
    }

    /**
     * Invoke async.
     * @param request
     * @param type
     * @return reply with signatures.
     */
    protected ReplyWithSignatures invokeAsync(byte[] request, TOMMessageType type)
    {
        int f = asyncProxy.getViewManager().getCurrentViewF(); // Verificar que corresponde a F replicas

        AsyncReplyListener replyListener = new AsyncReplyListener(2 * f + 1);

        int opId = asyncProxy.invokeAsynchRequest(request, replyListener, type);

        ReplyWithSignatures reply = null;

        long n = 0;
        while ((reply = replyListener.getReply()) == null && n < ASYNC_MAX_WAIT_ITER) {
            n++;
            try {
                Thread.sleep(ASYNC_TIME_TO_WAIT_MILLIS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        asyncProxy.cleanAsynchRequest(opId);

        if (reply == null)
            throw new InternalServerErrorException("Invoke async returned null");
            
        return reply;
    }

    
    @Override
    public Account createAccount(CreateAccount clientParams, Account account) {
        byte[] request = toJson(clientParams);
        byte[] resultBytes = invokeOrdered(request);

        Result<Account> result = this.fromJson(resultBytes,
            new TypeReference<Result<Account>>() { });

        return result.resultOrThrow();
    }

    @Override
    public Account getAccount(GetAccount clientParams, AccountId accountId) {
        byte[] request = toJson(clientParams);
        byte[] result = invokeUnordered(request);

        return this.fromJson(result,
            new TypeReference<Result<Account>>() { }).resultOrThrow();
    }

    @Override
    public int getBalance(GetBalance clientParams, AccountId accountId) {
        byte[] request = toJson(clientParams);
        byte[] result = invokeUnordered(request);

        return this.fromJson(result,
            new TypeReference<Result<Integer>>() { }).resultOrThrow();
    }

    @Override
    public int getTotalValue(GetTotalValue clientParams, AccountId[] accounts) {
        byte[] request = toJson(clientParams);
        byte[] result = invokeUnordered(request);

        return this.fromJson(result,
            new TypeReference<Result<Integer>>() { }).resultOrThrow();
    }

    @Override
    public int getGlobalValue() {
        byte[] request = toJson(new GetGlobalValue());
        byte[] result = invokeUnordered(request);

        return this.fromJson(result,
            new TypeReference<Result<Integer>>() { }).resultOrThrow();
    }

    @Override
    public void sendTransaction(SendTransaction clientParams, LedgerTransaction transaction) {
        byte[] request = toJson(clientParams);
        byte[] result = invokeOrdered(request);

        this.fromJson(result,
            new TypeReference<Result<Void>>() { }).resultOrThrow();
    }

    @Override
    public BCBlock[] getFullLedger() {
        byte[] request = toJson(new GetFullLedger());
        byte[] result = invokeUnordered(request);

        return this.fromJson(result,
            new TypeReference<Result<BCBlock[]>>() { }).resultOrThrow();
    }

    @Override
    public ReplyWithSignatures getBalanceAsync(GetBalance clientParams)
    {
        byte[] request = toJson(clientParams);
        return invokeAsync(request, TOMMessageType.UNORDERED_REQUEST);
    }

    @Override
    public ReplyWithSignatures sendTransactionAsync(SendTransaction clientParams)
    {
        byte[] request = toJson(clientParams);
        return invokeAsync(request, TOMMessageType.ORDERED_REQUEST);
    }

    @Override
    public ReplyWithSignatures proposeMinedBlockAsync(ProposeMinedBlock clientParams) {
        byte[] request = toJson(clientParams);
        return invokeAsync(request, TOMMessageType.ORDERED_REQUEST);
    }

    public class BFTSMaRtServerReplica extends DefaultSingleRecoverable {

        public BFTSMaRtServerReplica(int id) {
            new ServiceReplica(id, this, this);
        }

        @Override
        public byte[] appExecuteUnordered(byte[] arg0, MessageContext arg1) {
            try {
                init();

                Request request = fromJson(arg0, Request.class);
                Object result = null;

                switch (request.getOperation()) {
                    case GET_ACCOUNT:
                        result = getAccount((GetAccount) request);
                        break;
                    case GET_BALANCE:
                        result = getBalance((GetBalance) request);
                        break;
                    case GET_GLOBAL_LEDGER_VALUE:
                        result = getGlobalLedgerValue();
                        break;
                    case GET_LEDGER:
                        result = getLedger((GetFullLedger) request);
                        break;
                    case GET_TOTAL_VALUE:
                        result = getTotalValue((GetTotalValue) request);
                        break;
                    
                    // ASYNC
                    case GET_BALANCE_ASYNC:
                        result = encodeAndSignReply(getBalance((GetBalance) request));
                        break;
                    default:
                        break;
                }

                byte[] res = toJson(result);
                return res;
            } catch (Exception e) {
                Utils.logError(e, LOG);
                return null;
            }
        }

        @Override
        public byte[] appExecuteOrdered(byte[] arg0, MessageContext arg1) {
            try {
                init();

                Request request = fromJson(arg0, Request.class);
                Object result = null;

                switch (request.getOperation()) {
                    case CREATE_ACCOUNT:
                        result = createAccount((CreateAccount) request);
                        break;
                    case SEND_TRANSACTION:
                        result = sendTransaction((SendTransaction) request);
                        break;
                    case GET_ACCOUNT:
                        result = getAccount((GetAccount) request);
                        break;
                    case GET_BALANCE:
                        result = getBalance((GetBalance) request);
                        break;
                    case GET_GLOBAL_LEDGER_VALUE:
                        result = getGlobalLedgerValue();
                        break;
                    case GET_LEDGER:
                        result = getLedger((GetFullLedger) request);
                        break;
                    case GET_TOTAL_VALUE:
                        result = getTotalValue((GetTotalValue) request);
                        break;

                    // ASYNC
                    case GET_BALANCE_ASYNC:
                        result = encodeAndSignReply(getBalance((GetBalance) request));
                        break;
                    case SEND_TRANSACTION_ASYNC:
                        result = encodeAndSignReply(sendTransaction((SendTransaction) request));
                        break;

                    case PROPOSE_BLOCK_ASYNC:
                        result = encodeAndSignReply(proposeMinedBlock((ProposeMinedBlock) request));
                        break;

                    default:
                        break;

                }

                byte[] res = toJson(result);
                return res;
            } catch (Exception e) {
                Utils.logError(e, LOG);
                return null;
            }
        }

        @Override
        public byte[] getSnapshot() {
            try{
                init();
                LedgerState state = AccountsResourceWithBFTSMaRt.this.db.getState().resultOrThrow();
                return toJson(state);
            }catch(Exception e){
                //Utils.logError(e, LOG);
                return new byte[0];
            }
        }

        @Override
        public void installSnapshot(byte[] arg0) {
            try{
                init();

                LedgerState state = fromJson(arg0, LedgerState.class);
                AccountsResourceWithBFTSMaRt.this.db.loadState(state).resultOrThrow();

            }catch(Exception e){
                //Utils.logError(e, LOG);
            }
        }

        protected Result<String> proposeMinedBlock(ProposeMinedBlock request) {
            // verify and execute
            Result<String> result = AccountsResourceWithBFTSMaRt.this.proposeMinedBlock(request);

            if (result.isOK())
                LOG.info("Added block to blockchain.");
            else
                Utils.logError(result.errorException(), LOG);

            return result;
        }

        protected Result<Account> getAccount(GetAccount request) {
            // verify and execute
            Result<Account> result;
            try {
                result = AccountsResourceWithBFTSMaRt.this.db.getAccount(verifyGetAccount(request));
            } catch (WebApplicationException e) {
                result = Result.error(e);
            }

            if (result.isOK())
                LOG.info(result.value().getId().toString());
            else
                LOG.info(result.errorException().getMessage());

            return result;
        }

        protected Result<Integer> getBalance(GetBalance request) {
            // verify and execute
            Result<Integer> result;
            try {
                result = AccountsResourceWithBFTSMaRt.this.db.getBalance(verifyGetBalance(request));
            } catch (WebApplicationException e) {
                result = Result.error(e);
            }

            if (result.isOK())
                LOG.info(String.format("Balance - %d, %s\n", result.value(), request.getId()));
            else
                LOG.info(result.errorException().getMessage());

            return result;
        }

        protected Result<Integer> getGlobalLedgerValue() {
            Result<Integer> result = AccountsResourceWithBFTSMaRt.this.db.getGlobalLedgerValue();

            if (result.isOK())
                LOG.info("Global Ledger Value: " + result.value());
            else
                LOG.info(result.errorException().getMessage());

            return result;
        }

        protected Result<BCBlock[]> getLedger(GetFullLedger request) {
            Result<BCBlock[]> result = AccountsResourceWithBFTSMaRt.this.db.getLedger();

            if (result.isOK())
                LOG.info(String.format("Get Ledger with %d blocks.", result.value().length));
            else
                LOG.info(result.errorException().getMessage());

            return result;
        }

        protected Result<Integer> getTotalValue(GetTotalValue request) {
            // verify and execute
            Result<Integer> result;
            try {
                result = AccountsResourceWithBFTSMaRt.this.db.getTotalValue(verifyGetTotalValue(request));
            } catch (WebApplicationException e) {
                result = Result.error(e);
            }

            if (result.isOK())
                LOG.info(String.format("Total value for %d accounts: %d\n", request.getAccounts().length, result.value()));
            else
                LOG.info(result.errorException().getMessage());

            return result;
        }

        protected Result<Void> sendTransaction(SendTransaction request) {
            // verify and execute
            Result<Void> result;
            try {
                verifyAndAddTransactionToPool(request);
                result = Result.ok();
            } catch (WebApplicationException e) {
                result = Result.error(e);
            }

            if (!result.isOK())
                LOG.info(result.errorException().getMessage());

            return result;
        }

        protected Result<Account> createAccount(CreateAccount request) {
            // verify and execute
            Result<Account> result;
            try {
                result = AccountsResourceWithBFTSMaRt.this.db.createAccount(verifyCreateAccount(request));
            } catch (WebApplicationException e) {
                result = Result.error(e);
            }

            if (result.isOK())
                LOG.info(String.format("Created account with %s,\n%s\n", result.value().getId(), result.value().getOwner()));
            else
                LOG.info(result.errorException().getMessage());

            return result;
        }

        protected ReplyWithSignature encodeAndSignReply(Result<?> result)
        {
            ReplyWithSignature reply = new ReplyWithSignature();
            reply.setReplicaId(ServerConfig.getReplicaId());
            reply.setStatusCode(result.error());

            if (result.isOK() && result.value() != null)
                reply.setReply(toJson(result.value()));

            try {
                reply.sign(ServerConfig.getKeyPair().getPrivate());
                return reply;
            } catch (Exception e) {
                throw new InternalServerErrorException(e.getMessage(), e);
            }
        }
    }
}
