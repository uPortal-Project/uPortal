/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import org.jasig.portal.security.IPermission;
import org.jasig.portal.tools.versioning.VersionsManager;

/**
 * Contains version information about the current release.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 * @deprecated use VersionManager with fname UP_FRAMEWORK instead.
 */
public class Version {

    private static String product = "uPortal";

    private static final org.jasig.portal.tools.versioning.Version uPVersion =
        VersionsManager.getInstance().getVersion(IPermission.PORTAL_FRAMEWORK);

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
        return Integer.toString(uPVersion.getMajor());
    }

    /**
     * Returns the minor version.
     * For example, this would return <code>3</code> for uPortal 2.3.4.
     * @return the minor version
     */
    public static String getMinor() {
        return Integer.toString(uPVersion.getMinor());
    }

    /**
     * Returns the patch version.
     * For example, this would return <code>4</code> for uPortal 2.3.4.
     * This method may return an empty String.
     * @return the patch version
     */
    public static String getPatch() {
        return Integer.toString(uPVersion.getMicro());
    }

    /**
     * Previously, returned the security version.
     * For example, this would return <code>1</code> for uPortal 2.3.4.1.
     * Now, this method always returns the String "unknown".  Security
     * version number is not supported by VersionsManager, upon which
     * uPortal has standardized since the inception of this class.  This class
     * is deprecated and will be removed in a future uPortal release.
     * @return "unknown"
     */
    public static String getSecurity() {
        return "unknown";
    }

    /**
     * Previously, returned any extra string used to construct this version.
     * For example, this would return <code>+</code> for uPortal 2.3.4+.
     * A plus sign is used to denote that the code is between releases,
     * the head of a branch in CVS.
     * Now, this method always returns the String "unkown".   Extra information
     * beyond dotted triple version number is not supported by
     * VersionsManager, upon which
     * uPortal has standardized since the inception of this class.  This class
     * is deprecated and will be removed in a future uPortal release.
     * @return "unknown"
     */
    public static String getExtra() {
        return "unknown";
    }

    /**
     * Returns the release tag in the CVS repository
     * corresponding to the current code.
     * For example, <code>rel-2-3-4</code>.
     * @return the release tag matching the running code
     */
    public static String getReleaseTag() {
        return "rel-" + uPVersion.getMajor() + "-" + uPVersion.getMinor() + "-" + uPVersion.getMicro();
    }

    /**
     * Returns the version of the current code.
     * For example, <code>2.3.4</code>.
     * @return the current version of the running code
     */
    public static String getVersion() {
        return uPVersion.dottedTriple();
    }

    /**
     * Returns a display-friendly string representing the
     * current product and version.
     * For example, <code>uPortal 2.3.4</code>.
     * @return a verison string suitable for display
     */
    public static String getProductAndVersion() {
        return product + " " + getVersion();
    }

}
