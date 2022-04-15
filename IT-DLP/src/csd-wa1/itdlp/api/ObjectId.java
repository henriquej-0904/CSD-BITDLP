package itdlp.api;

import java.security.PublicKey;

public abstract class ObjectId {

    protected final byte[] id;

    /**
     * Create an ObjectId object from the specified id.
     * 
     * @param id The object id.
     */
    protected ObjectId(byte[] id) {
        this.id = id;
    }

    /**
     * @return the id
     */
    public byte[] getId() {
        return id;
    }

    /**
     * Get the public key of this object id.
     * 
     * @return The public key.
     */
    public abstract PublicKey getPublicKey();
    
}

