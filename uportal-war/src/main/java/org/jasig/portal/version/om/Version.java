package org.jasig.portal.version.om;

/**
 * Describes a version number, based on http://apr.apache.org/versioning.html
 * <br/>
 * Versions MUST implement equality as checking if the Major, Minor and Patch versions ALL match
 * 
 * 
 * @author Eric Dalquist
 */
public interface Version {
    /**
     * @return The major part
     */
    int getMajor();
    
    /**
     * @return The minor part
     */
    int getMinor();
    
    /**
     * @return The patch part
     */
    int getPatch();
}
