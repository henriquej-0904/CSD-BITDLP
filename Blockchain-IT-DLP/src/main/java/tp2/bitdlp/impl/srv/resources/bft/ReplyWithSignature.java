package tp2.bitdlp.impl.srv.resources.bft;



public class ReplyWithSignature<T>
{
    private int statusCode;

    private T reply;

    private String signature;

    /**
     * @param reply
     * @param signature
     */
    public ReplyWithSignature(int statusCode, T reply, String signature) {
        this.statusCode = statusCode;
        this.reply = reply;
        this.signature = signature;
    }

    /**
     * 
     */
    public ReplyWithSignature() {
    }

    /**
     * @return the reply
     */
    public T getReply() {
        return reply;
    }

    /**
     * @param reply the reply to set
     */
    public void setReply(T reply) {
        this.reply = reply;
    }

    /**
     * @return the signature
     */
    public String getSignature() {
        return signature;
    }

    /**
     * @param signature the signature to set
     */
    public void setSignature(String signature) {
        this.signature = signature;
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
}
