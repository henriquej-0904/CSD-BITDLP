package tp2.bitdlp.pow.merkletree;

public class Node {

    // double SHA256
    protected String hash;

    protected Node left, right;

    /**
     * 
     */
    public Node() {
    }

    /**
     * @param hash
     * @param left
     * @param right
     */
    public Node(String hash, Node left, Node right) {
        this.hash = hash;
        this.left = left;
        this.right = right;
    }

    /**
     * @return the hash
     */
    public String getHash() {
        return hash;
    }

    /**
     * @param hash the hash to set
     */
    public void setHash(String hash) {
        this.hash = hash;
    }

    /**
     * @return the left
     */
    public Node getLeft() {
        return left;
    }

    /**
     * @param left the left to set
     */
    public void setLeft(Node left) {
        this.left = left;
    }

    /**
     * @return the right
     */
    public Node getRight() {
        return right;
    }

    /**
     * @param right the right to set
     */
    public void setRight(Node right) {
        this.right = right;
    }
}
