package tp2.bitdlp.api;

import java.io.Serializable;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonIgnore;

import tp2.bitdlp.util.Utils;

public abstract class ObjectId implements Comparable<ObjectId>, Serializable {

    protected byte[] objectId;

    /**
     * Create an ObjectId object from the specified id.
     * 
     * @param id The object id.
     */
    protected ObjectId(byte[] id) {
        if (id == null)
            throw new IllegalArgumentException();
        
        this.objectId = id;
    }

    /**
     * 
     */
    protected ObjectId() {
    }

    /**
     * @param id the id to set
     */
    public void setObjectId(byte[] id) {
        this.objectId = id;
    }

    /**
     * @return the id
     */
    public byte[] getObjectId() {
        return objectId;
    }

    /**
     * Get the public key of this object id.
     * 
     * @return The public key.
     */
    @JsonIgnore
    public abstract PublicKey publicKey() throws InvalidKeySpecException;

    @Override
    public String toString() {
        return getClass().getSimpleName() + "=" + Utils.toHex(getObjectId());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null || !(obj instanceof ObjectId))
            return false;

        ObjectId other = (ObjectId)obj;
        return Arrays.equals(getObjectId(), other.getObjectId());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getObjectId());
    }

    @Override
    public int compareTo(ObjectId o) {
        if (this == o)
            return 0;

        return Arrays.compare(getObjectId(), o.getObjectId());
    }

}

