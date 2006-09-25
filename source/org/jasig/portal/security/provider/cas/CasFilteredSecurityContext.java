/* Copyright 2006 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security.provider.cas;

import java.io.IOException;

import org.jasig.portal.security.PortalSecurityException;
import org.jasig.portal.security.provider.ChainingSecurityContext;

import edu.yale.its.tp.cas.client.CASReceipt;
import edu.yale.its.tp.cas.client.filter.StaticCasReceiptCacherFilter;
import edu.yale.its.tp.cas.proxy.ProxyTicketReceptor;

/**
 * CAS security context backed by the StaticCasReceiptCacherFilter.
 * This security context implements the approach of fronting uPortal login
 * with the CAS Java Servlet filters (specifically CASValidateFilter and
 * StaticCasReceiptCacherFilter), allowing the filters to do the "heavy lifting" of
 * performing the authentication and then just reading and using the
 * results here in the security context.
 * 
 * This approach dodges having to configure a constant service URL for
 * portal login and thereby affords flexibility for supporting parameters
 * on login for specifying the fname of a channel to jump to on authentication,
 * e.g.
 * 
 * Relies on the CASValidationFilter already having done any necessary ticket validation,
 * and the StaticCasReceiptCacherFilter having cached the receipt, keyed by the ticket.
 * Based on Susan Bramhall's YaleSecurityContext.  Based on edu.yale.its.tp.portal.security.YaleCasFilteredContext
 * as distributed in the Yale CAS uPortal security provider module version 3.0.0.
 */
public class CasFilteredSecurityContext extends ChainingSecurityContext implements ICasSecurityContext {

    private static final long serialVersionUID = 1L;

    /**
     * Receipt stored here upon authenticate() invocation.
     */
    private CASReceipt receipt;

    CasFilteredSecurityContext() {
        super();
    }

    public int getAuthType() {
        return ICasSecurityContext.CAS_AUTHTYPE;
    }

    /*
     * Authentication entry-point Opaque credentials are set to value of ticket
     * supplied by CAS.  This method checks to see if the StaticCasReceiptCacherFilter
     * has previously cached a receipt representing the prior validation of this ticket.
     */
    public synchronized void authenticate() throws PortalSecurityException {
        if (log.isTraceEnabled()) {
            log.trace("entering authenticate()");
        }
        String serviceTicket = new String(
                this.myOpaqueCredentials.credentialstring);

        this.isauth = false;
        this.receipt = StaticCasReceiptCacherFilter
                .receiptForTicket(serviceTicket);

        if (this.receipt != null) {
            this.myPrincipal.setUID(this.receipt.getUserName());
            this.isauth = true;
            log.debug("CASContext authenticated [" + this.myPrincipal.getUID()
                    + "] using receipt [" + this.receipt + "]");
        }

        this.myAdditionalDescriptor = null; //no additional descriptor from CAS
        super.authenticate();
        if (log.isTraceEnabled()) {
            log.trace("returning from authenticate()");
        }
        return;
    }

 
    public String getCasServiceToken(String target) throws CasProxyTicketAcquisitionException {
        if (log.isTraceEnabled()) {
            log.trace("entering getCasServiceToken(" + target
                    + "), previously cached receipt=["
                    + this.receipt + "]");
        }
        if (this.receipt == null){
            if (log.isDebugEnabled()){
                log.debug("Returning null CAS Service Token because cached receipt is NULL.");
            }
            return null;
        }
        if (this.receipt.getPgtIou() == null){
            if (log.isDebugEnabled()){
                log.debug("Returning null CAS Service Token because cached receipt does not include a PGTIOU.");
            }
            return null;
        }
        String proxyTicket;
        try {
            proxyTicket = ProxyTicketReceptor.getProxyTicket(this.receipt
                    .getPgtIou(), target);
        } catch (IOException e) {
            log.error("Error contacting CAS server for proxy ticket", e);
            throw new CasProxyTicketAcquisitionException(target,this.receipt, e);
        }
        if (proxyTicket == null){
            log.error("Failed to obtain proxy ticket using receipt [" + this.receipt + "], has the Proxy Granting Ticket referenced by the pgtIou expired?");
            throw new CasProxyTicketAcquisitionException(target, this.receipt);
        }
        if (log.isTraceEnabled()) {
            log.trace("returning from getCasServiceToken(), returning proxy ticket ["
                    + proxyTicket + "]");
        }
        return proxyTicket;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getClass().getName());
        sb.append(" receipt:");
        sb.append(this.receipt);
        return sb.toString();
    }
}
