package tp2.bitdlp.impl.srv.resources.bft.bftsmart;

import java.util.Arrays;
import java.util.Comparator;

import tp2.bitdlp.util.reply.ReplyWithSignature;
import tp2.bitdlp.util.Utils;

public class ReplyWithSignatureComparator implements Comparator<byte[]>
{

    @Override
    public int compare(byte[] o1, byte[] o2) {
        try {
            ReplyWithSignature reply1 = Utils.json.readValue(o1, ReplyWithSignature.class);
            ReplyWithSignature reply2 = Utils.json.readValue(o2, ReplyWithSignature.class);

            return Arrays.equals(reply1.getReply(), reply2.getReply()) ? 0 : -1;
        } catch (Exception e) {
            return -1;
        }
    }
    
}
