package org.jasig.portal.permission.target;

import java.io.Serializable;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * PermissionTargetImpl represents a simple default implementation of 
 * IPermissionTarget.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 * @since 3.3
 */
public class PermissionTargetImpl implements IPermissionTarget, Comparable<IPermissionTarget>, Serializable {
    
    private static final long serialVersionUID = 1L;

    private final String key;
    
    private final String name;
    
    /**
     * Construct a new PermissionTargetImpl with the specified key and 
     * human-readable name.
     * 
     * @param key
     * @param name
     */
    public PermissionTargetImpl(String key, String name) {
        this.key = key;
        this.name = name;
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.target.IPermissionTarget#getKey()
     */
    public String getKey() {
        return key;
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.target.IPermissionTarget#getName()
     */
    public String getName() {
        return name;
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof IPermissionTarget)) {
            return false;
        }

        IPermissionTarget target = (IPermissionTarget) obj;
        return new EqualsBuilder()
            .append(this.key, target.getKey())
            .append(this.name, target.getName())
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(464270933, -1074792143)
                .append(this.key).append(this.name)
                .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("key", this.key)
                .append("name", this.name)
                .toString();
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(IPermissionTarget target) {
        return new CompareToBuilder()
            .append(this.name, target.getName())
            .append(this.key, target.getKey())
            .toComparison();
    }

}
