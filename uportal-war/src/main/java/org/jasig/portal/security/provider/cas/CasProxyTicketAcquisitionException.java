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

import org.jasig.portal.PortalException;

import java.security.Principal;

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

    private final Principal principal;

    /**
     * Exception thrown when cannot obtain proxy ticket for a given service using the given receipt.
     * @param serviceArg - service for which a PT was requested
     * @param principal - The principal who was trying to obtain the PT.
     */
    public CasProxyTicketAcquisitionException(final String serviceArg, final Principal principal) {
        super("Could not obtain proxy ticket for service [" + serviceArg + "] for principal [" + principal.getName() + "]");
        this.service = serviceArg;
        this.principal = principal;
    }

     /**
     * Get the identifier of the service service for which the portal was trying
     * to obtain a Proxy Ticket when this exception was generated.
     * @return the service.
     */
    public String getService() {
        return this.service;
    }

    public Principal getPrincipal() {
        return this.principal;
    }
}
