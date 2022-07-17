package tp2.bitdlp.data.mongo;

import com.fasterxml.jackson.core.JsonProcessingException;

import tp2.bitdlp.pow.block.BCBlock;
import tp2.bitdlp.pow.block.BCBlockHeader;
import tp2.bitdlp.pow.merkletree.MerkleTree;
import tp2.bitdlp.util.Utils;

public class BCBlockDAO {
    private BCBlockHeader header;
    private String transactions;
    /**
     * 
     */
    public BCBlockDAO() {
    }
    /**
     * @param header
     * @param transactions
     */
    public BCBlockDAO(BCBlockHeader header, String transactions) {
        this.header = header;
        this.transactions = transactions;
    }

    
    public BCBlockDAO(BCBlock block) {
        this.header = block.getHeader();
        try {
            this.transactions = Utils.json.writeValueAsString(block.getTransactions());
        } catch (JsonProcessingException e) {}
    }

    /**
     * @return the header
     */
    public BCBlockHeader getHeader() {
        return header;
    }
    /**
     * @param header the header to set
     */
    public void setHeader(BCBlockHeader header) {
        this.header = header;
    }
    /**
     * @return the transactions
     */
    public String getTransactions() {
        return transactions;
    }
    /**
     * @param transactions the transactions to set
     */
    public void setTransactions(String transactions) {
        this.transactions = transactions;
    }

    public BCBlock toBlock()
    {
        MerkleTree transactions = null;
        try {
            transactions = Utils.json.readValue(this.transactions, MerkleTree.class);
        } catch (Exception e) {}

        return new BCBlock(header, transactions);
    }
}
