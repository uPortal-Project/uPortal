/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.api.portlet;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * 
 * TODO  does the delegate see parent's parameters?
 * TODO  how does the parent change its parameters for URLs rendered by the child?
 * TODO  can the parent modify the state/mode/parameters of the child?
 *  
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface PortletDelegationDispatcher {
    public IPortletWindowId getPortletWindowId();
    
    public DelegateState getDelegateState();

    public DelegateState doAction(ActionRequest actionRequest, ActionResponse actionResponse);

    public DelegateState doAction(ActionRequest actionRequest, ActionResponse actionResponse, DelegateState delegateState);
    
    public DelegateState doRender(RenderRequest renderRequest, RenderResponse renderResponse) throws IOException;
    
    public DelegateState doRender(RenderRequest renderRequest, RenderResponse renderResponse, DelegateState delegateState) throws IOException;
}
