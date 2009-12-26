/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.api.portlet;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.url.PortletUrl;
import org.jasig.portal.url.IPortalRequestUtils;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletDelegationManagerImpl implements PortletDelegationManager {
    private static final String DELEGATE_PARENT_PORTLET_URL_PREFIX = "DELEGATE_PARENT_PORTLET_URL_";
    private static final String DELEGATE_PORTLET_ACTION_REDIRECT_URL = "DELEGATE_PORTLET_ACTION_REDIRECT_URL";

    private IPortalRequestUtils portalRequestUtils;
    
    public IPortalRequestUtils getPortalRequestUtils() {
        return this.portalRequestUtils;
    }

    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }
    

    @Override
    public void setParentPortletUrl(HttpServletRequest request, PortletUrl parentPortletUrl) {
        final IPortletWindowId parentPortletWindowId = parentPortletUrl.getTargetWindowId();
        request.setAttribute(DELEGATE_PARENT_PORTLET_URL_PREFIX + parentPortletWindowId.getStringId(), parentPortletUrl);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.api.portlet.PortletDelegationManager#getParentPortletUrl(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindowId)
     */
    @Override
    public PortletUrl getParentPortletUrl(HttpServletRequest request, IPortletWindowId parentPortletWindowId) {
        return (PortletUrl)request.getAttribute(DELEGATE_PARENT_PORTLET_URL_PREFIX + parentPortletWindowId.getStringId());
    }

    @Override
    public void setDelegatePortletActionRedirectUrl(HttpServletRequest request, PortletUrl portletUrl) {
        final HttpServletRequest portletAdaptorParentRequest = this.portalRequestUtils.getPortletAdaptorParentRequest(request);
        portletAdaptorParentRequest.setAttribute(DELEGATE_PORTLET_ACTION_REDIRECT_URL, portletUrl);
    }

    @Override
    public PortletUrl getDelegatePortletActionRedirectUrl(HttpServletRequest request) {
        request = this.portalRequestUtils.getOriginalPortletAdaptorRequest(request);
        return (PortletUrl)request.getAttribute(DELEGATE_PORTLET_ACTION_REDIRECT_URL);
    }
}
