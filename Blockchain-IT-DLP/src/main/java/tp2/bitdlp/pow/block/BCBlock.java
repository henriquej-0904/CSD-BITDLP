package tp2.bitdlp.pow.block;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;

import tp2.bitdlp.pow.merkletree.MerkleTree;
import tp2.bitdlp.pow.transaction.LedgerTransaction;
import tp2.bitdlp.util.Crypto;
import tp2.bitdlp.util.Utils;

public class BCBlock {

    protected static final String GENESYS_BLOC_PREVIOUS_HASH = "GENESYS BLOCK!!! HERE WE GO!!!!!";

    protected BCBlockHeader header;

    protected MerkleTree transactions;

    public BCBlock() {
    }

    public BCBlock(BCBlockHeader header, MerkleTree transactions)
    {
        this.header = header;
        this.transactions = transactions;
    }

    /**
     * Create a new block with the specified list of transactions.
     * The previousHash field must be defined before returning to miners.
     * 
     * @param transactions The list of transactions of the new block.
     * 
     * @return A new Block without the previousHash field.
     */
    public static BCBlock createBlock(List<LedgerTransaction> transactions)
    {
        if (transactions.isEmpty())
            throw new IllegalArgumentException("List of transactions is empty.");

        MerkleTree transactionsTree = new MerkleTree(transactions);
        return new BCBlock(
            new BCBlockHeader(null, Utils.toHex(transactionsTree.getMerkleRootHash())),
            transactionsTree);
    }

    /**
     * Create a genesis bloc,
     * 
     * @param transaction The transaction for the first miner.
     * 
     * @return A new Block without the previousHash field.
     */
    public static BCBlock createGenesisBlock(LedgerTransaction transaction)
    {
        MerkleTree transactionsTree = new MerkleTree(List.of(transaction));
        return new BCBlock(
            new BCBlockHeader(GENESYS_BLOC_PREVIOUS_HASH,
            Utils.toHex(transactionsTree.getMerkleRootHash())),
            transactionsTree);
    }

    public static boolean isGenesisBlock(BCBlock block)
    {
        return GENESYS_BLOC_PREVIOUS_HASH.equals(block.getHeader().getPreviousHash())
            && block.transactions.getTransactions().size() == 1;
    }

    public BCBlockHeader getHeader() {
        return header;
    }

    public void setHeader(BCBlockHeader header) {
        this.header = header;
    }

    public MerkleTree getTransactions() {
        return transactions;
    }

    public void setTransactions(MerkleTree transactions) {
        this.transactions = transactions;
    }

    public byte[] digest()
    {
        MessageDigest md = Crypto.getSha256Digest();
        try {
            md.update(Utils.json.writeValueAsBytes(this));;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        
        return md.digest();
    }

    @JsonIgnore
    public boolean isBlockMined()
    {
        byte[] digest = digest();
        int val = ByteBuffer.wrap(digest).getInt();
        return Integer.toUnsignedLong(val) < Integer.toUnsignedLong(getHeader().getDiffTarget());
    }
}
