package tp2.bitdlp.impl.srv.resources.blockmess;

import com.fasterxml.jackson.core.type.TypeReference;

import applicationInterface.ApplicationInterface;
import tp2.bitdlp.api.Account;
import tp2.bitdlp.impl.srv.resources.AccountsResource;
import tp2.bitdlp.impl.srv.resources.requests.CreateAccount;
import tp2.bitdlp.impl.srv.resources.requests.ProposeMinedBlock;
import tp2.bitdlp.impl.srv.resources.requests.SendTransaction;
import tp2.bitdlp.impl.srv.resources.requests.Request;
import tp2.bitdlp.util.result.Result;
import tp2.bitdlp.util.Utils;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.WebApplicationException;

/**
 * An implementation of the AccountsResourceBFT API with Blockmess
 */
public class AccountsResourceWithBlockmess extends AccountsResource
{
    private static BlockmessServerReplica replica;

    /**
     * @return the replica
     */
    public static BlockmessServerReplica getReplica() {
        return replica;
    }

    /**
     * @param replica the replica to set
     */
    public static void setReplica(BlockmessServerReplica replica) {
        AccountsResourceWithBlockmess.replica = replica;
    }

    protected byte[] invokeOrdered(byte[] request)
    {
        var res = replica.invokeSyncOperation(request);
        if (res.getLeft() == null)
            throw new InternalServerErrorException("Invoke ordered returned null");

        return res.getLeft();
    }

    @Override
    protected <T> T executeOrderedRequest(Request request, TypeReference<Result<T>> type)
    {
        byte[] requestBytes = toJson(request);
        byte[] resultBytes = invokeOrdered(requestBytes);

        Result<T> result = this.fromJson(resultBytes, type);

        return result.resultOrThrow();
    }

    public class BlockmessServerReplica extends ApplicationInterface {

        public BlockmessServerReplica(int id) {
            super(new String[0]);
        }

        @Override
        public byte[] processOperation(byte[] arg0) {
            try {
                AccountsResourceWithBlockmess.this.init();

                Request request = fromJson(arg0, Request.class);
                Object result = null;

                switch (request.getOperation()) {
                    case CREATE_ACCOUNT:
                        result = createAccount((CreateAccount) request);
                        break;
                    case SEND_TRANSACTION:
                        result = sendTransaction((SendTransaction) request);
                        break;

                    case PROPOSE_BLOCK:
                        result = proposeMinedBlock((ProposeMinedBlock) request);
                        break;
                }

                byte[] res = toJson(result);
                return res;
            } catch (Exception e) {
                Utils.logError(e, LOG);
                return null;
            }
        }

        protected Result<String> proposeMinedBlock(ProposeMinedBlock request) {
            // verify and execute
            Result<String> result = AccountsResourceWithBlockmess.this.proposeMinedBlock(request);

            if (result.isOK())
                LOG.info("Added block to blockchain.");
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
                LOG.info(result.errorException().getMessage());
            }

            return result;
        }

        protected Result<Account> createAccount(CreateAccount request) {
            // verify and execute
            Result<Account> result;
            try {
                result = AccountsResourceWithBlockmess.this.db.createAccount(verifyCreateAccount(request));
                LOG.info(String.format("Created account with %s,\n%s\n",
                    result.value().getId(), result.value().getOwner()));
            } catch (WebApplicationException e) {
                result = Result.error(e);
                LOG.info(result.errorException().getMessage());
            }

            return result;
        }
    }
}
