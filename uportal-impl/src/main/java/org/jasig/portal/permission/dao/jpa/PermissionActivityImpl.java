package org.jasig.portal.permission.dao.jpa;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Table;

import org.jasig.portal.permission.IPermissionActivity;

/**
 * PermissionActivityImpl represents the default JPA implementation of 
 * IPermissionActivity.
 * 
 * TODO: In the future, we may want to make this a Hibernate entity rather
 * than an embeddable.  
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 * @since 3.3
 */
@Embeddable
@Table(name = "UP_PERMISSION_ACTIVITY")
public class PermissionActivityImpl implements IPermissionActivity, Serializable {
    
    @Column(name = "ACTIVITY_FNAME", length = 128, nullable = false, unique = true)
    private String fname;
    
    @Column(name = "ACTIVITY_NAME", length = 128, nullable = false, unique = true)
    private String name;
    
    @Column(name = "ACTIVITY_DESCRIPTION", length = 255)
    private String description;

    @Column(name = "OWNER_TARGET_PROVIDER", length = 255, nullable = false)
    private String targetProvider;
    
    
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
        return this.targetProvider;
    }

    public void setTargetProviderKey(String targetProviderKey) {
        this.targetProvider = targetProviderKey;
    }

}
