package tp2.bitdlp.impl.srv.resources.requests;

import tp2.bitdlp.pow.block.BCBlock;

public class ProposeMinedBlock extends Request {
    
    private String clientSignature;
    private BCBlock block;

    public ProposeMinedBlock() {
        super(Operation.PROPOSE_BLOCK_ASYNC);
    }

    public ProposeMinedBlock(String clientSignature, BCBlock block){
        super(Operation.PROPOSE_BLOCK_ASYNC);
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
    
}


