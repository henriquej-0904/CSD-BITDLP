package tp2.bitdlp.impl.srv.resources;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import tp2.bitdlp.api.Account;
import tp2.bitdlp.api.AccountId;
import tp2.bitdlp.api.ObjectId;
import tp2.bitdlp.api.UserId;
import tp2.bitdlp.api.service.Accounts;
import tp2.bitdlp.data.LedgerDBlayer;
import tp2.bitdlp.data.LedgerDBlayerException;
import tp2.bitdlp.impl.srv.config.ServerConfig;
import tp2.bitdlp.impl.srv.resources.requests.CreateAccount;
import tp2.bitdlp.impl.srv.resources.requests.GetAccount;
import tp2.bitdlp.impl.srv.resources.requests.GetBalance;
import tp2.bitdlp.impl.srv.resources.requests.GetBlock;
import tp2.bitdlp.impl.srv.resources.requests.GetTotalValue;
import tp2.bitdlp.impl.srv.resources.requests.SendTransaction;
import tp2.bitdlp.pow.block.BCBlock;
import tp2.bitdlp.pow.transaction.InvalidTransactionException;
import tp2.bitdlp.pow.transaction.LedgerTransaction;
import tp2.bitdlp.pow.transaction.pool.TransactionsToMine;
import tp2.bitdlp.util.Crypto;
import tp2.bitdlp.util.Pair;
import tp2.bitdlp.util.Utils;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

public abstract class AccountsResource implements Accounts
{
    protected static final Logger LOG = Logger.getLogger(AccountsResource.class.getSimpleName());

    protected LedgerDBlayer db;

    protected TransactionsToMine transactionsToMine;

    /**
     * Init the db layer instance.
     */
    protected void init()
    {
        try {
            this.db = LedgerDBlayer.getInstance();
            this.transactionsToMine = TransactionsToMine.getInstance();
        } catch (LedgerDBlayerException e) {
            throw new InternalServerErrorException(e.getMessage(), e);
        }
    }

    /**
     * Get the accountId from the specified id.
     * @param accountId
     * @return The AccountId object.
     * @throws BadRequestException if the id is not valid.
     */
    protected AccountId getAccountId(byte[] accountId) throws BadRequestException
    {
        try {
            return new AccountId(accountId);
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage(), e);
        }
    }

    /**
     * Get the userId from the specified id.
     * @param userId
     * @return The UserId object.
     * @throws BadRequestException if the id is not valid.
     */
    protected UserId getUserId(byte[] userId) throws BadRequestException
    {
        try {
            return new UserId(userId);
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage(), e);
        }
    }

    protected PublicKey getPublicKey(ObjectId id)
    {
        try {
            return id.publicKey();
        } catch (InvalidKeySpecException e) {
            throw new BadRequestException(e.getMessage(), e);
        }
    }

    protected boolean verifySignature(ObjectId id, String signature, byte[]... data){
        return Crypto.verifySignature(getPublicKey(id), signature, data);
    }

    public String sign(PrivateKey key, byte[]... data)
    {
        try {
            return Crypto.sign(key, data);
        } catch (InvalidKeyException | SignatureException e) {
            throw new InternalServerErrorException(e.getMessage(), e);
        }
    }


    @Override
    public final Account createAccount(Pair<byte[],byte[]> accountUserPair, String userSignature) {
        CreateAccount clientParams;
        Account newAccount;

        try {
            init();

            clientParams = new CreateAccount(accountUserPair, userSignature);
            newAccount = verifyCreateAccount(clientParams);
        } catch (WebApplicationException e) {
            LOG.info(e.getMessage());
            throw e;
        }

        // execute operation
        Account account = createAccount(clientParams, newAccount);
        //LOG.info(String.format("Created account with %s,\n%s\n", accountId, owner));

        throw new WebApplicationException(
            Response.status(Status.OK)
            .entity(account)
            .header(Accounts.SERVER_SIG, sign(ServerConfig.getKeyPair().getPrivate(), account.digest()))
            .build()
        );
    }

    protected Account verifyCreateAccount(CreateAccount params)
    {
        AccountId accountId = getAccountId(params.getAccountUserPair().getLeft());
        UserId owner = getUserId(params.getAccountUserPair().getRight());

        // verify signature
        if (!verifySignature(owner, params.getUserSignature(),
            params.getAccountUserPair().getLeft(),
            params.getAccountUserPair().getRight()))
                throw new ForbiddenException("Invalid User Signature.");

        return new Account(accountId, owner);
    }

    /**
	 * Creates a new account.
	 *
     * @param clientParams The params sent by the client.
	 * @param account The new account
     * 
     * @return The created account object.
	 */
    public abstract Account createAccount(CreateAccount clientParams, Account account);



    @Override
    public final Account getAccount(String accountId) {
        GetAccount clientParams;
        AccountId id;

        try {
            init();

            clientParams = new GetAccount(accountId);
            id = verifyGetAccount(clientParams);
        } catch (WebApplicationException e) {
            LOG.info(e.getMessage());
            throw e;
        }

        Account account = getAccount(clientParams, id);
        // LOG.info(id.toString());

        throw new WebApplicationException(
                Response.status(Status.OK)
                        .entity(account)
                        .header(Accounts.SERVER_SIG, sign(ServerConfig.getKeyPair().getPrivate(), account.digest()))
                        .build());
    }

    protected AccountId verifyGetAccount(GetAccount clientParams)
    {
        return getAccountId(Utils.fromHex(clientParams.getId()));
    }

    /**
	 * Returns an account with the extract.
	 *
     * @param clientParams
	 * @param accountId account id
     * 
     * @return The account object.
	 */
    public abstract Account getAccount(GetAccount clientParams, AccountId accountId);



    @Override
    public final int getBalance(String accountId) {
        GetBalance clientParams;
        AccountId id;

        try {
            init();

            clientParams = new GetBalance(accountId);
            id = verifyGetBalance(clientParams);
        } catch (WebApplicationException e) {
            LOG.info(e.getMessage());
            throw e;
        }

        int result = getBalance(clientParams, id);
        // LOG.info(String.format("Balance - %d, %s\n", result, accountId));

        // sign result
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(result);

        throw new WebApplicationException(
                Response.status(Status.OK)
                        .entity(result)
                        .header(Accounts.SERVER_SIG, sign(ServerConfig.getKeyPair().getPrivate(), buffer.array()))
                        .build());
    }

    protected AccountId verifyGetBalance(GetBalance clientParams)
    {
        return getAccountId(Utils.fromHex(clientParams.getId()));
    }

    /**
	 * Returns the balance of an account.
	 *
     * @param clientParams
	 * @param accountId account id
     * 
     * @return The balance of the account.
	 */
    public abstract int getBalance(GetBalance clientParams, AccountId accountId);



    @Override
    public final int getTotalValue(byte[][] accounts) {
        GetTotalValue clientParams;
        AccountId[] accountIds;

        try {
            init();

            if (accounts == null || accounts.length == 0)
                throw new BadRequestException();

            clientParams = new GetTotalValue(accounts);
            accountIds = verifyGetTotalValue(clientParams);
        } catch (WebApplicationException e) {
            LOG.info(e.getMessage());
            throw e;
        }

        int result = getTotalValue(clientParams, accountIds);
        LOG.info(String.format("Total value for %d accounts: %d\n", accountIds.length, result));

        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(result);

        throw new WebApplicationException(
                Response.status(Status.OK)
                        .entity(result)
                        .header(Accounts.SERVER_SIG, sign(ServerConfig.getKeyPair().getPrivate(), buffer.array()))
                        .build());
    }

    protected AccountId[] verifyGetTotalValue(GetTotalValue clientParams)
    {
        return Stream.of(clientParams.getAccounts())
        .map(this::getAccountId).collect(Collectors.toList())
        .toArray(new AccountId[0]);
    }

    /**
     * Return total balance of account list
     * 
     * @param clientParams
     * @param accounts
     * @return total balance
     */
    public abstract int getTotalValue(GetTotalValue clientParams, AccountId[] accounts);

    @Override
    public final int getGlobalLedgerValue() {
        try {
            init();
        } catch (WebApplicationException e) {
            LOG.info(e.getMessage());
            throw e;
        }

        int result = getGlobalValue();

        //LOG.info("Global Ledger Value: " + result);

        // sign result
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(result);

        throw new WebApplicationException(
                Response.status(Status.OK)
                        .entity(result)
                        .header(Accounts.SERVER_SIG, sign(ServerConfig.getKeyPair().getPrivate(), buffer.array()))
                        .build());
    }

    /**
     * Return total amount of value registered in the ledger
     * @return total balance
     */
    public abstract int getGlobalValue();


    @Override
    public final LedgerTransaction sendTransaction(Pair<byte[], byte[]> originDestPair, int value,
            String accountSignature, int nonce)
    {
        SendTransaction clientParams;
        LedgerTransaction transaction;

        try {
            init();

            clientParams = new SendTransaction(originDestPair, value, accountSignature, nonce);
            transaction = verifySendTransaction(clientParams);
        } catch (WebApplicationException e) {
            LOG.info(e.getMessage());
            throw e;
        }

        sendTransaction(clientParams, transaction);

        // log operation if successful
        // LOG.info(String.format("ORIGIN: %s, DEST: %s, TYPE: %s, VALUE: %d",
        // originId, destId, transaction.getType(), value));

        throw new WebApplicationException(
                Response.status(Status.OK)
                        .entity(transaction)
                        .header(Accounts.SERVER_SIG, sign(ServerConfig.getKeyPair().getPrivate(), transaction.digest()))
                        .build());
    }

    protected LedgerTransaction verifySendTransaction(SendTransaction clientParams) {
        AccountId originId = getAccountId(clientParams.getOriginDestPair().getLeft());
            AccountId destId = getAccountId(clientParams.getOriginDestPair().getRight());

            // verify signature
            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES*2);
            buffer.putInt(clientParams.getValue());
            buffer.putInt(clientParams.getNonce());

            if (!verifySignature(originId, clientParams.getAccountSignature(), clientParams.getOriginDestPair().getLeft(),
                clientParams.getOriginDestPair().getRight(), buffer.array()))
                throw new ForbiddenException("Invalid Account Signature.");    
        
            try {
                LedgerTransaction t = LedgerTransaction.newTransaction(originId, destId, clientParams.getValue(), clientParams.getNonce());
                t.setClientSignature(clientParams.getAccountSignature());
                return t;
            } catch (InvalidTransactionException e) {
                throw new BadRequestException(e.getMessage(), e);
            }
    }

    /**
	 * Transfers money from an origin to a destination.
	 *
	 * @param transaction the ledger transaction
	 */
    public abstract void sendTransaction(SendTransaction clientParams, LedgerTransaction transaction);

    @Override
    public final LedgerTransaction[] getLedger() {
        try {
            init();
        } catch (WebApplicationException e) {
            LOG.info(e.getMessage());
            throw e;
        }

        LedgerTransaction[] result = getFullLedger();

        //LOG.info(String.format("Get Ledger with %d operations.", result.length));

        MessageDigest digest = Crypto.getSha256Digest();

        for (int i = 0; i < result.length; i++) {
            digest.update(result[i].digest());
        }

        throw new WebApplicationException(
                Response.status(Status.OK)
                        .entity(result)
                        .header(Accounts.SERVER_SIG, sign(ServerConfig.getKeyPair().getPrivate(), digest.digest()))
                        .build());
    }

     /**
     * Obtains the current Ledger.
     * @return The current Ledger.
     */
    public abstract LedgerTransaction[] getFullLedger();


    @Override
    public final BCBlock getBlockToMine(String minerAccountId)
    {
        GetBlock clientParams;

        try {
            init();

            clientParams = new GetBlock(minerAccountId);
            verifyGetBlock(clientParams);
        } catch (WebApplicationException e) {
            LOG.info(e.getMessage());
            throw e;
        }

        BCBlock block = getBlockToMine(clientParams);
        // LOG.info(id.toString());

        throw new WebApplicationException(
                Response.status(Status.OK)
                        .entity(block)
                        .header(Accounts.SERVER_SIG, sign(ServerConfig.getKeyPair().getPrivate(), block.digest()))
                        .build());
    }

    protected AccountId verifyGetBlock(GetBlock clientParams)
    {
        return getAccountId(Utils.fromHex(clientParams.getMinerAccountId()));
    }

    protected abstract BCBlock getBlockToMine(GetBlock clientParams);

    @Override
    public void proposeMinedBlock(BCBlock block) {
        // TODO Auto-generated method stub
        
    }

    protected byte[] toJson(Object obj)
    {
        try {
            return Utils.json.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            throw new InternalServerErrorException(e.getMessage(), e);
        }
    }

    protected <T> T fromJson(byte[] json, Class<T> valueType)
    {
        try {
            return Utils.json.readValue(json, valueType);
        } catch (IOException e) {
            throw new InternalServerErrorException(e.getMessage(), e);
        }
    }

    protected <T> T fromJson(byte[] json, TypeReference<T> valueTypeRef)
    {
        try {
            return Utils.json.readValue(json, valueTypeRef);
        } catch (IOException e) {
            throw new InternalServerErrorException(e.getMessage(), e);
        }
    }
}
