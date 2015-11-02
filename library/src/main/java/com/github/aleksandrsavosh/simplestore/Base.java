package com.github.aleksandrsavosh.simplestore;

import java.io.Serializable;
import java.util.Date;

/**
 * Base class of model
 * This class keep info about model
 * _id - this is unique local id
 * objectId - this is unique cloud id
 * createdAt - create date
 * updatedAt - update date
 *
 * equals and hashCode doesn't contain logic with localId and cloudId fields
 */
public abstract class Base implements Serializable {

    private Long localId;
    private String cloudId;
    private Date createdAt;
    private Date updatedAt;

    public Long getLocalId() {
        return localId;
    }

    public void setLocalId(Long localId) {
        this.localId = localId;
    }

    public String getCloudId() {
        return cloudId;
    }

    public void setCloudId(String cloudId) {
        this.cloudId = cloudId;
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

        if (createdAt != null ? !createdAt.equals(base.createdAt) : base.createdAt != null) return false;
        return !(updatedAt != null ? !updatedAt.equals(base.updatedAt) : base.updatedAt != null);

    }

    @Override
    public int hashCode() {
        int result = createdAt != null ? createdAt.hashCode() : 0;
        result = 31 * result + (updatedAt != null ? updatedAt.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Base{" +
                "localId=" + localId +
                ", cloudId='" + cloudId + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}

