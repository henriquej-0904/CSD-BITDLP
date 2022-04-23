package itdlp.tp1.util;

import java.io.Serializable;

public class Pair<K, V> implements Serializable
{
    private static final long serialVersionUID = 222334L;

    private K left;
    private V right;

    
    /**
     * @param left
     * @param right
     */
    public Pair(K left, V right) {
        this.left = left;
        this.right = right;
    }


    /**
     * 
     */
    public Pair() {
    }


    /**
     * @return the left
     */
    public K getLeft() {
        return left;
    }


    /**
     * @param left the left to set
     */
    public void setLeft(K left) {
        this.left = left;
    }


    /**
     * @return the right
     */
    public V getRight() {
        return right;
    }


    /**
     * @param right the right to set
     */
    public void setRight(V right) {
        this.right = right;
    }

    
}
