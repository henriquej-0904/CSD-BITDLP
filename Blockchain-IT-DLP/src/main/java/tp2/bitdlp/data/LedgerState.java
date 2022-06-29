package tp2.bitdlp.data;

import java.util.List;

import tp2.bitdlp.api.AccountId;
import tp2.bitdlp.api.UserId;
import tp2.bitdlp.pow.block.BCBlock;
import tp2.bitdlp.util.Pair;

public class LedgerState {

    private List<Pair<AccountId, UserId>> accounts;
    private List<BCBlock> ledger;

    
    /**
     * @param accounts
     * @param ledger
     */
    public LedgerState(List<Pair<AccountId, UserId>> accounts, List<BCBlock> ledger) {
        this.accounts = accounts;
        this.ledger = ledger;
    }

    public LedgerState() {
    }

    /**
     * @return the accounts
     */
    public List<Pair<AccountId, UserId>> getAccounts() {
        return accounts;
    }

    /**
     * @param accounts the accounts to set
     */
    public void setAccounts(List<Pair<AccountId, UserId>> accounts) {
        this.accounts = accounts;
    }

    /**
     * @return the ledger
     */
    public List<BCBlock> getLedger() {
        return ledger;
    }

    /**
     * @param ledger the ledger to set
     */
    public void setLedger(List<BCBlock> ledger) {
        this.ledger = ledger;
    }

    

}
