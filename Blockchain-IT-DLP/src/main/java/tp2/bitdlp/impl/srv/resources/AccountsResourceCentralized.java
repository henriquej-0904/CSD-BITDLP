package tp2.bitdlp.impl.srv.resources;

import tp2.bitdlp.api.Account;
import tp2.bitdlp.api.AccountId;
import tp2.bitdlp.api.operations.LedgerDeposit;
import tp2.bitdlp.api.operations.LedgerOperation;
import tp2.bitdlp.api.operations.LedgerTransaction;

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
    public void loadMoney(LedgerDeposit value) {
        this.db.loadMoney(value).resultOrThrow();
    }

    @Override
    public void sendTransaction(LedgerTransaction transaction) {
        this.db.sendTransaction(transaction).resultOrThrow();
    }

    @Override
    public LedgerOperation[] getFullLedger() {
        return this.db.getLedger().resultOrThrow();
    }
}