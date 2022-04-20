package itdlp.tp1.impl.client;

import java.io.Closeable;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.Signature;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.RandomUtils;

import itdlp.tp1.api.Account;
import itdlp.tp1.api.AccountId;
import itdlp.tp1.api.UserId;
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

    private URI endpoint;
    /**
     * 
     */
    public LedgerClient(URI endpoint)
    {
        this.client = ClientBuilder.newBuilder().connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .build();

        this.endpoint = endpoint;
    }

    protected String sign(KeyPair keys, byte[]... data)
    {
        try {
            Signature signature = Crypto.createSignatureInstance();
            signature.initSign(keys.getPrivate());
        
            for (byte[] buff : data) {
                signature.update(buff);
            }

            return Utils.toHex(signature.sign());
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public Result<Account> createAccount(AccountId accountId, UserId userId, KeyPair userKeys) {
        String signature = sign(userKeys, accountId.getId(), userId.getId());

        return request(this.client.target(this.endpoint).path(Accounts.PATH)
        .request().accept(MediaType.APPLICATION_JSON)
        .header(Accounts.USER_SIG, signature)
        .buildPost(
            Entity.json(new Pair<>(accountId.getId(), userId.getId()))
            ), Account.class);
    }

    public Result<Account> getAccount(AccountId accountId) {


        return request(this.client.target(this.endpoint)
            .path(Accounts.PATH).path(Utils.toHex(accountId.getId()))
            .request().accept(MediaType.APPLICATION_JSON)
            .buildGet(), Account.class);
    }

    public Result<Integer> getBalance(AccountId accountId) {
        return request(this.client.target(this.endpoint)
            .path(Accounts.PATH).path("balance").path(Utils.toHex(accountId.getId()))
            .request()
            .buildGet(), Integer.class);
    }

    public Result<Integer> getTotalValue(AccountId[] accounts) {

        List<byte[]> arr = Stream.of(accounts).map(AccountId::getId).collect(Collectors.toList());

        return request(this.client.target(this.endpoint)
            .path(Accounts.PATH).path("balance/sum")
            .request()
            .buildPost(Entity.json(arr)), Integer.class);
    }

    public Result<Integer> getGlobalLedgerValue() {
        return request(this.client.target(this.endpoint)
            .path(Accounts.PATH).path("balance/ledger")
            .request()
            .buildGet(), Integer.class);
    }

    public Result<Void> loadMoney(AccountId accountId, int value, KeyPair accountKeys)
    {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(value);

        String signature = sign(accountKeys, accountId.getId(), buffer.array());

        return request(this.client.target(this.endpoint).path(Accounts.PATH)
            .path("balance").path(Integer.toString(value))
            .request()
            .header(Accounts.ACC_SIG, signature)
            .buildPost(Entity.entity(accountId.getId(), MediaType.APPLICATION_OCTET_STREAM)),
                Void.class);
    }

    public Result<Void> sendTransaction(AccountId originId, AccountId destId, int value, KeyPair originAccountKeys)
    {
        ByteBuffer buffer = ByteBuffer.allocate(2 * Integer.BYTES);
        buffer.putInt(value);
        int nonce = RandomUtils.nextInt();
        buffer.putInt(nonce);

        String signature = sign(originAccountKeys, originId.getId(), destId.getId(), buffer.array());

        return request(this.client.target(this.endpoint).path(Accounts.PATH)
            .path("transaction").path(Integer.toString(value))
            .request()
            .header(Accounts.ACC_SIG, signature)
            .header(Accounts.NONCE, nonce)
            .buildPost(Entity.json(new Pair<>(originId.getId(), destId.getId()))),
                Void.class);
    }

    public Result<Account[]> getLedger() {
        return request(this.client.target(this.endpoint)
        .path(Accounts.PATH).path("ledger")
        .request()
        .buildGet(), Account[].class);
    }

    @Override
    public void close() {
        this.client.close();
    }

    private <T> Result<T> request(Invocation invocation, Class<T> responseType)
    {
        int numberTries = MAX_TRIES;
        RuntimeException error = null;

        while (numberTries > 0)
        {
            try (Response response = invocation.invoke();)
            {
                Result<T> result = parseResponse(response, responseType);
                return result;
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
