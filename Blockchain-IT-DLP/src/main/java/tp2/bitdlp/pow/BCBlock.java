package tp2.bitdlp.pow;

import java.util.List;

import tp2.bitdlp.api.operations.LedgerTransaction;

public class BCBlock {

    private BCBlockHeader header;

    private List<LedgerTransaction> data;

    public BCBlock() {
    }

    public BCBlock(List<LedgerTransaction> data, String previousHash, int version, String merkelRoot, int nonce, int timeStamp, int difTarget) {
        this.data = data;
        header = new BCBlockHeader(previousHash, version, merkelRoot, nonce
                                , (int) (System.currentTimeMillis()/1000), difTarget);
    }

    public BCBlockHeader getHeader() {
        return header;
    }

    public void setHeader(BCBlockHeader header) {
        this.header = header;
    }

    public List<LedgerTransaction> getData() {
        return data;
    }

    public void setData(List<LedgerTransaction> data) {
        this.data = data;
    }
}
