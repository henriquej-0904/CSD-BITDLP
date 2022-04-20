package itdlp.tp1.impl.srv.resources;

import java.util.Map;

import itdlp.tp1.api.Account;
import itdlp.tp1.api.AccountId;
import itdlp.tp1.api.operations.LedgerDeposit;
import itdlp.tp1.api.operations.LedgerTransaction;
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
            
            int value = this.db.getGlobalLedgerValue().resultOrThrow();

            LOG.info("Global Ledger Value: " + value);

            return value;
        } catch (WebApplicationException e) {
            LOG.info(e.getMessage());
            throw e;
        }
    }

    @Override
    public void loadMoney(AccountId accountId, LedgerDeposit value) {
        this.db.loadMoney(accountId, value).resultOrThrow();
    }

    @Override
    public void sendTransaction(LedgerTransaction transaction) {
        this.db.sendTransaction(transaction).resultOrThrow();
    }

    @Override
    public Map<AccountId, Account> getLedger() {
        try {
            init();

            Map<AccountId, Account> result = this.db.getLedger().resultOrThrow();

            LOG.info(String.format("Get Ledger with %d accounts.", result.size()));

            return result;
        } catch (WebApplicationException e) {
            LOG.info(e.getMessage());
            throw e;
        }
    }
}
