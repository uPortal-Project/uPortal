/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.api.portlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.Validate;
import org.jasig.portal.channels.portlet.IPortletRenderer;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.url.IPortalRequestUtils;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletDelegationDispatcherImpl implements PortletDelegationDispatcher {
    private final IPortletWindow portletWindow;
    private final int userId;
    
    private final IPortalRequestUtils portalRequestUtils;
    private final IPersonManager personManager;
    private final IPortletRenderer portletRenderer;
    
    

    public PortletDelegationDispatcherImpl(IPortletWindow portletWindow, int userId,
            IPortalRequestUtils portalRequestUtils, IPersonManager personManager, IPortletRenderer portletRenderer) {
        
        Validate.notNull(portletWindow, "portletWindow can not be null");
        Validate.notNull(portalRequestUtils, "portalRequestUtils can not be null");
        Validate.notNull(personManager, "personManager can not be null");
        Validate.notNull(portletRenderer, "portletRenderer can not be null");
        
        this.portletWindow = portletWindow;
        this.userId = userId;
        this.portalRequestUtils = portalRequestUtils;
        this.personManager = personManager;
        this.portletRenderer = portletRenderer;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.api.portlet.PortletDelegationDispatcher#doAction(javax.portlet.ActionRequest, javax.portlet.ActionResponse)
     */
    @Override
    public void doAction(ActionRequest actionRequest, ActionResponse actionResponse) {
        final HttpServletRequest request = this.portalRequestUtils.getOriginalPortalRequest(actionRequest);
        final HttpServletResponse response = this.portalRequestUtils.getOriginalPortalResponse(actionRequest);

        //Sanity check that the dispatch is being called by the same user it was created for
        final IPerson person = this.personManager.getPerson(request);
        if (this.userId != person.getID()) {
            throw new IllegalStateException("This dispatcher was created for userId " + this.userId + " but is being executed for userId " + person.getID());
        }
        
        this.portletRenderer.doAction(this.portletWindow.getPortletWindowId(), request, response);
        //TODO response would be committed at this point ... will be interesting to see how pluto handles redirecting the same request twice :(
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.api.portlet.PortletDelegationDispatcher#doRender(javax.portlet.RenderRequest, javax.portlet.RenderResponse)
     */
    @Override
    public void doRender(RenderRequest renderRequest, RenderResponse renderResponse) throws IOException {
        final HttpServletRequest request = this.portalRequestUtils.getOriginalPortalRequest(renderRequest);
        final HttpServletResponse response = this.portalRequestUtils.getOriginalPortalResponse(renderRequest);

        //Sanity check that the dispatch is being called by the same user it was created for
        final IPerson person = this.personManager.getPerson(request);
        if (this.userId != person.getID()) {
            throw new IllegalStateException("This dispatcher was created for userId " + this.userId + " but is being executed for userId " + person.getID());
        }
        
        final PrintWriter writer = renderResponse.getWriter();
        this.portletRenderer.doRender(this.portletWindow.getPortletWindowId(), request, response, writer);
        writer.flush();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.api.portlet.PortletDelegationDispatcher#getPortletMode()
     */
    @Override
    public PortletMode getPortletMode() {
        return this.portletWindow.getPortletMode();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.api.portlet.PortletDelegationDispatcher#getPortletWindowId()
     */
    @Override
    public IPortletWindowId getPortletWindowId() {
        return this.portletWindow.getPortletWindowId();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.api.portlet.PortletDelegationDispatcher#getWindowState()
     */
    @Override
    public WindowState getWindowState() {
        return this.portletWindow.getWindowState();
    }
}
