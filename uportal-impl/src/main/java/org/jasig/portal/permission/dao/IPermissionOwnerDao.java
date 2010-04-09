package org.jasig.portal.permission.dao;

import java.util.List;

import org.jasig.portal.permission.IPermissionActivity;
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
     * Retrieve the permission owner with the specified unique ID.
     * 
     * @param id
     * @return
     */
    public IPermissionOwner getPermissionOwner(Long id);

    /**
     * Retrieve the permission owner with the specified functional name.
     * 
     * @param fname
     * @return
     */
    public IPermissionOwner getPermissionOwner(String fname);

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
    
    /**
     * Retrieve a permission activity by unique ID.
     * 
     * @param id
     * @return
     */
    public IPermissionActivity getPermissionActivity(Long id);

    /**
     * Retrieve a permission activity under the specified permission owner
     * with the provided activity functional name.
     * 
     * @param ownerId        unique ID of the desired activity's owner
     * @param activityFname  functional name of the desired activity
     * @return
     */
    public IPermissionActivity getPermissionActivity(Long ownerId, String activityFname);
    
    /**
     * Retrieve a permission activity under the specified permission owner
     * with the provided activity functional name.
     * 
     * @param ownerFname     functional name of the desired activity's owner
     * @param activityFname  functional name of the activity itself
     * @return
     */
    public IPermissionActivity getPermissionActivity(String ownerFname, String activityFname);
    
    /**
     * Retrieve the permission activity associated with the supplied functional
     * name, under the specified permission owner.  If no matching activity 
     * can be found, create a new permission activity instance with the supplied 
     * owner and activity functional name.
     * 
     * 
     * @param owner  permission owner
     * @param fname  activity fname
     * @return
     */
    public IPermissionActivity getOrCreatePermissionActivity(IPermissionOwner owner, String fname);
    
    /**
     * Persist a permission activity to the data layer, creating or updating
     * the activity as appropriate.
     * 
     * @param activity
     * @return
     */
    public IPermissionActivity savePermissionActivity(IPermissionActivity activity);

}
