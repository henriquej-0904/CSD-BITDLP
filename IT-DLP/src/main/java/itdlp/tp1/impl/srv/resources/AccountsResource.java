package itdlp.tp1.impl.srv.resources;

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

import itdlp.tp1.api.Account;
import itdlp.tp1.api.AccountId;
import itdlp.tp1.api.ObjectId;
import itdlp.tp1.api.UserId;
import itdlp.tp1.api.operations.InvalidOperationException;
import itdlp.tp1.api.operations.LedgerDeposit;
import itdlp.tp1.api.operations.LedgerOperation;
import itdlp.tp1.api.operations.LedgerTransaction;
import itdlp.tp1.api.service.Accounts;
import itdlp.tp1.data.LedgerDBlayer;
import itdlp.tp1.data.LedgerDBlayerException;
import itdlp.tp1.util.Crypto;
import itdlp.tp1.util.Pair;
import itdlp.tp1.util.Utils;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.WebApplicationException;

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
            return id.getPublicKey();
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
        try {
            init();

            AccountId accountId = getAccountId(accountUserPair.getLeft());
            UserId owner = getUserId(accountUserPair.getRight());

            // verify signature
            if (!verifySignature(owner, Utils.fromHex(userSignature), accountUserPair.getLeft(),
                accountUserPair.getRight()))
                throw new ForbiddenException("Invalid User Signature.");

            // execute operation
            Account account = createAccount(new Account(accountId, owner));
            LOG.info(String.format("Created account with %s,\n%s\n", accountId, owner));

            return account;
        } catch (WebApplicationException e) {
            LOG.info(e.getMessage());
            throw e;
        }
    }

    /**
	 * Creates a new account.
	 *
	 * @param account The new account
     * 
     * @return The created account object.
	 */
    public abstract Account createAccount(Account account);



    @Override
    public final Account getAccount(String accountId) {
        try {
            init();
            
            AccountId id = getAccountId(Utils.fromHex(accountId));
            LOG.info(id.toString());

            return getAccount(id);
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

            return result;
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

            return result;
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
            
            int value = getGlobalValue();

            LOG.info("Global Ledger Value: " + value);

            return value;
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
    public final void loadMoney(byte[] accountId, int value, String accountSignature) {
        try {
            init();
            
            AccountId id = getAccountId(accountId);

            // verify signature
            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
            buffer.putInt(value);

            if (!verifySignature(id, Utils.fromHex(accountSignature), accountId, buffer.array()))
                throw new ForbiddenException("Invalid Account Signature.");   
            
            // execute operation
            LedgerDeposit deposit = new LedgerDeposit(value, id);

            LOG.info(String.format("ID: %s, TYPE: %s, VALUE: %s", id, deposit.getType(), value));

            loadMoney(deposit);
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
    public final void sendTransaction(Pair<byte[],byte[]> originDestPair, int value,
        String accountSignature, int nonce) {
        try {
            init();
            
            AccountId originId = getAccountId(originDestPair.getLeft());
            AccountId destId = getAccountId(originDestPair.getRight());

            // verify signature
            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES*2);
            buffer.putInt(value);
            buffer.putInt(nonce);

            if (!verifySignature(originId, Utils.fromHex(accountSignature), originDestPair.getLeft(),
                originDestPair.getRight(), buffer.array()))
                throw new ForbiddenException("Invalid Account Signature.");    

            // verify nonce
            MessageDigest digest = Crypto.getSha256Digest();

            buffer = ByteBuffer.allocate(Integer.BYTES);
            buffer.putInt(value);

            digest.update(originDestPair.getLeft());
            digest.update(originDestPair.getRight());
            digest.update(buffer.array());
            
            if(!db.nonceVerification(digest.digest(), nonce).resultOrThrow())
                throw new ForbiddenException(" Invalid Nonce.");

            // execute operation            
            LedgerTransaction transaction = new LedgerTransaction(originId, destId, value);

            LOG.info(String.format("ORIGIN: %s, DEST: %s, TYPE: %s, VALUE: %d", 
                originId, destId, transaction.getType(), value));

            sendTransaction(transaction);
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

            LOG.info(String.format("Get Ledger with %d accounts.", result.length));

            return result;
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
