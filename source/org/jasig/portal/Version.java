/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

/**
 * Contains version information about the current release.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class Version {
    
    // Update these strings appropriately for each release.
    // Use empty strings rather than null when value is not desired.
    private static String product = "uPortal";
    private static String major = "2";
    private static String minor = "5";
    private static String patch = "0";
    private static String extra = "M1";
    
    private static String releaseTag;
    private static String version;
    
    static {
        // Construct version
        releaseTag = "rel-" + major + "-" + minor;
        if (patch != null && patch.length() > 0) {
            releaseTag += "-" + patch;
        }
        releaseTag += extra;
        
        // Construct version for display
        version = major + "." + minor;
        if (patch != null && patch.length() > 0) {
            version += "." + patch;
        }
        version += extra;
    }
    
    /**
     * Returns the product name.
     * For example, this would return <code>uPortal</code> for uPortal 2.3.4.
     * @return the product name
     */
    public static String getProduct() {
        return product;
    }
    
    /**
     * Returns the major version.
     * For example, this would return <code>2</code> for uPortal 2.3.4.
     * @return the major version
     */
    public static String getMajor() {
        return major;
    }
    
    /**
     * Returns the minor version.  
     * For example, this would return <code>3</code> for uPortal 2.3.4.
     * @return the minor version
     */
    public static String getMinor() {
        return minor;
    }
        
    /**
     * Returns the patch version.  
     * For example, this would return <code>4</code> for uPortal 2.3.4.
     * This method may return an empty String.
     * @return the patch version
     */
    public static String getPatch() {
        return patch;
    }
    
    /**
     * Returns any extra string used to construct this version.
     * For example, this would return <code>+</code> for uPortal 2.3.4+.
     * A plus sign is used to denote that the code is between releases,
     * the head of a branch in CVS.  This method may return an empty String.
     * @return the extra string, if any
     */
    public static String getExtra() {
        return extra;
    }
    
    /**
     * Returns the release tag in the CVS repository
     * corresponding to the current code.
     * For example, <code>rel-2-3-4</code>.
     * @return the release tag matching the running code
     */
    public static String getReleaseTag() {
        return releaseTag;
    }
    
    /**
     * Returns the version of the current code.
     * For example, <code>2.3.4</code>.
     * @return the current version of the running code
     */
    public static String getVersion() {
        return version;
    }
    
    /**
     * Returns a display-friendly string representing the
     * current product and version.
     * For example, <code>uPortal 2.3.4</code>.
     * @return a verison string suitable for display
     */
    public static String getProductAndVersion() {
        return product + " " + version;
    }

}
