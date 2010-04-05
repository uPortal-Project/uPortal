package org.jasig.portal.permission.target;

import java.io.Serializable;

/**
 * PermissionTargetImpl represents a simple default implementation of 
 * IPermissionTarget.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 * @since 3.3
 */
public class PermissionTargetImpl implements IPermissionTarget, Serializable {
    
    private String key;
    
    private String name;
    
    /**
     * Default constructor
     */
    public PermissionTargetImpl() { }
    
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
     * @see org.jasig.portal.permission.target.IPermissionTarget#setKey(java.lang.String)
     */
    public void setKey(String key) {
        this.key = key;
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.target.IPermissionTarget#getName()
     */
    public String getName() {
        return name;
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.target.IPermissionTarget#setName(java.lang.String)
     */
    public void setName(String name) {
        this.name = name;
    }
    
}
