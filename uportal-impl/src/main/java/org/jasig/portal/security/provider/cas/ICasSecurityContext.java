/* Copyright 2006 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security.provider.cas;

/**
 * Interface implemented by CAS security contexts.
 * CAS security contexts are capable of getting a token - a proxy ticket -
 * suitable for accessing a given String-identified service.  This interface
 * declares a method for getting such a token.
 * 
 * This interface evolved from edu.yale.its.tp.portal.security.IYaleCasContext
 * as distributed in the Yale uPortal CAS security provider distribution version
 * 3.0.0.
 * 
 * @version $Revision$ $Date$
 */
public interface ICasSecurityContext {
    /** Authentication type for CAS authentication */
    public static final int CAS_AUTHTYPE = 0x1701;
    
    /**
     * Get a proxy ticket for a given target.
     * Implementations should return null if no PGTIOU or PGT is available by 
     * which to obtain a proxy ticket.
     * Implementations should throw a CASProxyTicketAcquisitionException if 
     * an error occurs during an attempt
     * to obtain a PGT.  In particular, inability to contact the CAS server and 
     * expiration of the underlying PGT
     * should result in a CASProxyTicketAcquisitionException.
     * @param target - URL for which a proxy ticket is desired.
     * @return a valid proxy ticket for the target, or null.
     * @throws CASProxyTicketAcquisitionException - when unable to obtain Proxy Ticket.
     */
    public String getCasServiceToken(String target) throws CasProxyTicketAcquisitionException;
}
