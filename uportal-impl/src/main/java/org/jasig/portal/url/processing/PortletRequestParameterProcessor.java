/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.url.processing;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlet.url.IPortletRequestParameterManager;
import org.jasig.portal.portlet.url.IPortletUrlSyntaxProvider;
import org.jasig.portal.portlet.url.PortletUrl;
import org.jasig.portal.portlet.url.RequestType;
import org.jasig.portal.url.IWritableHttpServletRequest;

/**
 * Parse out the parameters targeted to each portlet and store them in the manager. When constructing the request
 * object to send to pluto the parameters will get pulled back out of the manager and used as the only parameters in
 * the wrapper.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletRequestParameterProcessor implements IRequestParameterProcessor {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private IPortletUrlSyntaxProvider portletUrlSyntaxProvider;
    private IPortletRequestParameterManager portletRequestParameterManager;
    private IPortletWindowRegistry portletWindowRegistry;

    /* (non-Javadoc)
     * @see org.jasig.portal.url.processing.IRequestParameterProcessor#processParameters(org.jasig.portal.url.IWritableHttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public boolean processParameters(IWritableHttpServletRequest request, HttpServletResponse response) {
        final Map<IPortletWindowId, PortletUrl> parsedPortletParameters = this.portletUrlSyntaxProvider.parsePortletParameters(request);
        
        //TODO should parameters be applied directly to PortletWindow objects?
    }
}
