/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.api.portlet;

import java.io.IOException;
import java.io.Writer;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.jasig.portal.portlet.om.IPortletWindowId;

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
     * Calls {@link #doRender(RenderRequest, RenderResponse, DelegationRequest, Writer)} with no {@link DelegationRequest}
     * data and uses {@link RenderResponse#getWriter()} for the writer.,
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
    public DelegationResponse doRender(RenderRequest renderRequest, RenderResponse renderResponse, Writer writer) throws IOException;
    
    /**
     * Calls {@link #doRender(RenderRequest, RenderResponse, DelegationRequest, Writer)} using
     * {@link RenderResponse#getWriter()} for the writer.,
     * 
     * @see #doRender(RenderRequest, RenderResponse, DelegationRequest, Writer)
     */
    public DelegationResponse doRender(RenderRequest renderRequest, RenderResponse renderResponse, DelegationRequest delegationRequest) throws IOException;

    /**
     * Executes a portlet render request on the delegate window. The state, mode and parameters in the delegation request (if set) are used
     * by the delegate. The output of the portlet's rendering is written to the provided {@link Writer}
     * 
     * @param renderRequest The current portlet's render request
     * @param renderResponse The current portlet's render response
     * @param delegationRequest The state to set for the delegate and the basis for generated URLs
     * @param writer The Writer to send all content from the delegate portlet to
     * @return The delegation response state, will indicate if the delegate sent a redirect
     */
    public DelegationResponse doRender(RenderRequest renderRequest, RenderResponse renderResponse, DelegationRequest delegationRequest, Writer writer) throws IOException;
}
