package org.jasig.portal.permission.target;

import java.util.Collection;

/**
 * IPermissionTargetProviderRegistry provides a registry of target provider
 * instances.  This registry can be used to retrieve provider instances
 * associated with specified provider keys.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 * @since 3.3
 */
public interface IPermissionTargetProviderRegistry {
    
    /**
     * Register a new permission target provider under the specified key.
     * 
     * @param key
     * @param provider
     */
    public void registerTargetProvider(String key, IPermissionTargetProvider provider);
    
    /**
     * Retrieve a permission target provider instance for the given key.  If
     * no provider can be located matching the given key, this method will
     * return <code>null</code>. 
     * 
     * @param key
     * @return
     */
    public IPermissionTargetProvider getTargetProvider(String key);

    /**
     * Get the collection of all registered target providers.
     * 
     * @return
     */
    public Collection<IPermissionTargetProvider> getTargetProviders();

}
