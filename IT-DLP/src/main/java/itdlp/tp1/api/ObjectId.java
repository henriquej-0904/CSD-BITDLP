package itdlp.tp1.api;

import java.io.Serializable;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonIgnore;

import itdlp.tp1.util.Utils;

public abstract class ObjectId implements Comparable<ObjectId>, Serializable {

    protected byte[] id;

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
     * 
     */
    protected ObjectId() {
    }

    /**
     * @param id the id to set
     */
    public void setId(byte[] id) {
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
    @JsonIgnore
    public abstract PublicKey getPublicKey() throws InvalidKeySpecException;

    @Override
    public String toString() {
        return getClass().getSimpleName() + "=" + Utils.toHex(getId());
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

    @Override
    public int compareTo(ObjectId o) {
        if (this == o)
            return 0;

        return Arrays.compare(getId(), o.getId());
    }

}

