package itdlp.impl.srv.resources;

import java.util.List;

import itdlp.api.Account;
import itdlp.api.AccountId;
import itdlp.api.service.Accounts;

/**
 * A centralized implementation of the Accounts API
 */
public class AccountsResourceCentralized implements Accounts
{
    

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
