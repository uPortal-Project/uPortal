package org.jasig.portal.permission.target;

/**
 * IPermissionTarget represents a valid target for a permission.  Examples
 * of permission targets might include a uPortal group or a static string.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 * @since 3.3
 */
public interface IPermissionTarget {
    
    /**
     * Get the key of this permission target.  This key
     * will be used as the actual target string of a permission assignment.
     * 
     * @return
     */
    public String getKey();
    
    /**
     * Get the human-readable name of this permission target.
     * 
     * @return
     */
    public String getName();
    
}
