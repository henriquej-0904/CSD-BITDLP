package tp2.bitdlp.impl.srv.resources.bft;

import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceProxy;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import tp2.bitdlp.api.Account;
import tp2.bitdlp.api.AccountId;
import tp2.bitdlp.api.operations.LedgerDeposit;
import tp2.bitdlp.api.operations.LedgerOperation;
import tp2.bitdlp.api.operations.LedgerTransaction;
import tp2.bitdlp.data.LedgerDBlayer;
import tp2.bitdlp.data.LedgerDBlayerException;
import tp2.bitdlp.data.LedgerState;
import tp2.bitdlp.impl.srv.resources.AccountsResource;
import tp2.bitdlp.impl.srv.resources.requests.CreateAccount;
import tp2.bitdlp.impl.srv.resources.requests.GetBalance;
import tp2.bitdlp.impl.srv.resources.requests.GetFullLedger;
import tp2.bitdlp.impl.srv.resources.requests.GetGlobalValue;
import tp2.bitdlp.impl.srv.resources.requests.GetAccount;
import tp2.bitdlp.impl.srv.resources.requests.GetTotalValue;
import tp2.bitdlp.impl.srv.resources.requests.SendTransaction;
import tp2.bitdlp.impl.srv.resources.requests.LoadMoney;
import tp2.bitdlp.impl.srv.resources.requests.Request;
import tp2.bitdlp.util.Result;
import tp2.bitdlp.util.Utils;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.WebApplicationException;

/**
 * An implementation of the Accounts API with BFT SMaRt
 */
public class AccountsResourceWithBFTSMaRt extends AccountsResource {
    private static BFTSMaRtServerReplica replica;
    private static ServiceProxy proxy;

    public static ServiceProxy getProxy() {
        return proxy;
    }

    public static void setProxy(ServiceProxy proxy) {
        AccountsResourceWithBFTSMaRt.proxy = proxy;
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

    @SuppressWarnings("unchecked")
    protected <T> Result<T> readResult(byte[] arr) {
        Object result = readObject(arr);

        try {
            return (Result<T>) result;
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage(), e);
        }
    }

    public static Object readObject(byte[] arr) {
        try {
            return Utils.readObject(arr);
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage(), e);
        }
    }

    public static byte[] writeObject(Object req) {
        try {
            return Utils.writeObject(req);
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage(), e);
        }
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

    @Override
    public Account createAccount(CreateAccount clientParams, Account account) {
        byte[] request = writeObject(clientParams);
        byte[] result = invokeOrdered(request);

        return this.<Account>readResult(result).resultOrThrow();
    }

    @Override
    public Account getAccount(GetAccount clientParams, AccountId accountId) {
        byte[] request = writeObject(clientParams);
        byte[] result = invokeUnordered(request);

        return this.<Account>readResult(result).resultOrThrow();
    }

    @Override
    public int getBalance(GetBalance clientParams, AccountId accountId) {
        byte[] request = writeObject(clientParams);
        byte[] result = invokeUnordered(request);

        return this.<Integer>readResult(result).resultOrThrow();
    }

    @Override
    public int getTotalValue(GetTotalValue clientParams, AccountId[] accounts) {
        byte[] request = writeObject(clientParams);
        byte[] result = invokeUnordered(request);

        return this.<Integer>readResult(result).resultOrThrow();
    }

    @Override
    public int getGlobalValue() {
        byte[] request = writeObject(new GetGlobalValue());
        byte[] result = invokeUnordered(request);

        return this.<Integer>readResult(result).resultOrThrow();
    }

    @Override
    public void loadMoney(LoadMoney clientParams, LedgerDeposit value) {
        byte[] request = writeObject(clientParams);
        byte[] result = invokeOrdered(request);

        this.<Void>readResult(result).resultOrThrow();
    }

    @Override
    public void sendTransaction(SendTransaction clientParams, LedgerTransaction transaction) {
        byte[] request = writeObject(clientParams);
        byte[] result = invokeOrdered(request);

        this.<Void>readResult(result).resultOrThrow();
    }

    @Override
    public LedgerOperation[] getFullLedger() {
        byte[] request = writeObject(new GetFullLedger());
        byte[] result = invokeUnordered(request);

        return this.<LedgerOperation[]>readResult(result).resultOrThrow();
    }

    public class BFTSMaRtServerReplica extends DefaultSingleRecoverable {
        private LedgerDBlayer db;

        public BFTSMaRtServerReplica(int id) {
            new ServiceReplica(id, this, this);
        }

        @Override
        public byte[] appExecuteUnordered(byte[] arg0, MessageContext arg1) {
            try {
                init();

                Request request = (Request) readObject(arg0);
                Result<?> result = null;

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
                    default:
                        break;
                }

                return writeObject(result);
            } catch (Exception e) {
                LOG.severe(e.getMessage());
                return null;
            }
        }

        @Override
        public byte[] appExecuteOrdered(byte[] arg0, MessageContext arg1) {
            try {
                init();

                Request request = (Request) readObject(arg0);
                Result<?> result = null;

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
                    default:
                        result = null;
                        break;

                }

                return writeObject(result);
            } catch (Exception e) {
                LOG.severe(e.getMessage());
                return null;
            }
        }

        @Override
        public byte[] getSnapshot() {
            try{
                init();
                return this.db.getState().resultOrThrow().getSerializedState();
            }catch(Exception e){
                //Utils.logError(e, LOG);
                return new byte[0];
            }
        }

        @Override
        public void installSnapshot(byte[] arg0) {
            try{
                init();

                this.db.loadState(new LedgerState(arg0)).resultOrThrow();

            }catch(Exception e){
                //Utils.logError(e, LOG);
            }
        }

        protected Result<Account> getAccount(GetAccount request) {
            // verify and execute
            Result<Account> result;
            try {
                result = this.db.getAccount(verifyGetAccount(request));
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
                result = this.db.getBalance(verifyGetBalance(request));
            } catch (WebApplicationException e) {
                result = Result.error(e);
            }

            if (result.isOK())
                LOG.info(String.format("Balance - %d, %s\n", result.value(), request.getAccount()));
            else
                LOG.info(result.errorException().getMessage());

            return result;
        }

        protected Result<Integer> getGlobalLedgerValue() {
            Result<Integer> result = this.db.getGlobalLedgerValue();

            if (result.isOK())
                LOG.info("Global Ledger Value: " + result.value());
            else
                LOG.info(result.errorException().getMessage());

            return result;
        }

        protected Result<LedgerOperation[]> getLedger(GetFullLedger request) {
            Result<LedgerOperation[]> result = this.db.getLedger();

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
                result = this.db.getTotalValue(verifyGetTotalValue(request));
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
                result = this.db.sendTransaction(verifySendTransaction(request));
            } catch (WebApplicationException e) {
                result = Result.error(e);
            }

            if (result.isOK())
                LOG.info(String.format("ORIGIN: %s, DEST: %s, TYPE: %s, VALUE: %d", 
                    request.getTransaction().getOrigin(), request.getTransaction().getOrigin(),
                    request.getTransaction().getType(), request.getTransaction().getValue()));
            else
                LOG.info(result.errorException().getMessage());

            return result;
        }

        protected Result<Account> createAccount(CreateAccount request) {
            // verify and execute
            Result<Account> result;
            try {
                result = this.db.createAccount(verifyCreateAccount(request));
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
            Result<Void> result;
            try {
                result = this.db.loadMoney(verifyLoadMoney(request));
            } catch (WebApplicationException e) {
                result = Result.error(e);
            }

            if (result.isOK())
                LOG.info(String.format("ID: %s, TYPE: %s, VALUE: %s",
                    request.getValue().getAccountId(), request.getValue().getType(), request.getValue().getValue()));
            else
                LOG.info(result.errorException().getMessage());

            return result;
        }
    }
}
