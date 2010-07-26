/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.url;

import java.util.List;
import java.util.Map;


/**
 * Provides information about the portal request.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortalRequestInfo {
    /**
     * @return The state rendered by the URL
     */
    public UrlState getUrlState();
    
    /**
     * @return Type of url this request is for
     */
    public UrlType getUrlType();
    
    /**
     * @return Parameters targeting the portal itself
     */
    public Map<String, List<String>> getPortalParameters();
    
    /**
     * @return The layout node being targeted by the request. If the request isn't targeting a particular layout node null is returned.
     */
    public String getTargetedLayoutNodeId();
    
    /**
     * @return Parameters targeting the layout management system
     */
    public Map<String, List<String>> getLayoutParameters();
    
    /**
     * @return Information for a request targeting a portlet. If the request doesn't target a portlet null is returned.
     */
    public IPortletRequestInfo getPortletRequestInfo();
}
