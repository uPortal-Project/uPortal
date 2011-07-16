package org.jasig.portal.permission.target;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


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
     * @see org.jasig.portal.permission.target.IPermissionTargetProvider#searchTargets(java.lang.String)
     */
    public Collection<IPermissionTarget> searchTargets(String term) {
        
        // ensure that the search term is all lowercase to aid in string comparison
        term = term.toLowerCase();
        
        // initialize a new collection of matching targets
        Collection<IPermissionTarget> matching = new ArrayList<IPermissionTarget>();
        
        // iterate through each target, comparing it to the search term
        for (IPermissionTarget target : targetMap.values()) {
            // if the target's key or display name contains the search term,
            // count it as matching
            if (target.getKey().toLowerCase().contains(term)
                    || target.getName().toLowerCase().contains(term)) {
                matching.add(target);
            }
        }

        // return the list of matching targets
        return matching;
    }

}
