package org.jasig.portal.permission.target;

import java.util.Collection;

/**
 * IPermissionTargetProvider provides an interface for retrieving and validating
 * potential targets for a permission activity.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 * @since 3.3
 */
public interface IPermissionTargetProvider {
    
    /**
     * Return the permission target associated with the specified key under
     * this provider.  If no target with the given key is valid for this 
     * provider, return <code>null</code>. 
     * 
     * @param key
     * @return
     */
    public IPermissionTarget getTarget(String key);
    
    /**
     * Search this provider for a particular target using a single string
     * search term.  Each target provider implementation is responsible for
     * determining the definition a "matching" target.
     * 
     * @param term  search term
     * @return      collection of matching targets
     */
    public Collection<IPermissionTarget> searchTargets(String term);

}
