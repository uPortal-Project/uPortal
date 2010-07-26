package org.jasig.portal.permission.dao.jpa;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.jasig.portal.permission.IPermissionActivity;
import org.jasig.portal.permission.IPermissionOwner;

/**
 * PermissionOwnerImpl represents the default JPA implementation of 
 * IPermissionOwner.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 * @since 3.3
 */
@Entity
@Table(name = "UP_PERMISSION_OWNER")
@GenericGenerator(name = "UP_PERMISSION_OWNER_GEN", strategy = "native", parameters = {
		@Parameter(name = "sequence", value = "UP_PERMISSION_OWNER_SEQ"),
		@Parameter(name = "table", value = "UP_JPA_UNIQUE_KEY"),
		@Parameter(name = "column", value = "NEXT_UP_PERMISSION_OWNER__HI") })
public class PermissionOwnerImpl implements IPermissionOwner, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
	@GeneratedValue(generator = "UP_PERMISSION_OWNER_GEN")
    @Column(name = "OWNER_ID")
	private Long id;
	
    @Column(name = "OWNER_FNAME", length = 128, nullable = false, unique = true)
    private String fname;
    
    @Column(name = "OWNER_NAME", length = 128, nullable = false)
    private String name;
    
    @Column(name = "OWNER_DESCRIPTION", length = 255)
    private String description;

    @OneToMany(mappedBy = "owner", targetEntity = PermissionActivityImpl.class, fetch = FetchType.EAGER, cascade = { CascadeType.ALL })
    @JoinTable(name = "UP_PERMISSION_ACTIVITY", joinColumns = @JoinColumn(name = "OWNER_ID"))
    private Set<IPermissionActivity> activities = new HashSet<IPermissionActivity>();

    
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
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

    public String getOwnerName() {
        return this.name;
    }

    public String getOwnerToken() {
        return this.fname;
    }

    public Set<IPermissionActivity> getActivities() {
        return this.activities;
    }

    public void setActivities(Set<IPermissionActivity> activities) {
        this.activities = activities;
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof IPermissionOwner)) {
            return false;
        }

        IPermissionOwner owner = (IPermissionOwner) obj;
        return this.fname.equals(owner);
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
                .toString();
    }

}
