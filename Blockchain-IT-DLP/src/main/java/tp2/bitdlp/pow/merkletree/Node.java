package tp2.bitdlp.pow.merkletree;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Node.class, name = "Node"),

    @JsonSubTypes.Type(value = LeafNode.class, name = "LeafNode") }
)
public class Node {

    // double SHA256
    protected byte[] hash;

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
    public Node(byte[] hash, Node left, Node right) {
        this.hash = hash;
        this.left = left;
        this.right = right;
    }

    /**
     * @return the hash
     */
    public byte[] getHash() {
        return hash;
    }

    /**
     * @param hash the hash to set
     */
    public void setHash(byte[] hash) {
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
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Node))
            return false;
        Node other = (Node) obj;
        return Arrays.equals(hash, other.hash);
    }
}
