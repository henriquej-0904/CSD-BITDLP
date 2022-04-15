package itdlp.data;

import java.util.List;

import itdlp.api.Account;
import itdlp.api.AccountId;

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
    public Account createAccount(AccountId id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Account getAccount(AccountId id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getBalance(AccountId id) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getTotalValue(List<AccountId> accounts) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getGlobalLedgerValue() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int loadMoney(AccountId id, int value) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void sendTransaction(AccountId origin, AccountId dest, int value) {
        // TODO Auto-generated method stub
        
    }
    
}
