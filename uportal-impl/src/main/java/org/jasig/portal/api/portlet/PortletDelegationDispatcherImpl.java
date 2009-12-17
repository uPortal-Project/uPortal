/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.api.portlet;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.Validate;
import org.apache.pluto.PortletContainer;
import org.apache.pluto.PortletContainerException;
import org.jasig.portal.channels.portlet.ISpringPortletChannel;
import org.jasig.portal.layout.TransientUserLayoutManagerWrapper;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
import org.jasig.portal.portlet.registry.ITransientPortletWindowRegistry;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.url.IPortalRequestUtils;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletDelegationDispatcherImpl implements PortletDelegationDispatcher {
    private final IPortletDefinitionId portletDefinitionId;
    
    private final IPortletEntityRegistry portletEntityRegistry;
    private final ITransientPortletWindowRegistry transientPortletWindowRegistry;
    private final IPortalRequestUtils portalRequestUtils;
    private final PortletContainer portletContainer;
    private final IPersonManager personManager;
    
    private ISpringPortletChannel portletChannel;
    
    public PortletDelegationDispatcherImpl(IPortletDefinitionId portletDefinitionId,
            IPortletEntityRegistry portletEntityRegistry, ITransientPortletWindowRegistry transientPortletWindowRegistry, 
            IPortalRequestUtils portalRequestUtils, PortletContainer portletContainer, IPersonManager personManager) {
        
        Validate.notNull(portletDefinitionId);
        Validate.notNull(portletEntityRegistry);
        Validate.notNull(transientPortletWindowRegistry);
        Validate.notNull(portalRequestUtils);
        Validate.notNull(portletContainer);
        Validate.notNull(personManager);
        
        this.portletDefinitionId = portletDefinitionId;
        this.portletEntityRegistry = portletEntityRegistry;
        this.transientPortletWindowRegistry = transientPortletWindowRegistry;
        this.portalRequestUtils = portalRequestUtils;
        this.portletContainer = portletContainer;
        this.personManager = personManager;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.api.portlet.PortletDelegationDispatcher#doAction(javax.portlet.ActionRequest, javax.portlet.ActionResponse)
     */
    @Override
    public void doAction(ActionRequest actionRequest, ActionResponse actionResponse) {
        final HttpServletRequest request = this.portalRequestUtils.getOriginalPortletAdaptorRequest(actionRequest);
        final HttpServletResponse response = this.portalRequestUtils.getOriginalPortletAdaptorResponse(actionRequest);
        
        
        
        final IPerson person = this.personManager.getPerson(request);
        //TODO check if person is not null
        
        final IPortletEntity portletEntity = this.portletEntityRegistry.getOrCreatePortletEntity(this.portletDefinitionId, TransientUserLayoutManagerWrapper.SUBSCRIBE_PREFIX + "." + portletDefinitionId, person.getID());
        
        final IPortletEntityId portletEntityId = portletEntity.getPortletEntityId();
        final IPortletWindow defaultPortletWindow = this.transientPortletWindowRegistry.getOrCreateDefaultPortletWindow(request, portletEntityId);
        final IPortletWindowId portletWindowId = this.transientPortletWindowRegistry.createTransientPortletWindowId(request, defaultPortletWindow.getPortletWindowId());
        
        final IPortletWindow portletWindow = this.transientPortletWindowRegistry.createPortletWindow(request, portletWindowId.getStringId(), portletEntityId);
        
        //TODO check if doLoad was called?
        
        
        //TODO eventually replace this with a call to PortletRenderer that just returns a RenderedPortlet object?
        //RenderedPortlet would have the header, title, content, etc
        try {
            this.portletContainer.doAction(portletWindow, request, response);
        }
        catch (PortletException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (PortletContainerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
        //TODO extract out an IPortletRenderer from ISpringPortletChannel that works without requiring the uPortal IChannel APIs
        
//        this.portletChannel.action(channelStaticData, portalControlStructures, channelRuntimeData);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.api.portlet.PortletDelegationDispatcher#doRender(javax.portlet.RenderRequest, javax.portlet.RenderResponse)
     */
    @Override
    public void doRender(RenderRequest renderRequest, RenderResponse renderResponse) {
        //TODO check if doLoad was called
        
        //this.portletContainer.doRender(this.portletWindow, req, res);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.api.portlet.PortletDelegationDispatcher#getPortletMode()
     */
    @Override
    public PortletMode getPortletMode() {
        return null; //this.portletWindow.getPortletMode();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.api.portlet.PortletDelegationDispatcher#getPortletWindowId()
     */
    @Override
    public IPortletWindowId getPortletWindowId() {
        return null; //this.portletWindow.getPortletWindowId();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.api.portlet.PortletDelegationDispatcher#getWindowState()
     */
    @Override
    public WindowState getWindowState() {
        return null; //this.portletWindow.getWindowState();
    }
}
