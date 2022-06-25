package tp2.bitdlp.pow.block;

import java.util.List;

import tp2.bitdlp.pow.transaction.LedgerTransaction;

public class BCBlock {

    protected BCBlockHeader header;

    protected List<LedgerTransaction> transactions;

    public BCBlock() {
    }

    public BCBlock(BCBlockHeader header, List<LedgerTransaction> transactions)
    {
        this.header = header;
        this.transactions = transactions;
    }

    public BCBlockHeader getHeader() {
        return header;
    }

    public void setHeader(BCBlockHeader header) {
        this.header = header;
    }

    public List<LedgerTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<LedgerTransaction> transactions) {
        this.transactions = transactions;
    }
}
