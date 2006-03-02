/* Copyright 2006 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security.provider.cas;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ResourceMissingException;
import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.ISecurityContextFactory;
import org.jasig.portal.security.provider.BrokenSecurityContext;
import org.jasig.portal.utils.ResourceLoader;

/**
 * Factory for CasSecurityContext instances.
 * This factory implementation reads configuration properties from security.properties
 * once and stores those properties as static fields.  As a consequence, this
 * class is not usefully multiply instantiable -- you can multiply instantiate it, but
 * all instances will behave the same.  Also, this factory will not reflect changes
 * to these properties in security.properties until the class is unloaded and
 * reloaded -- typically only on portal JVM restart.
 */
public final class CasSecurityContextFactory 
    implements ISecurityContextFactory {
    
    private final Log log = LogFactory.getLog(getClass());
    
    /**
     * The https: URL to which CAS is to deliver proxy granting tickets. Static
     * to avoid going back to security properties repeatedly.
     */
    private static String casProxyCallbackUrl;

    /**
     * The URL to which service tickets will authenticate users. Static to avoid
     * going back to security properties repeatedly.
     */
    private static String portalServiceUrl;

    /**
     * The URL at which these service tickets can be validated. Static to avoid
     * going back to security properties repeatedly.
     */
    private static String casValidateUrl;

    /**
     * Retrieve my properties from security.properties
     * 
     * @throws IOException
     * @throws ResourceMissingException
     */
    private void initializeFromSecurityProperties()
            throws ResourceMissingException, IOException {
        //We retrieve the tokens representing the credential and principal
        // parameters from the security properties file.
        Properties props = ResourceLoader.getResourceAsProperties(
                CasSecurityContextFactory.class, "/properties/security.properties");

        CasSecurityContextFactory.casProxyCallbackUrl = props
                .getProperty("org.jasig.portal.security.provider.YaleCasContext.CasProxyCallbackUrl");
        log.debug("CasProxyCallbackUrl is ["
                + CasSecurityContextFactory.casProxyCallbackUrl + "]");
        CasSecurityContextFactory.portalServiceUrl = props
                .getProperty("org.jasig.portal.security.provider.YaleCasContext.PortalServiceUrl");
        log.debug("PortalServiceUrl is ["
                + CasSecurityContextFactory.portalServiceUrl + "]");
        CasSecurityContextFactory.casValidateUrl = props
                .getProperty("org.jasig.portal.security.provider.YaleCasContext.CasValidateUrl");
        log.debug("CasValidateUrl is [" + CasSecurityContextFactory.casValidateUrl
                + "]");
    }

    public ISecurityContext getSecurityContext() {
        try {
            if (CasSecurityContextFactory.casValidateUrl == null
                    || CasSecurityContextFactory.portalServiceUrl == null) {
                initializeFromSecurityProperties();
            }
            return new CasSecurityContext(CasSecurityContextFactory.portalServiceUrl,
                    CasSecurityContextFactory.casValidateUrl,
                    CasSecurityContextFactory.casProxyCallbackUrl);
        } catch (Throwable t) {
            log.error("Exception getting security context: " + t, t);
            // returning a broken security context allows other security contexts
            // in the chain, if any, to succeed even though the CAS context factory
            // is misconfigured.
            return new BrokenSecurityContext();
        }
    }

    /**
     * Static setter method for poor-man's dependency injection. Calling this
     * class's static setter methods is an alternative to using
     * security.properties. You could call this from a context listener. You can
     * safely just ignore this method.
     * 
     * @param casProxyCallbackUrlArg -
     *                the https: URL to which you would like CAS deliver proxy
     *                granting tickets.
     */
    public static void setCasProxyCallbackUrl(String casProxyCallbackUrlArg) {
        if (casProxyCallbackUrlArg != null
               && casProxyCallbackUrlArg.toUpperCase().startsWith("HTTPS://"))
            CasSecurityContextFactory.casProxyCallbackUrl = casProxyCallbackUrlArg;
    }

    /**
     * Static setter method for poor-man's dependency injection. Calling this
     * class's static setter methods is an alternative to using
     * security.properties. You could call this from a context listener. You can
     * safely just ignore this method.
     * 
     * @param portalServiceUrlArg -
     *                the URL to which service tickets will authenticate portal
     *                users
     */
    public static void setPortalServiceUrl(String portalServiceUrlArg) {
        if (portalServiceUrlArg == null)
            throw new IllegalArgumentException(
                    "Cannot set the portal service URL to null.");
        CasSecurityContextFactory.portalServiceUrl = portalServiceUrlArg;
    }

    /**
     * Static setter method for poor-man's dependency injection. Calling this
     * class's static setter methods is an alternative to using
     * security.properties. You could call this from a context listener. You can
     * safely just ignore this method.
     * 
     * @param casValidateUrlArg -
     *                the https: URL at which CAS will validate the tickets
     */
    public static void setCasValidateUrl(String casValidateUrlArg) {
        if (casValidateUrlArg == null)
            throw new IllegalArgumentException(
                    "Cannot set the cas validate URL to null.");
        CasSecurityContextFactory.casValidateUrl = casValidateUrlArg;
    }

}
