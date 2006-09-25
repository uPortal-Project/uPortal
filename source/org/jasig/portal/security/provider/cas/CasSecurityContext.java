/* Copyright 2006 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security.provider.cas;

import java.io.IOException;
import java.net.URLEncoder;

import org.jasig.portal.security.PortalSecurityException;
import org.jasig.portal.security.provider.ChainingSecurityContext;

import edu.yale.its.tp.cas.client.ServiceTicketValidator;
import edu.yale.its.tp.cas.proxy.ProxyTicketReceptor;

/**
 * <p>
 * A SecurityContext using the Central Authentication Service.
 * </p>
 * An ICasSecurityContext implementation in keeping with the traditional approach of
 * performing actual authentication inside this security context. This security
 * context is suitable for cases where your login URL (the "portalServiceUrl")
 * is a constant.
 * 
 * This class is based on edu.yale.its.tp.portal.security.YaleCasContext as
 * distributed in the Yale uPortal CAS security provider version 3.0.0.
 * 
 * @version $Revision$ $Date$
 */
public class CasSecurityContext 
    extends ChainingSecurityContext 
    implements ICasSecurityContext{
    
    private static final long serialVersionUID = 1L;

    /**
     * The URL of the uPortal Login servlet to which service tickets will
     * authenticate users.
     */
    private final String portalServiceUrl;

    /**
     * The https: URL at which CAS offers its ticket validation service.
     */
    private final String casValidateUrl;

    /**
     * The https: URL at which CAS is to call back the uPortal with Proxy
     * Granting Tickets.
     */
    private final String casProxyCallbackUrl;

    /**
     * The pgtIou which keys to the Proxy Granting Ticket associated with this
     * authenticated security context, if any.
     */
    private String pgtIou = null;

    /**
     * Instantiate a YaleCasContext given a constant URL to which CAS service
     * tickets will authenticate users, a URL at which to validate those
     * tickets, and a callback URL at which to ask CAS to deliver Proxy Granting
     * Tickets.
     * 
     * @param portalServiceUrl -
     *                the constant URL to which service tickets authenticate users
     * @param casValidateUrl -
     *                the https: URL at which CAS offers its ticket validation
     *                service
     * @param casProxyCallbackUrl -
     *                the https: URL to which CAS should send proxy granting
     *                tickets.
     */
    public CasSecurityContext(String portalServiceUrl, String casValidateUrl,
            String casProxyCallbackUrl) {
        super();
        log.trace("entering YaleCasContext(" + portalServiceUrl + ","
                + casValidateUrl + "," + casProxyCallbackUrl + ")");

        if (portalServiceUrl == null) {
            throw new IllegalArgumentException(
                    "Cannot instantiate a YaleCasContext with a null portalServiceUrl.");
        }
        if (casValidateUrl == null) {
            throw new IllegalArgumentException(
                    "Cannot instantiate a YaleCasContext with a null casValidateUrl.");
        }
        this.casProxyCallbackUrl = casProxyCallbackUrl;
        this.portalServiceUrl = portalServiceUrl;
        this.casValidateUrl = casValidateUrl;

    }

    /**
     * Instantiate a YaleCasContext given a constant URL to which CAS service
     * tickets will authenticate users, a URL at which to validate those
     * tickets.
     * 
     * @param portalServiceUrl -
     *                the constant URL to which service tickets authenticate users
     * @param casValidateUrl -
     *                the https: URL at which CAS offers its ticket validation
     *                service
     */
    public CasSecurityContext(String portalServiceUrl, String casValidateUrl) {
        this(portalServiceUrl, casValidateUrl, null);
    }

    public int getAuthType() {
        return ICasSecurityContext.CAS_AUTHTYPE;
    }

    public synchronized void authenticate() throws PortalSecurityException {
        if (log.isTraceEnabled()) {
            log.trace("entering authenticate()");
        }
        String serviceTicket = new String(
                this.myOpaqueCredentials.credentialstring);

        this.isauth = false;
        try {
            ServiceTicketValidator sv = new ServiceTicketValidator();

            sv.setCasValidateUrl(this.casValidateUrl);
            if (this.casProxyCallbackUrl != null) {
                sv.setProxyCallbackUrl(this.casProxyCallbackUrl);
            }
            sv.setService(URLEncoder.encode(this.portalServiceUrl, "UTF-8"));
            sv.setServiceTicket(serviceTicket);
            log.debug("authenticate(): Validating ServiceTicket: ["
                    + serviceTicket + "]");
            sv.validate();
            log
                    .debug("authenticate(): got response:[" + sv.getResponse()
                            + "]");

            if (sv.isAuthenticationSuccesful()) {
                this.myPrincipal.setUID(sv.getUser());

                // We keep the pgtIOU around as the key to reference the Proxy
                // granting ticket
                // that will be used to obtain future Proxy Tickets.
                // A channel has
                // access to this securityContext and can request a Proxy ticket
                // via
                // the public getCasServiceToken method
                this.pgtIou = sv.getPgtIou();
                this.isauth = true;
                log.debug("CASContext authenticated ["
                        + this.myPrincipal.getUID() + "]");
            }
            //else {
            //  throw new PortalSecurityException("error code: " +
            // sv.getErrorCode()+"\n error message: "+sv.getErrorMessage());
            // }
        } catch (Throwable t) {
            log.error(t);
            throw new PortalSecurityException("Error in CAS Authentication: "
                    + t.getMessage());
            /*
             * if PortalSecurityException supported it, it would be nice to
             * chain here: throw new PortalSecurityException("Error in CAS
             * Authentication:", t);
             */
        }
        this.myAdditionalDescriptor = null; //no additional
                                                      // descriptor from CAS
        super.authenticate();
        if (log.isTraceEnabled()) {
            log.trace("returning from authenticate()");
        }
        return;
    }

    public String getCasServiceToken(String target)
            throws CasProxyTicketAcquisitionException {
        if (log.isTraceEnabled()) {
            log.trace("entering getCasServiceToken(" + target
                    + "), previously cached pgtIou=[" + this.pgtIou + "]");
        }
        String proxyTicket;
        try {
            proxyTicket = ProxyTicketReceptor.getProxyTicket(this.pgtIou,
                    target);
        } catch (IOException e) {
            throw new CasProxyTicketAcquisitionException(target, this.pgtIou, e);
        }
        if (log.isTraceEnabled()) {
            log.trace("returning from getCasServiceToken() with return value ["
                    + proxyTicket + "]");
        }
        return proxyTicket;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getClass().getName());
        sb.append(" portalServiceUrl=[").append(this.portalServiceUrl).append(
                "]");
        sb.append(" casValidateUrl=[").append(this.casValidateUrl).append("]");
        sb.append(" casProxyCallbackUrl=[").append(this.casProxyCallbackUrl)
                .append("]");
        sb.append(" pgtIou=[").append(this.pgtIou).append("]");
        return sb.toString();
    }
}
