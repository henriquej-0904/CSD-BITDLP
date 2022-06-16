package tp2.bitdlp.impl.srv.resources;

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
import tp2.bitdlp.impl.srv.resources.requests.CreateAccount;
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
        init();

        CreateAccount clientParams = new CreateAccount(accountUserPair, userSignature);
        Account newAccount = verifyCreateAccount(clientParams);

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
        try {
            init();
            
            AccountId id = getAccountId(Utils.fromHex(accountId));

            Account account = getAccount(id);
            LOG.info(id.toString());

            throw new WebApplicationException(
                Response.status(Status.OK)
                .entity(account)
                .header(Accounts.SERVER_SIG, Crypto.sign(ServerConfig.getKeyPair(), account.digest()))
                .build()
            );
        } catch (WebApplicationException e) {
            LOG.info(e.getMessage());
            throw e;
        }
    }

    /**
	 * Returns an account with the extract.
	 *
	 * @param accountId account id
     * 
     * @return The account object.
	 */
    public abstract Account getAccount(AccountId accountId);



    @Override
    public final int getBalance(String accountId) {
        try {
            init();
            
            AccountId id = getAccountId(Utils.fromHex(accountId));
            
            int result = getBalance(id);
            LOG.info(String.format("Balance - %d, %s\n", result, accountId));

            // sign result
            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
            buffer.putInt(result);

            throw new WebApplicationException(
                Response.status(Status.OK)
                .entity(result)
                .header(Accounts.SERVER_SIG, Crypto.sign(ServerConfig.getKeyPair(), buffer.array()))
                .build()
            );            

        } catch (WebApplicationException e) {
            LOG.info(e.getMessage());
            throw e;
        }
    }

    /**
	 * Returns the balance of an account.
	 *
	 * @param accountId account id
     * 
     * @return The balance of the account.
	 */
    public abstract int getBalance(AccountId accountId);



    @Override
    public final int getTotalValue(byte[][] accounts) {
        try {
            init();
            
            if (accounts == null || accounts.length == 0)
                throw new BadRequestException();

            AccountId[] accountIds = Stream.of(accounts)
                .map(this::getAccountId).collect(Collectors.toList())
                .toArray(new AccountId[0]);

            int result = getTotalValue(accountIds);
            LOG.info(String.format("Total value for %d accounts: %d\n", accountIds.length, result));

            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
            buffer.putInt(result);

            throw new WebApplicationException(
                Response.status(Status.OK)
                .entity(result)
                .header(Accounts.SERVER_SIG, Crypto.sign(ServerConfig.getKeyPair(), buffer.array()))
                .build()
            );
        } catch (WebApplicationException e) {
            LOG.info(e.getMessage());
            throw e;
        }
    }

    /**
     * Return total balance of account list
     * @param accounts
     * @return total balance
     */
    public abstract int getTotalValue(AccountId[] accounts);

    @Override
    public final int getGlobalLedgerValue() {
        try {
            init();
            
            int result = getGlobalValue();

            LOG.info("Global Ledger Value: " + result);

            // sign result
            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
            buffer.putInt(result);

            throw new WebApplicationException(
                Response.status(Status.OK)
                .entity(result)
                .header(Accounts.SERVER_SIG, Crypto.sign(ServerConfig.getKeyPair(), buffer.array()))
                .build()
            );
            
        } catch (WebApplicationException e) {
            LOG.info(e.getMessage());
            throw e;
        }
    }

    /**
     * Return total amount of value registered in the ledger
     * @return total balance
     */
    public abstract int getGlobalValue();

    @Override
    public final LedgerDeposit loadMoney(byte[] accountId, int value, String accountSignature) {
        try {
            init();
            
            AccountId id = getAccountId(accountId);

            // verify signature
            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
            buffer.putInt(value);

            byte[] clientSignature = Utils.fromHex(accountSignature);
            if (!verifySignature(id, clientSignature, accountId, buffer.array()))
                throw new ForbiddenException("Invalid Account Signature.");   
            
            // execute operation
            LedgerDeposit deposit = new LedgerDeposit(value, id, clientSignature);

            loadMoney(deposit);

            LOG.info(String.format("ID: %s, TYPE: %s, VALUE: %s", id, deposit.getType(), value));

            throw new WebApplicationException(
                Response.status(Status.OK)
                .entity(deposit)
                .header(Accounts.SERVER_SIG, Crypto.sign(ServerConfig.getKeyPair(), deposit.digest()))
                .build()
            );

        } catch (WebApplicationException e) {
            LOG.info(e.getMessage());
            throw e;
        } catch (InvalidOperationException e) {
            LOG.info(e.getMessage());
            throw new BadRequestException(e.getMessage(), e);
        }
    }

    /**
	 * Loads money into an account.
	 *
     * @param value value to be loaded
	 */
    public abstract void loadMoney(LedgerDeposit deposit);



    @Override
    public final LedgerTransaction sendTransaction(Pair<byte[],byte[]> originDestPair, int value,
        String accountSignature, int nonce) {
        try {
            init();
            
            AccountId originId = getAccountId(originDestPair.getLeft());
            AccountId destId = getAccountId(originDestPair.getRight());

            // verify signature
            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES*2);
            buffer.putInt(value);
            buffer.putInt(nonce);

            byte[] clientSignature = Utils.fromHex(accountSignature);
            if (!verifySignature(originId, clientSignature, originDestPair.getLeft(),
                originDestPair.getRight(), buffer.array()))
                throw new ForbiddenException("Invalid Account Signature.");    
        
            LedgerTransaction transaction = new LedgerTransaction(originId, destId, value, nonce, clientSignature);
            
            sendTransaction(transaction);

            // log operation if successful        
            LOG.info(String.format("ORIGIN: %s, DEST: %s, TYPE: %s, VALUE: %d", 
                originId, destId, transaction.getType(), value));

            throw new WebApplicationException(
                Response.status(Status.OK)
                .entity(transaction)
                .header(Accounts.SERVER_SIG, Crypto.sign(ServerConfig.getKeyPair(), transaction.digest()))
                .build()
            );
                
        } catch (WebApplicationException e) {
            LOG.info(e.getMessage());
            throw e;
        } catch (InvalidOperationException e) {
            LOG.info(e.getMessage());
            throw new BadRequestException(e.getMessage(), e);
        }
    }

    /**
	 * Transfers money from an origin to a destination.
	 *
	 * @param transaction the ledger transaction
	 */
    public abstract void sendTransaction(LedgerTransaction transaction);

    @Override
    public final LedgerOperation[] getLedger() {
        try {
            init();

            LedgerOperation[] result = getFullLedger();

            LOG.info(String.format("Get Ledger with %d operations.", result.length));

            MessageDigest digest = Crypto.getSha256Digest();

            for(int i = 0; i < result.length; i++){
                digest.update(result[i].digest());
            }

            throw new WebApplicationException(
                Response.status(Status.OK)
                .entity(result)
                .header(Accounts.SERVER_SIG, Crypto.sign(ServerConfig.getKeyPair(), digest.digest()))
                .build()
            );
        } catch (WebApplicationException e) {
            LOG.info(e.getMessage());
            throw e;
        }
    }

     /**
     * Obtains the current Ledger.
     * @return The current Ledger.
     */
    public abstract LedgerOperation[] getFullLedger();
    
}
