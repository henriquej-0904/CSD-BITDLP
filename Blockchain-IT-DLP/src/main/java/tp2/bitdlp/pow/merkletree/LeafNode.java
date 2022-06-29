package tp2.bitdlp.pow.merkletree;

import org.bson.codecs.pojo.annotations.BsonDiscriminator;

import tp2.bitdlp.pow.transaction.LedgerTransaction;

@BsonDiscriminator(value = "LeafNode", key = "_cls")
public class LeafNode extends Node
{
    protected LedgerTransaction transaction;

    /**
     * 
     */
    public LeafNode() {
    }


    /**
     * @param transaction
     */
    public LeafNode(byte[] hash, LedgerTransaction transaction)
    {
        super(hash, null, null);
        this.transaction = transaction;
    }


    /**
     * @return the transaction
     */
    public LedgerTransaction getTransaction() {
        return transaction;
    }

    /**
     * @param transaction the transaction to set
     */
    public void setTransaction(LedgerTransaction transaction) {
        this.transaction = transaction;
    }
}
