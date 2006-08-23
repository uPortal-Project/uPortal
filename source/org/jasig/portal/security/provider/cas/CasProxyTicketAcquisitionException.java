/* Copyright 2006 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security.provider.cas;

import org.jasig.portal.PortalException;

import edu.yale.its.tp.cas.client.CASReceipt;

/**
 * Represents an exceptional condition encountered while attempting to
 * acquire a CAS Proxy Ticket.
 *
 * This exception evolved from edu.yale.its.tp.portal.security.CASProxyTicketAcquisitionException
 * as distributed in the Yale uPortal CAS security provider distribution version 3.0.0.
 */
public class CasProxyTicketAcquisitionException extends PortalException {

    private static final long serialVersionUID = 1L;

    /**
     * The service for which a proxy ticket could not be acquired.
     */
    private final String service;

    /**
     * CASReceipt provding background about the interaction with the CAS server
     * that did not produce a proxy granting ticket.
     */
    private final CASReceipt receipt;

    /**
     * The pgtiou that was unsuccessfully presented for obtaining a proxy
     * ticket.  This field will be null when the constructor supplies a
     * CASReceipt instead of a pgtiou.  The CASReceipt contains the pgtiou.
     */
    private final String pgtIou;


    /**
     * Exception thrown when cannot obtain proxy ticket for a given service using the given receipt.
     * @param serviceArg - service for which a PT was requested
     * @param receiptArg - receipt the pgtIou of which was being used to obtain the PT.
     */
    public CasProxyTicketAcquisitionException(String serviceArg, CASReceipt receiptArg){
        super("Could not obtain proxy ticket for service [" + serviceArg + "] using credentials [" + receiptArg + "]");
        this.service = serviceArg;
        this.receipt = receiptArg;
        this.pgtIou = null;
    }

    /**
     * Exception thrown when cannot obtain proxy ticket for a given service using the given receipt.
     * @param serviceArg - service for which a PT was requested
     * @param receiptArg - receipt the pgtIou of which was being used to obtain the PT.
     * @param cause - underlying throwable causing the error condition
     */
    public CasProxyTicketAcquisitionException(String serviceArg, CASReceipt receiptArg, Throwable cause){
        super("Could not obtain proxy ticket for service [" + serviceArg + "] using credentials [" + receiptArg + "].", cause);
        this.service = serviceArg;
        this.receipt = receiptArg;
        this.pgtIou = null;
    }

    /**
     * Exception thrown when cannot obtain proxy ticket for a given service using the given pgtIou.
     * @param serviceArg - service for which a PT was requested.
     * @param pgtIouArg - the pgtIou for the PGT which was to be used to obtain the PT.
     */
    public CasProxyTicketAcquisitionException(String serviceArg, String pgtIouArg){
        super("Could not obtain proxy ticket for service [" + serviceArg + "] using credentials [" + pgtIouArg + "]");
        this.service = serviceArg;
        this.pgtIou = pgtIouArg;
        this.receipt = null;
    }

    /**
     * Exception thrown when cannot obtain proxy ticket for a given service using the given pgtIou.
     * @param serviceArg - service for which a PT was requested.
     * @param pgtIouArg - the pgtIou for the PGT which was to be used to obtain the PT.
     * @param cause - underlying cause of the error condition
     */
    public CasProxyTicketAcquisitionException(String serviceArg, String pgtIouArg, Throwable cause){
        super("Could not obtain proxy ticket for service [" + serviceArg + "] using credentials [" + pgtIouArg + "]", cause);
        this.service = serviceArg;
        this.pgtIou = pgtIouArg;
        this.receipt = null;
    }

    /**
     * Get the CASReceipt if present, null otherwise.
     * CASReceipts convey information about a CAS authentication and can provide
     * useful diagnostic context for understanding the failure to acquire a
     * proxy granting ticket represented by this exception.
     * The CASReceipt may not be available depending upon the context with which
     * this exception was initialized, so this method may return null.
     * @return the CASReceipt or null.
     */
    public CASReceipt getReceipt() {
        return this.receipt;
    }

    /**
     * Get the identifer of the service service for which the portal was trying
     * to obtain a Proxy Ticket when this exception was generated.
     * @return the service.
     */
    public String getService() {
        return this.service;
    }

    /**
     * Get the PgtIou.
     * The pgtiou may have been set explicitly in the constructor or this method
     * may read it from the stored CASReceipt.
     * @return the pgtiou.
     */
    public String getPgtIou() {

        if (this.pgtIou != null) {
            return this.pgtIou;
        } else if (this.receipt != null) {
            return this.receipt.getPgtIou();
        }

        return null;
    }
}
