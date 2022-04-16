package itdlp.data;

import java.util.HashMap;
import java.util.Map;

import itdlp.api.Account;
import itdlp.api.AccountId;
import itdlp.util.Result;

/**
 * An implementation of the LedgerDBlayer in Memory.
 * TODO: implement the class.
 */
public class LedgerDBinMemory extends LedgerDBlayer
{
    private static LedgerDBinMemory instance;


    private Map<AccountId, Account> accounts;


    /**
     * Get the current instance of the Ledger DB.
     * @return The Ledger DB.
     */
    public static LedgerDBinMemory getInstance()
    {
        if (instance != null)
            return instance;

        instance = new LedgerDBinMemory();
        return instance;
    }

    private LedgerDBinMemory()
    {
        this.accounts = new HashMap<>();
    }

    @Override
    public Result<Account> createAccount(Account account) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Result<Account> getAccount(AccountId accountId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Result<Integer> getBalance(AccountId accountId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Result<Integer> getTotalValue(AccountId[] accounts) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Result<Integer> getGlobalLedgerValue() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Result<Integer> loadMoney(AccountId id, int value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Result<Void> sendTransaction(AccountId origin, AccountId dest, int value) {
        // TODO Auto-generated method stub
        return null;
    }
}
