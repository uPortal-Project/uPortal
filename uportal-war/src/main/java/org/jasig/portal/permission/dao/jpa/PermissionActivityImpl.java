package org.jasig.portal.permission.dao.jpa;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.jasig.portal.permission.IPermissionActivity;
import org.jasig.portal.permission.IPermissionOwner;

/**
 * PermissionActivityImpl represents the default JPA implementation of 
 * IPermissionActivity.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 * @since 3.3
 */
@Entity
@Table(name = "UP_PERMISSION_ACTIVITY")
@GenericGenerator(name = "UP_PERMISSION_ACTIVITY_GEN", strategy = "native", parameters = {
        @Parameter(name = "sequence", value = "UP_PERMISSION_ACTIVITY_SEQ"),
        @Parameter(name = "table", value = "UP_JPA_UNIQUE_KEY"),
        @Parameter(name = "column", value = "NEXT_UP_PERMISSION_ACTIVITY__HI") })
public class PermissionActivityImpl implements IPermissionActivity, Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "UP_PERMISSION_ACTIVITY_GEN")
    @Column(name = "ACTIVITY_ID")
    private Long id;
    
    @Column(name = "ACTIVITY_FNAME", length = 128, nullable = false, unique = true)
    private String fname;
    
    @Column(name = "ACTIVITY_NAME", length = 128, nullable = false, unique = true)
    private String name;
    
    @Column(name = "ACTIVITY_DESCRIPTION", length = 255)
    private String description;

    @Column(name = "OWNER_TARGET_PROVIDER", length = 255, nullable = false)
    private String targetProviderKey;
    
    @ManyToOne(targetEntity = PermissionOwnerImpl.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "OWNER_ID", nullable = false, updatable = false)
    private final IPermissionOwner owner;

    /*
     * Internal, for hibernate
     */
    @SuppressWarnings("unused")
    private PermissionActivityImpl() {
        this.owner = null;
    }

    public PermissionActivityImpl(IPermissionOwner owner) {
        this.owner = owner;
    }
    
    public String getFname() {
        return this.fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTargetProviderKey() {
        return this.targetProviderKey;
    }

    public void setTargetProviderKey(String targetProviderKey) {
        this.targetProviderKey = targetProviderKey;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public IPermissionOwner getOwner() {
        return owner;
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof IPermissionActivity)) {
            return false;
        }

        IPermissionActivity activity = (IPermissionActivity) obj;
        return this.fname.equals(activity);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return new HashCodeBuilder(464270933, -1074792143).append(this.fname)
                .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", this.id)
                .append("fname", this.fname)
                .append("name", this.name)
                .append("description", this.description)
                .append("targetProviderKey", this.targetProviderKey)
                .toString();
    }

}
