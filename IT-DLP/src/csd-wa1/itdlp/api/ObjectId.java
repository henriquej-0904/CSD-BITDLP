package itdlp.api;

import java.security.PublicKey;
import java.util.Arrays;

public abstract class ObjectId {

    protected final byte[] id;

    /**
     * Create an ObjectId object from the specified id.
     * 
     * @param id The object id.
     */
    protected ObjectId(byte[] id) {
        if (id == null)
            throw new IllegalArgumentException();
        
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

    @Override
    public String toString() {
        return getClass().getSimpleName() + "=" + Arrays.toString(this.id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null || !(obj instanceof ObjectId))
            return false;

        ObjectId other = (ObjectId)obj;
        return Arrays.equals(getId(), other.getId());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getId());
    }

}

