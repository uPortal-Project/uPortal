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

package org.jasig.portal.api.portlet;

import java.io.IOException;
import java.io.Writer;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.rendering.PortletOutputHandler;
import org.jasig.portal.portlet.rendering.PortletResourceOutputHandler;

/**
 * Used to dispatch requests to a delegate portlet window. Also provides information about the
 * state of the delegate portlet window.
 *  
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface PortletDelegationDispatcher {
    /**
     * @return The ID of the delegate portlet window, useful for retrieving this dispatcher again from the {@link PortletDelegationLocator}
     */
    public IPortletWindowId getPortletWindowId();
    
    /**
     * @return The current sate of the delegate window
     */
    public DelegateState getDelegateState();
    
    /**
     * Calls {@link #doAction(ActionRequest, ActionResponse, DelegationRequest)} with no {@link DelegationRequest}
     * data 
     * 
     * @see #doAction(ActionRequest, ActionResponse, DelegationRequest)
     */
    public DelegationActionResponse doAction(ActionRequest actionRequest, ActionResponse actionResponse) throws IOException;

    /**
     * Executes a portlet action request on the delegate window. The state, mode and parameters in the delegation request (if set) are used
     * by the delegate.
     * 
     * @param actionRequest The current portlet's action request
     * @param actionResponse The current portlet's action response
     * @param delegationRequest The state to set for the delegate and the basis for generated URLs
     * @return The delegation response state, will indicate if the delegate sent a redirect
     */
    public DelegationActionResponse doAction(ActionRequest actionRequest, ActionResponse actionResponse, DelegationRequest delegationRequest) throws IOException;
    
    /**
     * Calls {@link #doAction(ResourceRequest, ResourceResponse, DelegationRequest)} with no {@link DelegationRequest}
     * data 
     * 
     * @see #doServeResource(ResourceRequest, ResourceResponse, DelegationRequest)
     */
    public DelegationResponse doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws IOException;

    /**
     * Calls @link {@link #doServeResource(ResourceRequest, ResourceResponse, DelegationRequest, PortletOutputHandler)} wrapping the
     * {@link ResourceResponse} for the {@link PortletOutputHandler}
     * 
     * @see #doServeResource(ResourceRequest, ResourceResponse, DelegationRequest, PortletOutputHandler)
     */
    public DelegationResponse doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse, DelegationRequest delegationRequest) throws IOException;

    /**
     * Executes a portlet resource request on the delegate window. The state, mode and parameters in the delegation request (if set) are used
     * by the delegate.
     * 
     * @param resourceRequest The current portlet's resource request
     * @param resourceResponse The current portlet's resource response
     * @param delegationRequest The state to set for the delegate and the basis for generated URLs
     * @param portletOutputHandler The output handler to write to
     * @return The delegation response state, will indicate if the delegate sent a redirect
     */
    public DelegationResponse doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse, DelegationRequest delegationRequest, PortletResourceOutputHandler portletOutputHandler) throws IOException;
    
    /**
     * Calls {@link #doRender(RenderRequest, RenderResponse, DelegationRequest, Writer)} with no {@link DelegationRequest}
     * data wrapping the {@link RenderResponse} for the {@link PortletOutputHandler}
     * 
     * @see #doRender(RenderRequest, RenderResponse, DelegationRequest, Writer)
     */
    public DelegationResponse doRender(RenderRequest renderRequest, RenderResponse renderResponse) throws IOException;
    
    /**
     * Calls {@link #doRender(RenderRequest, RenderResponse, DelegationRequest, Writer)} with no {@link DelegationRequest}
     * data.
     * 
     * @see #doRender(RenderRequest, RenderResponse, DelegationRequest, Writer)
     */
    public DelegationResponse doRender(RenderRequest renderRequest, RenderResponse renderResponse, PortletOutputHandler portletOutputHandler) throws IOException;
    
    /**
     * Calls {@link #doRender(RenderRequest, RenderResponse, DelegationRequest, Writer)} wrapping the
     * {@link RenderResponse} for the {@link PortletOutputHandler}
     * 
     * @see #doRender(RenderRequest, RenderResponse, DelegationRequest, Writer)
     */
    public DelegationResponse doRender(RenderRequest renderRequest, RenderResponse renderResponse, DelegationRequest delegationRequest) throws IOException;

    /**
     * Executes a portlet render request on the delegate window. The state, mode and parameters in the delegation request (if set) are used
     * by the delegate. The output of the portlet's rendering is written to the provided {@link PortletOutputHandler}
     * 
     * @param renderRequest The current portlet's render request
     * @param renderResponse The current portlet's render response
     * @param delegationRequest The state to set for the delegate and the basis for generated URLs
     * @param portletOutputHandler The PortletOutputHandler to send all content from the delegate portlet to
     * @return The delegation response state, will indicate if the delegate sent a redirect
     */
    public DelegationResponse doRender(RenderRequest renderRequest, RenderResponse renderResponse, DelegationRequest delegationRequest, PortletOutputHandler portletOutputHandler) throws IOException;
}
