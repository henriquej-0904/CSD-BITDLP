package tp2.bitdlp.impl.srv.resources;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
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
import tp2.bitdlp.api.operations.InvalidOperationException;
import tp2.bitdlp.api.operations.LedgerDeposit;
import tp2.bitdlp.api.operations.LedgerOperation;
import tp2.bitdlp.api.operations.LedgerTransaction;
import tp2.bitdlp.api.service.Accounts;
import tp2.bitdlp.data.LedgerDBlayer;
import tp2.bitdlp.data.LedgerDBlayerException;
import tp2.bitdlp.impl.srv.config.ServerConfig;
import tp2.bitdlp.impl.srv.resources.bft.ReplyWithSignatures;
import tp2.bitdlp.impl.srv.resources.requests.CreateAccount;
import tp2.bitdlp.impl.srv.resources.requests.GetAccount;
import tp2.bitdlp.impl.srv.resources.requests.GetBalance;
import tp2.bitdlp.impl.srv.resources.requests.GetTotalValue;
import tp2.bitdlp.impl.srv.resources.requests.LoadMoney;
import tp2.bitdlp.impl.srv.resources.requests.SendTransaction;
import tp2.bitdlp.util.Crypto;
import tp2.bitdlp.util.Pair;
import tp2.bitdlp.util.Utils;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

public abstract class AccountsResource implements Accounts
{
    protected static final Logger LOG = Logger.getLogger(AccountsResource.class.getSimpleName());

    protected LedgerDBlayer db;

    /**
     * Init the db layer instance.
     */
    protected void init()
    {
        try {
            this.db = LedgerDBlayer.getInstance();
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

    protected boolean verifySignature(ObjectId id, byte[] signature, byte[]... data){
        try {
            Signature verify = Crypto.createSignatureInstance();
            verify.initVerify(getPublicKey(id));

            for (byte[] buff : data)
                verify.update(buff);

            return verify.verify(signature);
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
            .header(Accounts.SERVER_SIG, Crypto.sign(ServerConfig.getKeyPair(), account.digest()))
            .build()
        );
    }

    protected Account verifyCreateAccount(CreateAccount params)
    {
        AccountId accountId = getAccountId(params.getAccountUserPair().getLeft());
        UserId owner = getUserId(params.getAccountUserPair().getRight());

        // verify signature
        if (!verifySignature(owner, Utils.fromHex(params.getUserSignature()),
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
                        .header(Accounts.SERVER_SIG, Crypto.sign(ServerConfig.getKeyPair(), account.digest()))
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
                        .header(Accounts.SERVER_SIG, Crypto.sign(ServerConfig.getKeyPair(), buffer.array()))
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
                        .header(Accounts.SERVER_SIG, Crypto.sign(ServerConfig.getKeyPair(), buffer.array()))
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
                        .header(Accounts.SERVER_SIG, Crypto.sign(ServerConfig.getKeyPair(), buffer.array()))
                        .build());
    }

    /**
     * Return total amount of value registered in the ledger
     * @return total balance
     */
    public abstract int getGlobalValue();

    @Override
    public final LedgerDeposit loadMoney(byte[] accountId, int value, String accountSignature) {
        LoadMoney clientParams;
        LedgerDeposit deposit;

        try {
            init();

            clientParams = new LoadMoney(accountId, value, accountSignature);
            deposit = verifyLoadMoney(clientParams);
        } catch (WebApplicationException e) {
            LOG.info(e.getMessage());
            throw e;
        }

        loadMoney(clientParams, deposit);

        // LOG.info(String.format("ID: %s, TYPE: %s, VALUE: %s", id, deposit.getType(),
        // value));

        throw new WebApplicationException(
                Response.status(Status.OK)
                        .entity(deposit)
                        .header(Accounts.SERVER_SIG, Crypto.sign(ServerConfig.getKeyPair(), deposit.digest()))
                        .build());
    }

    protected LedgerDeposit verifyLoadMoney(LoadMoney clientParams) {
        // verify signature
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(clientParams.getValue());

        byte[] clientSignature = Utils.fromHex(clientParams.getAccountSignature());
        AccountId accountId = getAccountId(clientParams.getAccountId());

        if (!verifySignature(accountId, clientSignature,
            clientParams.getAccountId(), buffer.array()))
            throw new ForbiddenException("Invalid Account Signature.");

        // execute operation
        try {
            return new LedgerDeposit(clientParams.getValue(), accountId, clientSignature);
        } catch (InvalidOperationException e) {
            throw new BadRequestException(e.getMessage(), e);
        }
    }

    /**
	 * Loads money into an account.
	 *
     * @param value value to be loaded
	 */
    public abstract void loadMoney(LoadMoney clientParams, LedgerDeposit deposit);



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
                        .header(Accounts.SERVER_SIG, Crypto.sign(ServerConfig.getKeyPair(), transaction.digest()))
                        .build());
    }

    protected LedgerTransaction verifySendTransaction(SendTransaction clientParams) {
        AccountId originId = getAccountId(clientParams.getOriginDestPair().getLeft());
            AccountId destId = getAccountId(clientParams.getOriginDestPair().getRight());

            // verify signature
            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES*2);
            buffer.putInt(clientParams.getValue());
            buffer.putInt(clientParams.getNonce());

            byte[] clientSignature = Utils.fromHex(clientParams.getAccountSignature());
            if (!verifySignature(originId, clientSignature, clientParams.getOriginDestPair().getLeft(),
                clientParams.getOriginDestPair().getRight(), buffer.array()))
                throw new ForbiddenException("Invalid Account Signature.");    
        
            try {
                return new LedgerTransaction(originId, destId, clientParams.getValue(), clientParams.getNonce(), clientSignature);
            } catch (InvalidOperationException e) {
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
    public final LedgerOperation[] getLedger() {
        try {
            init();
        } catch (WebApplicationException e) {
            LOG.info(e.getMessage());
            throw e;
        }

        LedgerOperation[] result = getFullLedger();

        //LOG.info(String.format("Get Ledger with %d operations.", result.length));

        MessageDigest digest = Crypto.getSha256Digest();

        for (int i = 0; i < result.length; i++) {
            digest.update(result[i].digest());
        }

        throw new WebApplicationException(
                Response.status(Status.OK)
                        .entity(result)
                        .header(Accounts.SERVER_SIG, Crypto.sign(ServerConfig.getKeyPair(), digest.digest()))
                        .build());
    }

     /**
     * Obtains the current Ledger.
     * @return The current Ledger.
     */
    public abstract LedgerOperation[] getFullLedger();




    @Override
    public int getBalanceAsync(String accountId) {
        GetBalance clientParams;
        AccountId id;

        try {
            init();

            clientParams = new GetBalance(accountId);
            clientParams.async();
            id = verifyGetBalance(clientParams);
        } catch (WebApplicationException e) {
            LOG.info(e.getMessage());
            throw e;
        }

        ReplyWithSignatures<byte[]> reply = getBalanceAsync(clientParams, id);

        // LOG.info(String.format("Balance - %d, %s\n", result, accountId));

        String signature = signReplyWithSignatures(reply);

        throw new WebApplicationException(
                Response.status(Status.OK)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(new String(toJson(reply)))
                        .header(Accounts.SERVER_SIG, signature)
                        .build());
    }

    /**
	 * Returns the balance of an account.
	 *
     * @param clientParams
	 * @param accountId account id
     * 
     * @return The balance of the account.
	 */
    public abstract ReplyWithSignatures<byte[]> getBalanceAsync(GetBalance clientParams, AccountId accountId);



    @Override
    public LedgerTransaction sendTransactionAsync(Pair<byte[], byte[]> originDestPair, int value,
            String accountSignature, int nonce) {
        // TODO Auto-generated method stub
        return null;
    }

    protected String signReplyWithSignatures(ReplyWithSignatures<byte[]> reply)
    {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(reply.getStatusCode());

        try {
            Signature signature = Crypto.createSignatureInstance();
            signature.initSign(ServerConfig.getKeyPair().getPrivate());
        
            signature.update(buffer.array());
            signature.update(reply.getReply());

            for (String sig : reply.getSignatures()) {
                signature.update(sig.getBytes());
            }

            return Utils.toHex(signature.sign());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage(), e);
        }
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
