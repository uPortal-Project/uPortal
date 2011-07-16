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

package org.jasig.portal.spring.web.flow;

import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.springframework.webflow.context.ExternalContext;

/**
 * Useful general utilities for uPortal's webflows.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public interface IPortalWebFlowUtils {

    /**
     * Return an IPerson instance representing the current user.
     * 
     * @param externalContext portlet webflow external context
     * @return
     */
    public IPerson getCurrentPerson(ExternalContext externalContext);

    /**
     * Return an IAuthorizationPrincipal instance representing the current user.
     * 
     * @param externalContext portlet webflow external context
     * @return
     */
    public IAuthorizationPrincipal getCurrentPrincipal(ExternalContext externalContext);

}