package itdlp.tp1.impl.srv.resources.bft;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceProxy;
import bftsmart.tom.server.defaultservices.DefaultRecoverable;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import itdlp.tp1.api.Account;
import itdlp.tp1.api.AccountId;
import itdlp.tp1.api.operations.LedgerDeposit;
import itdlp.tp1.api.operations.LedgerTransaction;
import itdlp.tp1.impl.srv.resources.AccountsResource;
import itdlp.tp1.impl.srv.resources.requests.CreateAccount;
import itdlp.tp1.util.Result;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.WebApplicationException;

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

    @SuppressWarnings("unchecked")
    protected <T> Result<T> readResult(byte[] arr){
        try ( ByteArrayInputStream inputArr = new ByteArrayInputStream(arr);
              ObjectInputStream as = new ObjectInputStream(inputArr);
            ){
                return (Result<T>) as.readObject();
        } catch (Exception e) {
                throw new InternalServerErrorException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> byte[] writeRequest(T req){
        try ( ByteArrayOutputStream outputArr = new ByteArrayOutputStream();
              ObjectOutputStream os = new ObjectOutputStream(outputArr);
            ){
                os.writeObject(req);
                os.flush();

                return  outputArr.toByteArray();
        } catch (Exception e) {
                throw new InternalServerErrorException(e.getMessage(), e);
        }
    }


    @Override
    public Account createAccount(Account account) {
        byte[] request = writeRequest(new CreateAccount(account));
        byte[] result = proxy.invokeOrdered(request);

        return this.<Account>readResult(result).resultOrThrow();
    }

    @Override
    public Account getAccount(AccountId accountId) {
    }

    @Override
    public int getBalance(AccountId accountId) {

    }

    @Override
    public int getTotalValue(AccountId[] accounts) {
    }

    @Override
    public int getGlobalLedgerValue() {
    }

    @Override
    public void loadMoney(AccountId accountId, LedgerDeposit value) {
    }

    @Override
    public void sendTransaction(LedgerTransaction transaction) {
    }

    @Override
    public Account[] getLedger() {
    }

    protected class BFTSMaRtServerReplica extends DefaultSingleRecoverable{

        @Override
        public byte[] appExecuteUnordered(byte[] arg0, MessageContext arg1) {
            // TODO Auto-generated method stub
            
            return null;
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
        
    }
}
