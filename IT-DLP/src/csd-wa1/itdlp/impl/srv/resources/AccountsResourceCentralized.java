package itdlp.impl.srv.resources;

import itdlp.api.Account;
import itdlp.api.AccountId;
import itdlp.api.operations.LedgerDeposit;
import itdlp.api.operations.LedgerTransaction;
import jakarta.ws.rs.WebApplicationException;

/**
 * A centralized implementation of the Accounts API
 */
public class AccountsResourceCentralized extends AccountsResource
{

    @Override
    public Account createAccount(Account account) {
        return this.db.createAccount(account).resultOrThrow();
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
    public void loadMoney(AccountId accountId, LedgerDeposit value) {
        this.db.loadMoney(accountId, value);
    }

    @Override
    public void sendTransaction(LedgerTransaction transaction) {
        this.db.sendTransaction(transaction);
    }

}
