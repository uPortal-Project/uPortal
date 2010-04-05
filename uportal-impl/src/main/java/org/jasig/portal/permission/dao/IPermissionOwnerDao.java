package org.jasig.portal.permission.dao;

import java.util.List;

import org.jasig.portal.permission.IPermissionOwner;

/**
 * IPermissionOwnerDao represents an interface for retrieving and persisting
 * permission owners.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 * @since 3.3
 */
public interface IPermissionOwnerDao {

    /**
     * Retrieve the permission owner associated with the supplied functional
     * name.  If no matching permission owner can be found, create a new
     * permission owner instance with the supplied functional name.
     * 
     * @param fname functional name of the desired permission owner
     * @return      
     */
    public IPermissionOwner getOrCreatePermissionOwner(String fname);

    /**
     * Retrieve a list of all known permission owners from the data store.
     * 
     * @return
     */
    public List<IPermissionOwner> getAllPermissionOwners();
    
    /**
     * Persist a permission owner to the data layer, creating or updating
     * the owner as appropriate.
     * 
     * @param owner
     * @return
     */
    public IPermissionOwner saveOwner(IPermissionOwner owner);

}
