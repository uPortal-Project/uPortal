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

package org.jasig.portal.security.provider;

import org.jasig.portal.security.PortalSecurityException;

/**
 * <p>
 * A SecurityContext that can never be authenticated.
 * </p>
 * <p>An unauthenticatable security context which a security context factory
 * may return in the case where its initialization of the security context it was supposed 
 * to return failed.  This allows a security context factory to "fail gracefully",
 * fulfilling its API contract and allowing other security contexts in the chain to
 * continue to operate.<p>
 * <p>
 * This class is a descendent of edu.yale.its.tp.portal.security.BrokenSecurityContext,
 * which was distributed in the Yale CAS uPortal security provider module version
 * 3.0.0.</p>
 * 
 * @version $Revision$ $Date$
 */
public class BrokenSecurityContext extends ChainingSecurityContext {

    private static final long serialVersionUID = 1L;

    public static final int BROKEN_AUTH_TYPE = 666;


    /**
     * Instantiate a BrokenSecurityContext
     */
    public BrokenSecurityContext() {
        super();
    }
    
    public int getAuthType() {
        return BROKEN_AUTH_TYPE;
    }

    public void authenticate() throws PortalSecurityException {
        if (log.isTraceEnabled()) {
            log.trace("entering authenticate()");
        }
        // Note that this.isauth is still false.
        super.authenticate();
        if (log.isTraceEnabled()) {
            log.trace("returning from authenticate()");
        }
        return;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getClass().getName());
        return sb.toString();
    }
}
