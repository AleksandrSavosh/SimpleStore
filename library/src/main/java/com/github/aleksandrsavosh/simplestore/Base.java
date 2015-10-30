package com.github.aleksandrsavosh.simplestore;

import java.io.Serializable;
import java.util.Date;

/**
 * Base class of model
 * This class keep info about model
 * objectId - this is unique id of model
 * createdAt - create date
 * updatedAt - update date
 */
public abstract class Base implements Serializable {

    private String objectId;
    private Date createdAt;
    private Date updatedAt;

    public Base() {
    }

    public Base(String objectId, Date createdAt, Date updatedAt) {
        this.objectId = objectId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Base base = (Base) o;

        if (objectId != null ? !objectId.equals(base.objectId) : base.objectId != null) return false;
        if (createdAt != null ? !createdAt.equals(base.createdAt) : base.createdAt != null) return false;
        return !(updatedAt != null ? !updatedAt.equals(base.updatedAt) : base.updatedAt != null);

    }

    @Override
    public int hashCode() {
        int result = objectId != null ? objectId.hashCode() : 0;
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        result = 31 * result + (updatedAt != null ? updatedAt.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "{Class=Base" +
                ", objectId='" + objectId + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}

