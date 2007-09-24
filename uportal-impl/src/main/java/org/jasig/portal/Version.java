/* Copyright 2004 2006 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.tools.versioning.VersionsManager;

/**
 * Contains version information about the current release.
 * This implementation uses static fields and static initializers to pre-compute
 * once the return values for all String-returning methods so as to avoid
 * String concatenation induced object churn at runtime.  This adds a moderate
 * amount of code complexity in exchange for non-impactful memory utilization
 * behaviors in the case where this class is invoked in a tight loop, as when
 * it is called on every portal render informing the theme transform.
 *
 * This class is deprecated so these heroics are likely misapplied.  Use
 * VersionManager instead.
 *
 * @author Ken Weiner, kweiner@unicon.net
 * @author Bernie Durfee, bernard.durfee@suny.edu
 * @version $Revision$
 * @deprecated use VersionManager with fname UP_FRAMEWORK instead.
 */
public class Version {

    private static final Log LOG = LogFactory.getLog(org.jasig.portal.Version.class);

    private static String product = "uPortal";

    private static org.jasig.portal.tools.versioning.Version uPVersion = null;

    private static String majorVersion = "unknown";

    private static String minorVersion = "unknown";

    private static String microVersion = "unknown";

    private static String releaseTag = "unknown";

    private static String dottedTriple = "unknown";

    private static String productAndVersion = "unknown";

    static {
        // try blocks in static initializer to ensure that this class will statically
        // initialize and report unknown versions rather than failing to initialize at all.

        try {
            uPVersion = VersionsManager.getInstance().getVersion(IPermission.PORTAL_FRAMEWORK);

            try {
                majorVersion = Integer.toString(uPVersion.getMajor());
            } catch (Exception e) {
                LOG.error("Error computing major version of uPortal.", e);
            }


            try {
                minorVersion = Integer.toString(uPVersion.getMinor());
            } catch (Exception e) {
                LOG.error("Error computing minor version of uPortal.", e);
            }


            try {
                microVersion = Integer.toString(uPVersion.getMicro());
            } catch (Exception e) {
                LOG.error("Error computing micro version of uPortal.", e);
            }

            releaseTag = "rel-" + majorVersion + "-" + minorVersion + "-" + microVersion;

            try {
                dottedTriple = uPVersion.dottedTriple();
            } catch (Exception e) {
                LOG.error("Error computing dotted triple representation of uPortal version.", e);
            }



            productAndVersion = product + " " + dottedTriple;


        } catch (Exception e) {
            // if getting the version from VersionsManager fails,
            // populating the dependent String static fields of this class will
            // also fail, so this static initializer doesn't bother trying in that case
            LOG.error("Error getting version of " + IPermission.PORTAL_FRAMEWORK + " from VersionsManager.", e);
        }


    }

    /**
     * Returns the product name.
     * For example, this would return <code>uPortal</code> for uPortal 2.3.4.
     * If the product name is unknown this method returns the String "unknown".
     * @return the product name
     */
    public static String getProduct() {
        return product;
    }

    /**
     * Returns the major version.
     * For example, this would return <code>2</code> for uPortal 2.3.4.
     * If the product major version is unknown this method returns the String "unknown".
     * @return the major version
     */
    public static String getMajor() {
        return majorVersion;
    }

    /**
     * Returns the minor version.
     * For example, this would return <code>3</code> for uPortal 2.3.4.
     * If the product minor version is unknown this method returns the String "unknown".
     * @return the minor version
     */
    public static String getMinor() {
        return minorVersion;
    }

    /**
     * Returns the patch version.
     * For example, this would return <code>4</code> for uPortal 2.3.4.
     * This method may return an empty String.
     * If the product patch version is unknown this method returns the String "unknown".
     * @return the patch version
     */
    public static String getPatch() {
        return microVersion;
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
     * If the product release tag is unknown this method returns the String "unknown".
     * @return the release tag matching the running code
     */
    public static String getReleaseTag() {
        return releaseTag;
    }

    /**
     * Returns the version of the current code.
     * For example, <code>2.3.4</code>.
     * If the product version is unknown this method returns the String "unknown".
     * @return the current version of the running code
     */
    public static String getVersion() {
        return dottedTriple;
    }

    /**
     * Returns a display-friendly string representing the
     * current product and version.
     * For example, <code>uPortal 2.3.4</code>.
     * If the version is unknown this method returns the String "unknown".
     * @return a verison string suitable for display
     */
    public static String getProductAndVersion() {
        return productAndVersion;
    }

}
