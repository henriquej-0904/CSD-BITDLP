package tp2.bitdlp.impl.srv.resources.bft.bftsmart;

import java.nio.ByteBuffer;

import com.fasterxml.jackson.core.type.TypeReference;

import bftsmart.tom.AsynchServiceProxy;
import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceProxy;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.core.messages.TOMMessageType;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import tp2.bitdlp.api.Account;
import tp2.bitdlp.api.AccountId;
import tp2.bitdlp.api.operations.LedgerDeposit;
import tp2.bitdlp.api.operations.LedgerOperation;
import tp2.bitdlp.api.operations.LedgerTransaction;
import tp2.bitdlp.data.LedgerState;
import tp2.bitdlp.impl.srv.config.ServerConfig;
import tp2.bitdlp.impl.srv.resources.bft.AccountsResourceBFT;
import tp2.bitdlp.impl.srv.resources.bft.ReplyWithSignature;
import tp2.bitdlp.impl.srv.resources.bft.ReplyWithSignatures;
import tp2.bitdlp.impl.srv.resources.requests.CreateAccount;
import tp2.bitdlp.impl.srv.resources.requests.GetBalance;
import tp2.bitdlp.impl.srv.resources.requests.GetFullLedger;
import tp2.bitdlp.impl.srv.resources.requests.GetGlobalValue;
import tp2.bitdlp.impl.srv.resources.requests.GetAccount;
import tp2.bitdlp.impl.srv.resources.requests.GetTotalValue;
import tp2.bitdlp.impl.srv.resources.requests.SendTransaction;
import tp2.bitdlp.impl.srv.resources.requests.LoadMoney;
import tp2.bitdlp.impl.srv.resources.requests.Request;
import tp2.bitdlp.util.Crypto;
import tp2.bitdlp.util.Result;
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
    protected ReplyWithSignatures<byte[]> invokeAsync(byte[] request, TOMMessageType type)
    {
        int f = asyncProxy.getViewManager().getCurrentViewF(); // Verificar que corresponde a F replicas

        AsyncReplyListener replyListener = new AsyncReplyListener(2 * f + 1);

        int opId = asyncProxy.invokeAsynchRequest(request, replyListener, type);

        ReplyWithSignatures<byte[]> reply = null;

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
    public void loadMoney(LoadMoney clientParams, LedgerDeposit value) {
        byte[] request = toJson(clientParams);
        byte[] result = invokeOrdered(request);

        this.fromJson(result,
            new TypeReference<Result<Void>>() { }).resultOrThrow();
    }

    @Override
    public void sendTransaction(SendTransaction clientParams, LedgerTransaction transaction) {
        byte[] request = toJson(clientParams);
        byte[] result = invokeOrdered(request);

        this.fromJson(result,
            new TypeReference<Result<Void>>() { }).resultOrThrow();
    }

    @Override
    public LedgerOperation[] getFullLedger() {
        byte[] request = toJson(new GetFullLedger());
        byte[] result = invokeUnordered(request);

        return this.fromJson(result,
            new TypeReference<Result<LedgerOperation[]>>() { }).resultOrThrow();
    }

    @Override
    public ReplyWithSignatures<byte[]> getBalanceAsync(GetBalance clientParams, AccountId accountId)
    {
        byte[] request = toJson(clientParams);
        return invokeAsync(request, TOMMessageType.UNORDERED_REQUEST);
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
                    case LOAD_MONEY:
                        result = loadMoney((LoadMoney) request);
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
                return AccountsResourceWithBFTSMaRt.this.db.getState().resultOrThrow().getSerializedState();
            }catch(Exception e){
                //Utils.logError(e, LOG);
                return new byte[0];
            }
        }

        @Override
        public void installSnapshot(byte[] arg0) {
            try{
                init();

                AccountsResourceWithBFTSMaRt.this.db.loadState(new LedgerState(arg0)).resultOrThrow();

            }catch(Exception e){
                //Utils.logError(e, LOG);
            }
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

        protected Result<LedgerOperation[]> getLedger(GetFullLedger request) {
            Result<LedgerOperation[]> result = AccountsResourceWithBFTSMaRt.this.db.getLedger();

            if (result.isOK())
                LOG.info(String.format("Get Ledger with %d operations.", result.value().length));
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
            LedgerTransaction transaction = null;
            Result<Void> result;
            try {
                transaction = verifySendTransaction(request);
                result = AccountsResourceWithBFTSMaRt.this.db.sendTransaction(transaction);
            } catch (WebApplicationException e) {
                result = Result.error(e);
            }

            if (result.isOK())
                LOG.info(String.format("ORIGIN: %s, DEST: %s, TYPE: %s, VALUE: %d", 
                    transaction.getOrigin(), transaction.getOrigin(),
                    transaction.getType(), transaction.getValue()));
            else
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

        protected Result<Void> loadMoney(LoadMoney request) {
            // verify and execute
            LedgerDeposit deposit = null;
            Result<Void> result;
            try {
                deposit = verifyLoadMoney(request);
                result = AccountsResourceWithBFTSMaRt.this.db.loadMoney(deposit);
            } catch (WebApplicationException e) {
                result = Result.error(e);
            }

            if (result.isOK())
                LOG.info(String.format("ID: %s, TYPE: %s, VALUE: %s",
                    deposit.getAccountId(), LedgerOperation.Type.DEPOSIT, request.getValue()));
            else
                LOG.info(result.errorException().getMessage());

            return result;
        }

        protected ReplyWithSignature<byte[]> encodeAndSignReply(Result<?> result)
        {
            ReplyWithSignature<byte[]> reply = new ReplyWithSignature<>();
            reply.setStatusCode(result.error());

            if (result.isOK() && result.value() != null)
                reply.setReply(toJson(result.value()));

            return signReply(reply);
        }

        protected ReplyWithSignature<byte[]> signReply(ReplyWithSignature<byte[]> reply)
        {
            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
            buffer.putInt(reply.getStatusCode());

            reply.setSignature(Crypto.sign(ServerConfig.getKeyPair(), buffer.array(), reply.getReply()));

            return reply;
        }
    }
}
