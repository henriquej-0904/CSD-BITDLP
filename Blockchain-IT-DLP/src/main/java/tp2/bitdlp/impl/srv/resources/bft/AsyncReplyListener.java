package tp2.bitdlp.impl.srv.resources.bft;

import bftsmart.communication.client.ReplyListener;
import bftsmart.tom.RequestContext;
import bftsmart.tom.core.messages.TOMMessage;

public class AsyncReplyListener implements ReplyListener {

    private byte[] reply;

    private byte[][] signatures;

    /**
     * 
     */
    public AsyncReplyListener(int numReplies) {
    }

    @Override
    public void replyReceived(RequestContext arg0, TOMMessage arg1) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub
        
    }

    /**
     * @return the reply
     */
    public byte[] getReply() {
        return reply;
    }

    /**
     * @return the signatures
     */
    public byte[][] getSignatures() {
        return signatures;
    }    
    
}
