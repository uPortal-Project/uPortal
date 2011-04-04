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

package org.jasig.portal.portlets.swapper;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import org.jasig.portal.security.mvc.LoginController;
import org.jasig.services.persondir.IPersonAttributes;
import org.springframework.webflow.context.ExternalContext;

/**
 * Helper class for identity swapper
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class IdentitySwapperHelperImpl implements IIdentitySwapperHelper {

    /* (non-Javadoc)
     * @see org.jasig.portal.portlets.swapper.IIdentitySwapperHelper#swapAttributes(org.springframework.webflow.context.ExternalContext, org.jasig.services.persondir.IPersonAttributes)
     */
    public void swapAttributes(ExternalContext externalContext, IPersonAttributes person) {
        final PortletRequest portletRequest = (PortletRequest)externalContext.getNativeRequest();
        final PortletSession portletSession = portletRequest.getPortletSession();
        portletSession.setAttribute(LoginController.SWAP_TARGET_UID, person.getName(), PortletSession.APPLICATION_SCOPE);
    }
}
