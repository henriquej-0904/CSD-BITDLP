package tp2.bitdlp.impl.srv.resources.requests;

public class GetBlock extends Request
{
    String minerAccountId;

    public GetBlock() {
        super(Operation.GET_BLOCK);
    }

    public GetBlock(String minerAccountId)
    {
        super(Operation.GET_BLOCK);
        this.minerAccountId = minerAccountId;
    }

    /**
     * @return the minerAccountId
     */
    public String getMinerAccountId() {
        return minerAccountId;
    }

    /**
     * @param minerAccountId the minerAccountId to set
     */
    public void setMinerAccountId(String minerAccountId) {
        this.minerAccountId = minerAccountId;
    }

    
}


