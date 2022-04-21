package itdlp.tp1.impl.srv.resources.bft;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceProxy;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import itdlp.tp1.api.Account;
import itdlp.tp1.api.AccountId;
import itdlp.tp1.api.operations.LedgerDeposit;
import itdlp.tp1.api.operations.LedgerTransaction;
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
import jakarta.ws.rs.InternalServerErrorException;

/**
 * An implementation of the Accounts API with BFT SMaRt
 */
public class AccountsResourceWithBFTSMaRt extends AccountsResource
{

    private static ServiceProxy proxy;

    public static ServiceProxy getProxy() {
        return proxy;
    }

    public static void setProxy(ServiceProxy proxy) {
        AccountsResourceWithBFTSMaRt.proxy = proxy;
    }

    protected Object readObject(byte[] arr){
        try ( ByteArrayInputStream inputArr = new ByteArrayInputStream(arr);
              ObjectInputStream as = new ObjectInputStream(inputArr);
            ){
                return as.readObject();
        } catch (Exception e) {
                throw new InternalServerErrorException(e.getMessage(), e);
        }
    }

    protected byte[] writeObject(Object req){
        try ( ByteArrayOutputStream outputArr = new ByteArrayOutputStream();
              ObjectOutputStream os = new ObjectOutputStream(outputArr);
            ){
                os.writeObject(req);
                os.flush();

                return outputArr.toByteArray();
        } catch (Exception e) {
                throw new InternalServerErrorException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> Result<T> readResult(byte[] arr){
        return (Result<T>) readObject(arr);
    }


    @Override
    public Account createAccount(Account account) {
        byte[] request = writeObject(new CreateAccount(account));
        byte[] result = proxy.invokeOrdered(request);

        return this.<Account>readResult(result).resultOrThrow();
    }

    @Override
    public Account getAccount(AccountId accountId) {
        byte[] request = writeObject(new GetAccount(accountId));
        byte[] result = proxy.invokeUnordered(request);

        return this.<Account>readResult(result).resultOrThrow();
    }

    @Override
    public int getBalance(AccountId accountId) {
        byte[] request = writeObject(new GetBalance(accountId));
        byte[] result = proxy.invokeUnordered(request);

        return this.<Integer>readResult(result).resultOrThrow();
    }

    @Override
    public int getTotalValue(AccountId[] accounts) {
        byte[] request = writeObject(new GetTotalValue(accounts));
        byte[] result = proxy.invokeUnordered(request);

        return this.<Integer>readResult(result).resultOrThrow();
    }

    @Override
    public int getGlobalValue() {
        byte[] request = writeObject(new GetGlobalValue());
        byte[] result = proxy.invokeUnordered(request);

        return this.<Integer>readResult(result).resultOrThrow();
    }

    @Override
    public void loadMoney(AccountId accountId, LedgerDeposit value) {
        byte[] request = writeObject(new LoadMoney(accountId, value));
        byte[] result = proxy.invokeOrdered(request);

        this.<Void>readResult(result).resultOrThrow();
    }

    @Override
    public void sendTransaction(LedgerTransaction transaction) {
        byte[] request = writeObject(new SendTransaction(transaction));
        byte[] result = proxy.invokeOrdered(request);

        this.<Void>readResult(result).resultOrThrow();
    }

    @Override
    public Account[] getFullLedger() {
        byte[] request = writeObject(new GetFullLedger());
        byte[] result = proxy.invokeUnordered(request);

        return this.<Account[]>readResult(result).resultOrThrow();
    }

    protected class BFTSMaRtServerReplica extends DefaultSingleRecoverable{

        @Override
        public byte[] appExecuteUnordered(byte[] arg0, MessageContext arg1)
        {
            Request request = (Request) readObject(arg0);
            Result<?> result = null;

            switch (request.getOperation()) {
                case GET_ACCOUNT:
                    result = getAccount((GetAccount)request);
                    break;
                case GET_BALANCE:
                    break;
                case GET_GLOBAL_LEDGER_VALUE:
                    break;
                case GET_LEDGER:
                    break;
                case GET_TOTAL_VALUE:
                    break;
                default:
                    break;
            }

            return writeObject(result);
        }

        
        @Override
        public byte[] appExecuteOrdered(byte[] arg0, MessageContext arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public byte[] getSnapshot() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void installSnapshot(byte[] arg0) {
            // TODO Auto-generated method stub
            
        }

        protected Result<Account> getAccount(GetAccount request)
        {
            return AccountsResourceWithBFTSMaRt.this.db.getAccount(request.getId());
        }
        
    }
}
