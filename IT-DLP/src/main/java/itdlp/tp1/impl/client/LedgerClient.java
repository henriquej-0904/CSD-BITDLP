package itdlp.tp1.impl.client;

import java.io.Closeable;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.net.ssl.SSLContext;

import itdlp.tp1.api.Account;
import itdlp.tp1.api.AccountId;
import itdlp.tp1.api.UserId;
import itdlp.tp1.api.operations.LedgerOperation;
import itdlp.tp1.api.service.Accounts;
import itdlp.tp1.util.Crypto;
import itdlp.tp1.util.Pair;
import itdlp.tp1.util.Result;
import itdlp.tp1.util.Utils;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

public class LedgerClient implements Closeable
{
    private static final int MAX_TRIES = 1;
    private static final int TIMEOUT_MILLIS = 3 * 1000;

    private static final int CONNECT_TIMEOUT = 60 * 3;
    private static final int READ_TIMEOUT = 60 * 3;

    private Client client;

    private PublicKey serverPublicKey;

    private URI endpoint;

    private SecureRandom random;

    
    public LedgerClient(URI endpoint, SecureRandom random)
    {
        this.client = getClientBuilder().build();
        this.endpoint = endpoint;
        this.random = random;
    }

    public LedgerClient(String replicaId, URI endpoint, SecureRandom random, SSLContext sslContext) throws KeyStoreException
    {
        ClientBuilder builder = getClientBuilder().sslContext(sslContext)
            .hostnameVerifier((arg0, arg1) -> true);

        this.client = builder.build();
        this.endpoint = endpoint;
        this.random = random;

        this.serverPublicKey = Crypto.getTrustStore().getCertificate(replicaId).getPublicKey();
    }

    private static ClientBuilder getClientBuilder()
    {
        return ClientBuilder.newBuilder().connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS);
    }

    protected <T> Result<T> verifyResponseSignature(Pair<Result<T>, Response> pairResponse,
        Function<T, byte[]> digestFunc) throws InvalidServerSignatureException
    {
        if (!pairResponse.getLeft().isOK())
            return pairResponse.getLeft();

        String signature = pairResponse.getRight().getHeaderString(Accounts.SERVER_SIG);
        if (signature == null || signature.equals(""))
            throw new InvalidServerSignatureException("Invalid server signature.");

        if (!Crypto.verifySignature(serverPublicKey, signature, digestFunc.apply(pairResponse.getLeft().value()) ))
            throw new InvalidServerSignatureException("Invalid server signature.");

        return pairResponse.getLeft();
    }

    public Result<Account> createAccount(AccountId accountId, UserId userId, KeyPair userKeys)
        throws InvalidServerSignatureException
    {
        String signature = Crypto.sign(userKeys, accountId.getObjectId(), userId.getObjectId());

        Pair<Result<Account>, Response> resultPair = request(this.client.target(this.endpoint).path(Accounts.PATH)
        .request().accept(MediaType.APPLICATION_JSON)
        .header(Accounts.USER_SIG, signature)
        .buildPost(
            Entity.json(new Pair<>(accountId.getObjectId(), userId.getObjectId()))
            ), Account.class);

        return verifyResponseSignature(resultPair, Account::digest);
    }

    public Result<Account> getAccount(AccountId accountId) throws InvalidServerSignatureException {
        Pair<Result<Account>, Response> resultPair = request(this.client.target(this.endpoint)
            .path(Accounts.PATH).path(Utils.toHex(accountId.getObjectId()))
            .request().accept(MediaType.APPLICATION_JSON)
            .buildGet(), Account.class);

        return verifyResponseSignature(resultPair, Account::digest);
    }

    public Result<Integer> getBalance(AccountId accountId) throws InvalidServerSignatureException {
        Pair<Result<Integer>, Response> resultPair =  request(this.client.target(this.endpoint)
            .path(Accounts.PATH).path("balance").path(Utils.toHex(accountId.getObjectId()))
            .request()
            .buildGet(), Integer.class);

        return verifyResponseSignature(resultPair, this::getBytes);
    }

    public Result<Integer> getTotalValue(AccountId[] accounts) throws InvalidServerSignatureException {

        List<byte[]> arr = Stream.of(accounts).map(AccountId::getObjectId).collect(Collectors.toList());

        Pair<Result<Integer>, Response> resultPair = request(this.client.target(this.endpoint)
            .path(Accounts.PATH).path("balance/sum")
            .request()
            .buildPost(Entity.json(arr)), Integer.class);

        return verifyResponseSignature(resultPair, this::getBytes);
    }

    public Result<Integer> getGlobalLedgerValue() throws InvalidServerSignatureException {
        Pair<Result<Integer>, Response> resultPair = request(this.client.target(this.endpoint)
            .path(Accounts.PATH).path("balance/ledger")
            .request()
            .buildGet(), Integer.class);

        return verifyResponseSignature(resultPair, this::getBytes);
    }

    // public Result<Void> loadMoney(AccountId accountId, int value, KeyPair accountKeys)
    // {
    //     ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
    //     buffer.putInt(value);

    //     String signature = Crypto.sign(accountKeys, accountId.getObjectId(), buffer.array());

    //     return request(this.client.target(this.endpoint).path(Accounts.PATH)
    //         .path("balance").path(Integer.toString(value))
    //         .request()
    //         .header(Accounts.ACC_SIG, signature)
    //         .buildPost(Entity.entity(accountId.getObjectId(), MediaType.APPLICATION_OCTET_STREAM)),
    //             Void.class);
    // }

    // public Result<Void> sendTransaction(AccountId originId, AccountId destId, int value, KeyPair originAccountKeys)
    // {
    //     ByteBuffer buffer = ByteBuffer.allocate(2 * Integer.BYTES);
    //     buffer.putInt(value);
    //     int nonce = this.random.nextInt();
    //     buffer.putInt(nonce);

    //     String signature = Crypto.sign(originAccountKeys, originId.getObjectId(), destId.getObjectId(), buffer.array());

    //     return request(this.client.target(this.endpoint).path(Accounts.PATH)
    //         .path("transaction").path(Integer.toString(value))
    //         .request()
    //         .header(Accounts.ACC_SIG, signature)
    //         .header(Accounts.NONCE, nonce)
    //         .buildPost(Entity.json(new Pair<>(originId.getObjectId(), destId.getObjectId()))),
    //             Void.class);
    // }

    // public Result<Void> sendTransaction(AccountId originId, AccountId destId, int value, KeyPair originAccountKeys,
    //     int nonce)
    // {
    //     ByteBuffer buffer = ByteBuffer.allocate(2 * Integer.BYTES);
    //     buffer.putInt(value);
    //     buffer.putInt(nonce);

    //     String signature = Crypto.sign(originAccountKeys, originId.getObjectId(), destId.getObjectId(), buffer.array());

    //     return request(this.client.target(this.endpoint).path(Accounts.PATH)
    //         .path("transaction").path(Integer.toString(value))
    //         .request()
    //         .header(Accounts.ACC_SIG, signature)
    //         .header(Accounts.NONCE, nonce)
    //         .buildPost(Entity.json(new Pair<>(originId.getObjectId(), destId.getObjectId()))),
    //             Void.class);
    // }

    public Result<LedgerOperation[]> getLedger() throws InvalidServerSignatureException {
        Pair<Result<LedgerOperation[]>, Response> resultPair = request(this.client.target(this.endpoint)
        .path(Accounts.PATH).path("ledger")
        .request()
        .buildGet(), LedgerOperation[].class);

        return verifyResponseSignature(resultPair, (ledgerOps) -> 
        {
            MessageDigest digest = Crypto.getSha256Digest();

            for(int i = 0; i < ledgerOps.length; i++){
                digest.update(ledgerOps[i].digest());
            }

            return digest.digest();
        });
    }

    @Override
    public void close() {
        this.client.close();
    }

    private byte[] getBytes(int value)
    {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(value);
        return buffer.array();
    }

    private <T> Pair<Result<T>, Response> request(Invocation invocation, Class<T> responseType)
    {
        int numberTries = MAX_TRIES;
        RuntimeException error = null;

        while (numberTries > 0)
        {
            try (Response response = invocation.invoke();)
            {
                Result<T> result = parseResponse(response, responseType);
                return new Pair<>(result, response);
            }
            catch (Exception e) {
                error = new RuntimeException(e.getMessage(), e);
            }

            if (error != null)
            {
                numberTries--;

                try {
                    Thread.sleep(TIMEOUT_MILLIS);
                } catch (Exception e) {}
            }
        }

        throw error;
    }

    
    private <T> Result<T> parseResponse(Response response, Class<T> responseType)
    {
        Status status = response.getStatusInfo().toEnum();
        if (status == Status.OK)
        {
            try
            {
                T value = response.readEntity(responseType);
                return Result.ok(value);
            } catch (Exception e)
            {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        else if (status == Status.NO_CONTENT)
            return Result.ok();
        else
            return Result.error(status.getStatusCode());
    }
}
