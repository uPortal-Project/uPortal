/**
 * Copyright © 2004 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
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
    private static String minor = "4";
    private static String patch = "";
    private static String extra = "+";
    
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
