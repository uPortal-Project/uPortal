package org.jasig.portal.permission.target;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * SimpleStringTargetProviderImpl provides a basic target provider implementation
 * capable of registering static strings as targets.  This implementation is
 * appropriate for permission owners for which targets are simple static 
 * strings that are well-defeined and known in advance.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 * @since 3.3
 */
public class SimpleStringTargetProviderImpl implements IPermissionTargetProvider, Serializable {
    
    public Map<String, IPermissionTarget> targetMap = new HashMap<String, IPermissionTarget>();
    
    /**
     * Add a permission target to this target provider.
     * 
     * @param target
     */
    public void addTarget(IPermissionTarget target) {
        targetMap.put(target.getKey(), target);
    }
    
    /**
     * Set the permission targets for this provider.
     * 
     * @param targets  collection of targets
     */
    public void setTargets(Collection<IPermissionTarget> targets) {

        // clear out any existing targets
        targetMap.clear();
        
        // add each target to the internal map and index it by the target key 
        for (IPermissionTarget target : targets) {
            targetMap.put(target.getKey(), target);
        }
        
    }
    
    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.target.IPermissionTargetProvider#getTarget(java.lang.String)
     */
    public IPermissionTarget getTarget(String key) {
        return targetMap.get(key);
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.target.IPermissionTargetProvider#getTargetKeys()
     */
    public Set<String> getTargetKeys() {
        return targetMap.keySet();
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.target.IPermissionTargetProvider#getTargets()
     */
    public Collection<IPermissionTarget> getTargets() {
        return targetMap.values();
    }

}
