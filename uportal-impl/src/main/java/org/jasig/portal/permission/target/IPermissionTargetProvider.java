package org.jasig.portal.permission.target;

import java.util.Collection;
import java.util.Set;

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
     * Get the set of all known target keys valid for use as targets for 
     * this provider.
     * 
     * @return
     */
    public Set<String> getTargetKeys();
    
    /**
     * Get the set of all known targets valid for use as targets for this
     * provider. 
     * 
     * @return
     */
    public Collection<IPermissionTarget> getTargets();
    
    /**
     * Return the permission target associated with the specified key under
     * this provider.  If no target with the given key is valid for this 
     * provider, return <code>null</code>. 
     * 
     * @param key
     * @return
     */
    public IPermissionTarget getTarget(String key);

}
