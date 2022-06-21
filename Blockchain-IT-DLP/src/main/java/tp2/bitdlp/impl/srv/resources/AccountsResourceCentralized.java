package tp2.bitdlp.impl.srv.resources;

import tp2.bitdlp.api.Account;
import tp2.bitdlp.api.AccountId;
import tp2.bitdlp.api.operations.LedgerDeposit;
import tp2.bitdlp.api.operations.LedgerOperation;
import tp2.bitdlp.api.operations.LedgerTransaction;
import tp2.bitdlp.impl.srv.resources.requests.CreateAccount;
import tp2.bitdlp.impl.srv.resources.requests.GetAccount;
import tp2.bitdlp.impl.srv.resources.requests.GetBalance;
import tp2.bitdlp.impl.srv.resources.requests.GetTotalValue;
import tp2.bitdlp.impl.srv.resources.requests.LoadMoney;
import tp2.bitdlp.impl.srv.resources.requests.SendTransaction;
import tp2.bitdlp.util.Result;

/**
 * A centralized implementation of the Accounts API
 */
public class AccountsResourceCentralized extends AccountsResource
{

    @Override
    public Account createAccount(CreateAccount clientParams, Account account) {
        Result<Account> result = this.db.createAccount(account);

            if (result.isOK())
                LOG.info(String.format("Created account with %s,\n%s\n", result.value().getId(), result.value().getOwner()));
            else
                LOG.info(result.errorException().getMessage());

            return result.resultOrThrow();
    }

    @Override
    public Account getAccount(GetAccount clientParams, AccountId accountId) {
        // verify and execute
        Result<Account> result = this.db.getAccount(accountId);

        if (result.isOK())
            LOG.info(result.value().getId().toString());
        else
            LOG.info(result.errorException().getMessage());

        return result.resultOrThrow();
    }

    @Override
    public int getBalance(GetBalance clientParams, AccountId accountId) {
        Result<Integer> result = this.db.getBalance(accountId);

            if (result.isOK())
                LOG.info(String.format("Balance - %d, %s\n", result.value(), accountId));
            else
                LOG.info(result.errorException().getMessage());

            return result.resultOrThrow();
    }

    @Override
    public int getTotalValue(GetTotalValue clientParams, AccountId[] accounts) {
        Result<Integer> result = this.db.getTotalValue(accounts);

            if (result.isOK())
                LOG.info(String.format("Total value for %d accounts: %d\n", accounts.length, result.value()));
            else
                LOG.info(result.errorException().getMessage());

            return result.resultOrThrow();
    }

    @Override
    public int getGlobalValue() {
        Result<Integer> result = this.db.getGlobalLedgerValue();

            if (result.isOK())
                LOG.info("Global Ledger Value: " + result.value());
            else
                LOG.info(result.errorException().getMessage());

            return result.resultOrThrow();
    }

    @Override
    public void loadMoney(LoadMoney clientParams, LedgerDeposit deposit) {
            Result<Void> result = this.db.loadMoney(deposit);

            if (result.isOK())
                LOG.info(String.format("ID: %s, TYPE: %s, VALUE: %s",
                    deposit.getAccountId(), LedgerOperation.Type.DEPOSIT, deposit.getValue()));
            else
                LOG.info(result.errorException().getMessage());

            result.resultOrThrow();
    }

    @Override
    public void sendTransaction(SendTransaction clientParams, LedgerTransaction transaction) {
            Result<Void> result = this.db.sendTransaction(transaction);

            if (result.isOK())
                LOG.info(String.format("ORIGIN: %s, DEST: %s, TYPE: %s, VALUE: %d", 
                    transaction.getOrigin(), transaction.getOrigin(),
                    transaction.getType(), transaction.getValue()));
            else
                LOG.info(result.errorException().getMessage());

            result.resultOrThrow();
    }

    @Override
    public LedgerOperation[] getFullLedger() {
        Result<LedgerOperation[]> result = this.db.getLedger();

            if (result.isOK())
                LOG.info(String.format("Get Ledger with %d operations.", result.value().length));
            else
                LOG.info(result.errorException().getMessage());

            return result.resultOrThrow();
    }    
}
