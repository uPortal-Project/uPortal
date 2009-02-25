/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.portlets.swapper;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import org.jasig.portal.LoginServlet;
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
        portletSession.setAttribute(LoginServlet.SWAP_TARGET_UID, person.getName(), PortletSession.APPLICATION_SCOPE);
    }
}
