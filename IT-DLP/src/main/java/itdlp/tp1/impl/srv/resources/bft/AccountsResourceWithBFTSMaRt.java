package itdlp.tp1.impl.srv.resources.bft;

import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceProxy;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import itdlp.tp1.api.Account;
import itdlp.tp1.api.AccountId;
import itdlp.tp1.api.operations.LedgerDeposit;
import itdlp.tp1.api.operations.LedgerOperation;
import itdlp.tp1.api.operations.LedgerTransaction;
import itdlp.tp1.data.LedgerDBlayer;
import itdlp.tp1.data.LedgerDBlayerException;
import itdlp.tp1.data.LedgerState;
import itdlp.tp1.impl.srv.resources.AccountsResource;
import itdlp.tp1.impl.srv.resources.requests.CreateAccount;
import itdlp.tp1.impl.srv.resources.requests.GetBalance;
import itdlp.tp1.impl.srv.resources.requests.GetFullLedger;
import itdlp.tp1.impl.srv.resources.requests.GetGlobalValue;
import itdlp.tp1.impl.srv.resources.requests.GetAccount;
import itdlp.tp1.impl.srv.resources.requests.GetTotalValue;
import itdlp.tp1.impl.srv.resources.requests.SendTransaction;
import itdlp.tp1.impl.srv.resources.requests.LoadMoney;
import itdlp.tp1.impl.srv.resources.requests.Request;
import itdlp.tp1.util.Result;
import itdlp.tp1.util.Utils;
import jakarta.ws.rs.InternalServerErrorException;

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
    public Account createAccount(Account account) {
        byte[] request = writeObject(new CreateAccount(account));
        byte[] result = invokeOrdered(request);

        return this.<Account>readResult(result).resultOrThrow();
    }

    @Override
    public Account getAccount(AccountId accountId) {
        byte[] request = writeObject(new GetAccount(accountId));
        byte[] result = invokeUnordered(request);

        return this.<Account>readResult(result).resultOrThrow();
    }

    @Override
    public int getBalance(AccountId accountId) {
        byte[] request = writeObject(new GetBalance(accountId));
        byte[] result = invokeUnordered(request);

        return this.<Integer>readResult(result).resultOrThrow();
    }

    @Override
    public int getTotalValue(AccountId[] accounts) {
        byte[] request = writeObject(new GetTotalValue(accounts));
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
    public void loadMoney(LedgerDeposit value) {
        byte[] request = writeObject(new LoadMoney(value));
        byte[] result = invokeOrdered(request);

        this.<Void>readResult(result).resultOrThrow();
    }

    @Override
    public void sendTransaction(LedgerTransaction transaction) {
        byte[] request = writeObject(new SendTransaction(transaction));
        byte[] result = invokeOrdered(request);

        this.<Void>readResult(result).resultOrThrow();
    }

    @Override
    public LedgerOperation[] getFullLedger() {
        byte[] request = writeObject(new GetFullLedger());
        byte[] result = invokeUnordered(request);

        return this.<LedgerOperation[]>readResult(result).resultOrThrow();
    }

    public static class BFTSMaRtServerReplica extends DefaultSingleRecoverable {
        private LedgerDBlayer db;

        public BFTSMaRtServerReplica(int id) {
            new ServiceReplica(id, this, this);
        }

        /**
         * Init the db layer instance.
         */
        protected void init() {
            try {
                this.db = LedgerDBlayer.getInstance();
            } catch (LedgerDBlayerException e) {
                throw new InternalServerErrorException(e.getMessage(), e);
            }
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
                LOG.severe(e.getMessage());
                return new byte[0];
            }
        }

        @Override
        public void installSnapshot(byte[] arg0) {
            try{
                init();

                this.db.loadState(new LedgerState(arg0)).resultOrThrow();

            }catch(Exception e){
                LOG.severe(e.getMessage());
            }
        }

        protected Result<Account> getAccount(GetAccount request) {
            return this.db.getAccount(request.getId());
        }

        protected Result<Integer> getBalance(GetBalance request) {
            return this.db.getBalance(request.getAccount());
        }

        protected Result<Integer> getGlobalLedgerValue() {
            return this.db.getGlobalLedgerValue();
        }

        protected Result<LedgerOperation[]> getLedger(GetFullLedger request) {
            return this.db.getLedger();
        }

        protected Result<Integer> getTotalValue(GetTotalValue request) {
            return this.db.getTotalValue(request.getAccounts());
        }

        protected Result<Void> sendTransaction(SendTransaction request) {
            return this.db.sendTransaction(request.getTransaction());
        }

        protected Result<Account> createAccount(CreateAccount request) {
            return this.db.createAccount(request.getAccount());
        }

        protected Result<Void> loadMoney(LoadMoney request) {
            return this.db.loadMoney(request.getValue());
        }
    }
}
