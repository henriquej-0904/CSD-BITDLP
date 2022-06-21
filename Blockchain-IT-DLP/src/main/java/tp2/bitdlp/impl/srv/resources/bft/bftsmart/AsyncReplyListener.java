package tp2.bitdlp.impl.srv.resources.bft.bftsmart;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;

import bftsmart.communication.client.ReplyListener;
import bftsmart.tom.RequestContext;
import bftsmart.tom.core.messages.TOMMessage;
import tp2.bitdlp.impl.srv.resources.bft.ReplyWithSignature;
import tp2.bitdlp.impl.srv.resources.bft.ReplyWithSignatures;
import tp2.bitdlp.util.Utils;

public class AsyncReplyListener implements ReplyListener {

    private int maxReplies;

    private List<ReplyWithSignatures<byte[]>> replies;

    private ReplyWithSignatures<byte[]> reply;

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
        byte[] replyBytes = arg1.getContent();

        try {
            ReplyWithSignature<byte[]> reply = Utils.json.readValue(replyBytes,
                    new TypeReference<ReplyWithSignature<byte[]>>() {
                    });

            if (reply.getSignature() == null || reply.getSignature().isEmpty())
                return;

            // TODO: verify signature!

            ReplyWithSignatures<byte[]> replyWithSignatures = null;

            Iterator<ReplyWithSignatures<byte[]>> it = replies.iterator();
            while (it.hasNext() && replyWithSignatures == null) {
                ReplyWithSignatures<byte[]> tmp = it.next();
                if (reply.getStatusCode() == tmp.getStatusCode()
                    && Arrays.equals(reply.getReply(), tmp.getReply()))
                    replyWithSignatures = tmp;
            }

            if (replyWithSignatures == null) {
                replyWithSignatures = new ReplyWithSignatures<byte[]>(reply.getStatusCode(), reply.getReply());
                replies.add(replyWithSignatures);
            }

            replyWithSignatures.addSignature(reply.getSignature());

            // reply processed!

            if (hasConsensus(replyWithSignatures))
                this.reply = replyWithSignatures;

        } catch (Exception e) {
            return;
        }
    }

    protected boolean hasConsensus(ReplyWithSignatures<byte[]> reply)
    {
        return reply.getNumReplies() >= maxReplies;
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub
        
    }

    /**
     * @return the reply
     */
    public ReplyWithSignatures<byte[]> getReply() {
        return reply;
    }
    
}
