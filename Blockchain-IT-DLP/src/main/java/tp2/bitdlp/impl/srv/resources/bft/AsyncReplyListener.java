package tp2.bitdlp.impl.srv.resources.bft;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import bftsmart.communication.client.ReplyListener;
import bftsmart.tom.RequestContext;
import bftsmart.tom.core.messages.TOMMessage;

public class AsyncReplyListener implements ReplyListener {

    private int maxReplies;

    private List<ReplyWithSignatures> replies;

    private ReplyWithSignatures reply;

    /**
     * 
     */
    public AsyncReplyListener(int maxReplies)
    {
        this.maxReplies = maxReplies;
        this.replies = new LinkedList<>();
    }

    @Override
    public void replyReceived(RequestContext arg0, TOMMessage arg1) {
        byte[] reply = arg1.serializedMessage;
        byte[] signature = arg1.serializedMessageSignature;

        ReplyWithSignatures replyWithSignatures = null;

        Iterator<ReplyWithSignatures> it = replies.iterator();
        while (it.hasNext() && replyWithSignatures == null)
        {
            ReplyWithSignatures tmp = it.next();
            if (Arrays.equals(reply, tmp.getReply()))
                replyWithSignatures = tmp;
        }

        if (replyWithSignatures == null)
        {
            replyWithSignatures = new ReplyWithSignatures(maxReplies, reply);
            replies.add(replyWithSignatures);
        }

        replyWithSignatures.addSignature(signature);

        if (replyWithSignatures.hasConsensus())
        {
            this.reply = replyWithSignatures;
            this.notifyAll();
        }
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub
        
    }

    /**
     * @return the reply
     */
    public ReplyWithSignatures getReply() {
        return reply;
    }
    
}
