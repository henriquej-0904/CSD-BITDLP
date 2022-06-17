package tp2.bitdlp.impl.srv.resources.bft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReplyWithSignatures
{
    private int maxReplies;

    private byte[] reply;

    private List<byte[]> signatures;

    /**
     * @param maxReplies
     */
    public ReplyWithSignatures(int maxReplies, byte[] reply)
    {
        this.maxReplies = maxReplies;
        this.reply = reply;
    }

    public void addSignature(byte[] signature)
    {
        if (this.signatures == null)
            this.signatures = new ArrayList<>(maxReplies);
        
        this.signatures.add(signature);
    }

    /**
     * @return the maxReplies
     */
    public int getMaxReplies() {
        return maxReplies;
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
    public byte[] getReply() {
        return reply;
    }

    /**
     * @return the signatures
     */
    public List<byte[]> getSignatures() {
        return signatures;
    }

    public boolean hasConsensus()
    {
        return signatures.size() >= maxReplies;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (!(obj instanceof ReplyWithSignatures))
            return false;

        return Arrays.equals(reply, ((ReplyWithSignatures)obj).reply);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(reply);
    }    
}
