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

package org.jasig.portal.portlet.delegation;

import java.io.Writer;
import java.util.Map;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.tags.Constants;
import org.jasig.portal.api.portlet.DelegationRequest;
import org.jasig.portal.api.portlet.DelegationResponse;
import org.jasig.portal.api.portlet.PortletDelegationDispatcher;
import org.jasig.portal.api.portlet.PortletDelegationLocator;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.rendering.PortletOutputHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.View;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletDelegationView implements View {
    /**
     * Model attribute to store the optional {@link Writer} to use when delegating a {@link RenderRequest}
     */
    public static final String DELEGATE_RENDER_OUTPUT_HANDLER = "DELEGATE_RENDER_OUTPUT_HANDLER";

    /**
     * Model attribute to store the optional {@link DelegationRequest}
     */
    public static final String DELEGATE_REQUEST = "DELEGATE_REQUEST";

    /**
     * Model attribute to store the required {@link IPortletWindowId}
     */
    public static final String DELEGATE_PORTLET_WINDOW_ID = "DELEGATE_PORTLET_WINDOW_ID";

    
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private PortletDelegationLocator portletDelegationLocator;
    
    @Autowired
    public void setPortletDelegationLocator(PortletDelegationLocator portletDelegationLocator) {
        this.portletDelegationLocator = portletDelegationLocator;
    }

    /* (non-Javadoc)
     * @see org.springframework.web.servlet.View#getContentType()
     */
    @Override
    public String getContentType() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.springframework.web.servlet.View#render(java.util.Map, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        final IPortletWindowId portletWindowId = (IPortletWindowId)model.get(DELEGATE_PORTLET_WINDOW_ID);
        final DelegationRequest delegationRequest = (DelegationRequest)model.get(DELEGATE_REQUEST);
        
        final PortletRequest portletRequest = (PortletRequest)request.getAttribute(Constants.PORTLET_REQUEST);
        final PortletResponse portletResponse = (PortletResponse)request.getAttribute(Constants.PORTLET_RESPONSE);
        
        final PortletDelegationDispatcher requestDispatcher = portletDelegationLocator.getRequestDispatcher(portletRequest, portletWindowId);
        if (requestDispatcher == null) {
            throw new IllegalArgumentException("No PortletDelegationDispatcher exists for portlet window id: " + portletWindowId);
        }
        this.logger.debug("Found delegation dispatcher for portlet window id {} - {}", portletWindowId, requestDispatcher);
        
        final DelegationResponse delegationResponse;
        final String phase = (String)request.getAttribute(PortletRequest.LIFECYCLE_PHASE);
        if (PortletRequest.RENDER_PHASE.equals(phase)){
            final PortletOutputHandler portletOutputHandler = (PortletOutputHandler)model.get(DELEGATE_RENDER_OUTPUT_HANDLER);
            if (portletOutputHandler != null) {
                this.logger.debug("Delegating RenderRequest with custom Writer and {}", delegationRequest);
                delegationResponse = requestDispatcher.doRender((RenderRequest)portletRequest, (RenderResponse)portletResponse, delegationRequest, portletOutputHandler);
            }
            else {
                this.logger.debug("Delegating RenderRequest with default Writer and {}", delegationRequest);
                delegationResponse = requestDispatcher.doRender((RenderRequest)portletRequest, (RenderResponse)portletResponse, delegationRequest);
            }
        }       
        else if (PortletRequest.RESOURCE_PHASE.equals(phase)){
            this.logger.debug("Delegating ResourceRequest and {}", delegationRequest);
            delegationResponse = requestDispatcher.doServeResource((ResourceRequest)portletRequest, (ResourceResponse)portletResponse, delegationRequest);
        }
        else {
            throw new UnsupportedOperationException("Portlet lifecycle phase " + phase + " is not supported by the delegation view");
        }

        this.logger.debug("{}", delegationResponse);
    }

}
