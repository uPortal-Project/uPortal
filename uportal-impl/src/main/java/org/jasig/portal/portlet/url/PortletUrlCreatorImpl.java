/**
 * Copyright (c) 2000-2010, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.portlet.url;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.container.PortletURLProvider;
import org.apache.pluto.container.ResourceURLProvider;
import org.apache.pluto.container.PortletURLProvider.TYPE;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service
public class PortletUrlCreatorImpl implements IPortletUrlCreator {
    private IPortletUrlSyntaxProvider portletUrlSyntaxProvider;

    @Autowired
    public void setPortletUrlSyntaxProvider(IPortletUrlSyntaxProvider portletUrlSyntaxProvider) {
        this.portletUrlSyntaxProvider = portletUrlSyntaxProvider;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.url.IPortletUrlCreator#createActionUrlProvider(org.jasig.portal.portlet.om.IPortletWindow, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public PortletURLProvider createActionUrlProvider(IPortletWindow portletWindow, HttpServletRequest containerRequest, HttpServletResponse containerResponse) {
        return this.createUrlProvider(TYPE.ACTION, portletWindow, containerRequest, containerResponse);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.url.IPortletUrlCreator#createRenderUrlProvider(org.jasig.portal.portlet.om.IPortletWindow, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public PortletURLProvider createRenderUrlProvider(IPortletWindow portletWindow, HttpServletRequest containerRequest, HttpServletResponse containerResponse) {
        return this.createUrlProvider(TYPE.RENDER, portletWindow, containerRequest, containerResponse);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.url.IPortletUrlCreator#createResourceUrlProvider(org.jasig.portal.portlet.om.IPortletWindow, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ResourceURLProvider createResourceUrlProvider(IPortletWindow portletWindow, HttpServletRequest containerRequest, HttpServletResponse containerResponse) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Resource URLs are not implemented yet");
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.url.IPortletUrlCreator#createUrlProvider(org.apache.pluto.container.PortletURLProvider.TYPE, org.jasig.portal.portlet.om.IPortletWindow, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public PortletURLProvider createUrlProvider(TYPE type, IPortletWindow portletWindow, HttpServletRequest containerRequest, HttpServletResponse containerResponse) {
        return new PortletURLProviderImpl(type, portletWindow, containerRequest, this.portletUrlSyntaxProvider);
    }
}
