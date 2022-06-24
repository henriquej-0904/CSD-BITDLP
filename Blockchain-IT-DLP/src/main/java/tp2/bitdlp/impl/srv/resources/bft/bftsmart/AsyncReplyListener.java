package tp2.bitdlp.impl.srv.resources.bft.bftsmart;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import bftsmart.communication.client.ReplyListener;
import bftsmart.tom.RequestContext;
import bftsmart.tom.core.messages.TOMMessage;
import tp2.bitdlp.util.reply.ReplyWithSignature;
import tp2.bitdlp.util.reply.ReplyWithSignatures;
import tp2.bitdlp.util.Utils;

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
    public void replyReceived(RequestContext arg0, TOMMessage arg1)
    {
        if (this.reply != null)
            return;

        byte[] replyBytes = arg1.getContent();

        try {
            ReplyWithSignature reply = Utils.json.readValue(replyBytes,
                ReplyWithSignature.class);

            if (reply.getSignature() == null || reply.getSignature().isEmpty())
                return;

            ReplyWithSignatures replyWithSignatures = null;

            Iterator<ReplyWithSignatures> it = replies.iterator();
            while (it.hasNext() && replyWithSignatures == null) {
                ReplyWithSignatures tmp = it.next();
                if (reply.getStatusCode() == tmp.getStatusCode()
                    && Arrays.equals(reply.getReply(), tmp.getReply()))
                    replyWithSignatures = tmp;
            }

            if (replyWithSignatures == null) {
                replyWithSignatures = new ReplyWithSignatures(reply.getStatusCode(), reply.getReply());
                replies.add(replyWithSignatures);
            }

            replyWithSignatures.addSignature(reply.getReplicaId(), reply.getSignature());

            // reply processed!

            if (hasConsensus(replyWithSignatures))
                this.reply = replyWithSignatures;

        } catch (Exception e) {
            return;
        }
    }

    protected boolean hasConsensus(ReplyWithSignatures reply)
    {
        return reply.getNumReplies() >= maxReplies;
    }

    @Override
    public void reset() {
        this.replies.clear();
        this.reply = null;
    }

    /**
     * @return the reply
     */
    public ReplyWithSignatures getReply() {
        return reply;
    }
    
}
