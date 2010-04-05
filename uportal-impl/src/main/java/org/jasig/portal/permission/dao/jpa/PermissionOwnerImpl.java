package org.jasig.portal.permission.dao.jpa;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Table;

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

    @org.hibernate.annotations.CollectionOfElements(fetch = FetchType.EAGER, targetElement = PermissionActivityImpl.class)
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

}
