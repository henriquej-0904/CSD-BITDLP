package tp2.bitdlp.impl.srv.resources.requests;

import tp2.bitdlp.pow.block.BCBlock;

public class ProposeMinedBlock extends Request {
    
    private String minerId;
    private String clientSignature;
    private BCBlock block;

    public ProposeMinedBlock() {
        super(Operation.PROPOSE_BLOCK);
    }

    public ProposeMinedBlock(String minerId, String clientSignature, BCBlock block){
        super(Operation.PROPOSE_BLOCK);
        this.minerId = minerId;
        this.clientSignature = clientSignature;
        this.block = block;
    }

    /**
     * @return the clientSignature
     */
    public String getClientSignature() {
        return clientSignature;
    }

    /**
     * @param clientSignature the clientSignature to set
     */
    public void setClientSignature(String clientSignature) {
        this.clientSignature = clientSignature;
    }

    /**
     * @return the block
     */
    public BCBlock getBlock() {
        return block;
    }

    /**
     * @param block the block to set
     */
    public void setBlock(BCBlock block) {
        this.block = block;
    }

    /**
     * @return the minerId
     */
    public String getMinerId() {
        return minerId;
    }

    /**
     * @param minerId the minerId to set
     */
    public void setMinerId(String minerId) {
        this.minerId = minerId;
    }
    
    
}


