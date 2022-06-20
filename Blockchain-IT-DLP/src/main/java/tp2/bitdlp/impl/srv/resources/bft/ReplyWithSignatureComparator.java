package tp2.bitdlp.impl.srv.resources.bft;

import java.util.Arrays;
import java.util.Comparator;

import com.fasterxml.jackson.core.type.TypeReference;

import tp2.bitdlp.util.Utils;

public class ReplyWithSignatureComparator implements Comparator<byte[]>
{

    @Override
    public int compare(byte[] o1, byte[] o2) {
        try {
            ReplyWithSignature<byte[]> reply1 = Utils.json.readValue(o1, new TypeReference<ReplyWithSignature<byte[]>>() {});
            ReplyWithSignature<byte[]> reply2 = Utils.json.readValue(o2, new TypeReference<ReplyWithSignature<byte[]>>() {});

            return Arrays.equals(reply1.getReply(), reply2.getReply()) ? 0 : -1;
        } catch (Exception e) {
            return -1;
        }
    }
    
}
