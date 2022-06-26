package tp2.bitdlp.pow.merkletree;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import tp2.bitdlp.pow.transaction.LedgerTransaction;
import tp2.bitdlp.util.Crypto;

public class MerkleTree
{
    protected Node merkleRoot;

    // sempre par
    protected int size;

    /**
     * 
     */
    public MerkleTree() {
    }

    public MerkleTree(List<LedgerTransaction> transactions)
    {
        if (transactions.size() % 2 != 0)
            throw new IllegalArgumentException("Invalid number of transactions, must be even");
        
        this.merkleRoot = createTree(transactions);
        this.size = transactions.size();
    }

    /**
     * @return the merkleRoot
     */
    public Node getMerkleRoot() {
        return merkleRoot;
    }

    /**
     * @param merkleRoot the merkleRoot to set
     */
    public void setMerkleRoot(Node merkleRoot) {
        this.merkleRoot = merkleRoot;
    }

    /**
     * @return the size
     */
    public int getSize() {
        return size;
    }

    /**
     * @param size the size to set
     */
    public void setSize(int size) {
        this.size = size;
    }

    @JsonIgnore
    public List<LedgerTransaction> getTransactions()
    {
        return getTransactions(this.merkleRoot, new ArrayList<>(this.size));
    }

    @JsonIgnore
    public byte[] getMerkleRootHash()
    {
        return this.merkleRoot == null ? null : this.merkleRoot.getHash();
    }

    protected static Node createTree(List<LedgerTransaction> transactions)
    {
        List<Node> nodes = createLeafNodes(transactions);
        List<Node> parentNodes;
        Iterator<Node> it;

        while (nodes.size() >= 2)
        {
            parentNodes = new ArrayList<>(nodes.size() / 2);
            
            it = nodes.iterator();
            while (it.hasNext())
            {
                // tirar 2 nodes e calcular o parent
                // adicionar a parentNodes
                parentNodes.add(createParentNode(it.next(), it.next()));
            }
            
            // nodes -> calculated parentNodes
            nodes = parentNodes;
        }

        // nodes size == 1 -> merkle root
        return nodes.get(0);
    }

    protected static List<Node> createLeafNodes(List<LedgerTransaction> transactions)
    {
        List<Node> leafs = new ArrayList<>(transactions.size());
        MessageDigest md = Crypto.getSha256Digest();

        for (LedgerTransaction t : transactions) {
            md.update(t.digest());
            leafs.add(new LeafNode(md.digest(), t));
        }
        
        return leafs;
    }

    protected static Node createParentNode(Node left, Node right)
    {
        MessageDigest digest =  Crypto.getSha256Digest();

        digest.update(left.getHash());
        digest.update(right.getHash());
        byte[] firstHash = digest.digest();

        digest.update(firstHash);
        return new Node(digest.digest(), left, right);
    }

    // verify tree - hashes

    /* public boolean verifyTree(){
        Queue<Node> buff = new LinkedList<>();
        buff.add(merkleRoot);
        Node node;

        while(!buff.isEmpty()){
            node = buff.remove();

            if(!(node instanceof LeafNode)){

                if(!node.hash.equals(getChildrenHash(node))){
                    return false;
                }

                buff.add(node.left);
                buff.add(node.right);
            }
        }

        return true;
    }

    public String getChildrenHash(Node node){
        MessageDigest digest =  Crypto.getSha256Digest();
        digest.update(Utils.fromBase64(node.left.hash + node.right.hash));

        return Utils.toBase64(digest.digest());
    } */

    protected List<LedgerTransaction> getTransactions(Node node, List<LedgerTransaction> list){

        if(node == null)
            return list;
        
        if(node instanceof LeafNode){
            list.add(((LeafNode) node).getTransaction());
            return list;
        }

        getTransactions(node.left, list);
        getTransactions(node.right, list);

        return list;
    }
}
