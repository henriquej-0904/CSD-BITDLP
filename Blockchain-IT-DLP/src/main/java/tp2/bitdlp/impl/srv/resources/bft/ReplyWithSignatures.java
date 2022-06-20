package tp2.bitdlp.impl.srv.resources.bft;

import java.util.LinkedList;
import java.util.List;

public class ReplyWithSignatures<T>
{
    private int statusCode;

    private T reply;

    private List<String> signatures;

    /**
     * 
     */
    public ReplyWithSignatures() {
    }

    /**
     * @param maxReplies
     */
    public ReplyWithSignatures(int statusCode, T reply)
    {
        this.statusCode = statusCode;
        this.reply = reply;
    }

    public void addSignature(String signature)
    {
        if (this.signatures == null)
            this.signatures = new LinkedList<>();
        
        this.signatures.add(signature);
    }

    /**
     * @return the numReplies
     */
    public int getNumReplies() {
        return signatures.size();
    }

    /**
     * @return the reply
     */
    public T getReply() {
        return reply;
    }

    /**
     * @return the signatures
     */
    public List<String> getSignatures() {
        return signatures;
    }

    /**
     * @return the statusCode
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * @param statusCode the statusCode to set
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * @param reply the reply to set
     */
    public void setReply(T reply) {
        this.reply = reply;
    }

    /**
     * @param signatures the signatures to set
     */
    public void setSignatures(List<String> signatures) {
        this.signatures = signatures;
    }

    
}
