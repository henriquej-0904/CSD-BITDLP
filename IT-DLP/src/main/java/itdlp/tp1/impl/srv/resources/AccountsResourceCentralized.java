package itdlp.tp1.impl.srv.resources;

import itdlp.tp1.api.Account;
import itdlp.tp1.api.AccountId;
import itdlp.tp1.api.operations.LedgerDeposit;
import itdlp.tp1.api.operations.LedgerTransaction;

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
    public int getGlobalValue() {
            return this.db.getGlobalLedgerValue().resultOrThrow();
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
    public Account[] getFullLedger() {
        return this.db.getLedger().resultOrThrow();
    }
}
