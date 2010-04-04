/**
 * Copyright (c) 2000-2010, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.portlet.container;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletResourceResponseContext;
import org.jasig.portal.portlet.container.properties.IRequestPropertiesManager;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.url.IPortletUrlCreator;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletResourceResponseContextImpl extends PortletMimeResponseContextImpl implements PortletResourceResponseContext {
    
    public PortletResourceResponseContextImpl(PortletContainer portletContainer, IPortletWindow portletWindow,
            HttpServletRequest containerRequest, HttpServletResponse containerResponse,
            IRequestPropertiesManager requestPropertiesManager, IPortletUrlCreator portletUrlCreator) {

        super(portletContainer, portletWindow, containerRequest, containerResponse, requestPropertiesManager, portletUrlCreator);
        
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletResourceResponseContext#setCharacterEncoding(java.lang.String)
     */
    @Override
    public void setCharacterEncoding(String charset) {
        if (!this.isClosed()) {
            this.servletResponse.setCharacterEncoding(charset);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletResourceResponseContext#setContentLength(int)
     */
    @Override
    public void setContentLength(int len) {
        if (!this.isClosed()) {
            this.servletResponse.setContentLength(len);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletResourceResponseContext#setLocale(java.util.Locale)
     */
    @Override
    public void setLocale(Locale locale) {
        if (!this.isClosed()) {
            this.servletResponse.setLocale(locale);
        }
    }
}
