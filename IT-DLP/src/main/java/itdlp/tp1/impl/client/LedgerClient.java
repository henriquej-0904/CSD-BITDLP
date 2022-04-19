package itdlp.tp1.impl.client;

import java.net.URI;
import java.security.KeyPair;
import java.security.Signature;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import itdlp.tp1.api.Account;
import itdlp.tp1.api.AccountId;
import itdlp.tp1.api.UserId;
import itdlp.tp1.api.service.Accounts;
import itdlp.tp1.util.Crypto;
import itdlp.tp1.util.Result;
import itdlp.tp1.util.Utils;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

public class LedgerClient
{
    private static final int MAX_TRIES = 3;
    private static final int TIMEOUT_MILLIS = 3 * 1000;

    private static final int CONNECT_TIMEOUT = 10;
    private static final int READ_TIMEOUT = 10;

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

            byte[] signed = signature.sign();
            return Utils.toBase64(signed);
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
            Entity.json(new ImmutablePair<>(accountId.getId(), userId.getId()))
            ), Account.class);
    }

    public Result<Account> getAccount(AccountId accountId) {
        // TODO Auto-generated method stub
        return request(this.client.target(this.endpoint).path(Accounts.PATH).path(Utils.toBase64(accountId.getId()))
        .request().accept(MediaType.APPLICATION_JSON)
        .buildGet(), Account.class);
    }

    public Result<Integer> getBalance(AccountId accountId) {
        // TODO Auto-generated method stub
        return request(this.client.target(this.endpoint)
        .path(Accounts.PATH).path("balance").path(Utils.toBase64(accountId.getId()))
        .request()
        .buildGet(), Integer.class);
    }

    public Result<Integer> getTotalValue(AccountId[] accounts) {

        byte[][] arr = (byte[][]) Stream.of(accounts).map(AccountId::getId).toArray();

        // TODO Auto-generated method stub
        return request(this.client.target(this.endpoint)
        .path(Accounts.PATH).path("balance/sum")
        .request()
        .buildPost(
            Entity.json(arr)), Integer.class);
    }

    public Result<Integer> getGlobalLedgerValue() {
        // TODO Auto-generated method stub
        return request(this.client.target(this.endpoint)
        .path(Accounts.PATH).path("balance/ledger")
        .request()
        .buildGet(), Integer.class);
    }

    public Result<Void> loadMoney(AccountId accountId, int value, KeyPair accountKeys) {
        // TODO Auto-generated method stub
        
    }

    public Result<Void> sendTransaction(AccountId originId, AccountId destId, int value, KeyPair originAccountKeys) {
        // TODO Auto-generated method stub
        
    }

    public Result<Map<AccountId, Account>> getLedger() {
        // TODO Auto-generated method stub

        return request(this.client.target(this.endpoint)
        .path(Accounts.PATH).path("ledger")
        .request()
        .buildGet(), new GenericType<Map<AccountId, Account>>() {} );
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

    private <T> Result<T> request(Invocation invocation, GenericType<T> responseType)
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

    private <T> Result<T> parseResponse(Response response, GenericType<T> responseType)
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
