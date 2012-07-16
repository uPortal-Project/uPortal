package org.jasig.portal.version.dao;

import org.jasig.portal.version.om.Version;

/**
 * Access information about version numbers for various parts of uPortal
 * 
 * @author Eric Dalquist
 */
public interface VersionDao {
    /**
     * Get the version information for the specified product, returns null if no version is set.
     */
    Version getVersion(String product);
    
    /**
     * Create or update a the version number for the specified product
     */
    Version setVersion(String product, int major, int minor, int patch);
}
