/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
     * @throws CasProxyTicketAcquisitionException - when unable to obtain Proxy Ticket.
     */
    public String getCasServiceToken(String target) throws CasProxyTicketAcquisitionException;
}
