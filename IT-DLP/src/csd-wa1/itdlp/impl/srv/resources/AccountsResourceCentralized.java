package itdlp.impl.srv.resources;

import itdlp.api.Account;
import itdlp.api.AccountId;
import jakarta.ws.rs.WebApplicationException;

/**
 * A centralized implementation of the Accounts API
 */
public class AccountsResourceCentralized extends AccountsResource
{

    @Override
    public Account createAccount(AccountId accountId) {
        return this.db.createAccount(accountId).resultOrThrow();
    }

    @Override
    public Account getAccount(AccountId accountId) {
        return this.db.getAccount(accountId).resultOrThrow();
    }

    @Override
    public int getBalance(AccountId accountId) {
        return this.db.getBalance(accountId).resultOrThrow();
    }

    @Override
    public int getTotalValue(AccountId[] accounts) {
        return this.db.getTotalValue(accounts).resultOrThrow();
    }

    @Override
    public int getGlobalLedgerValue() {
        try {
            init();
            
            return this.db.getGlobalLedgerValue().resultOrThrow();
        } catch (WebApplicationException e) {
            LOG.info(e.getMessage());
            throw e;
        }
    }

    @Override
    public void loadMoney(AccountId accountId, int value) {
        this.db.loadMoney(accountId, value);
    }

    @Override
    public void sendTransaction(AccountId origin, AccountId dest, int value) {
        this.db.sendTransaction(origin, dest, value);
    }

}
