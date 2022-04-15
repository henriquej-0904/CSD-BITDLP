package itdlp.data;

import java.util.List;

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

    }

    @Override
    public Result<Account> createAccount(AccountId accountId) {
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
    public Result<Integer> getTotalValue(List<AccountId> accounts) {
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
