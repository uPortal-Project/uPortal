/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlets.swapper;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import org.jasig.portal.LoginServlet;
import org.jasig.services.persondir.IPersonAttributes;
import org.springframework.webflow.context.ExternalContext;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class IdentitySwapperHelperImpl {

    public void swapAttributes(ExternalContext externalContext, IPersonAttributes person) {
        final PortletRequest portletRequest = (PortletRequest)externalContext.getNativeRequest();
        final PortletSession portletSession = portletRequest.getPortletSession();
        portletSession.setAttribute(LoginServlet.SWAP_TARGET_UID, person.getName(), PortletSession.APPLICATION_SCOPE);
    }
}
