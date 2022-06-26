package tp2.bitdlp.pow.merkletree;

import java.security.MessageDigest;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import tp2.bitdlp.pow.transaction.LedgerTransaction;
import tp2.bitdlp.util.Crypto;
import tp2.bitdlp.util.Utils;

public class MerkleTree
{
    protected Node merkleRoot;

    // sempre par
    protected int size;

    // verify tree - hashes

    public boolean verifyTree(){
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
    }

    // obter transactions

    public List<LedgerTransaction> getTransactions(Node node, List<LedgerTransaction> list){

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
