/**
 * Copyright (c) 2000-2010, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.portlet.container;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.container.PortletActionResponseContext;
import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletURLProvider;
import org.jasig.portal.portlet.container.properties.IRequestPropertiesManager;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.url.IPortletUrlCreator;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletActionResponseContextImpl extends PortletStateAwareResponseContextImpl implements PortletActionResponseContext {
    private boolean redirect;
    private String redirectLocation;
    private String renderURLParamName;
    
    public PortletActionResponseContextImpl(PortletContainer portletContainer, IPortletWindow portletWindow,
            HttpServletRequest containerRequest, HttpServletResponse containerResponse,
            IRequestPropertiesManager requestPropertiesManager, IPortletUrlCreator portletUrlCreator) {
        super(portletContainer, portletWindow, containerRequest, containerResponse, 
                requestPropertiesManager, portletUrlCreator);
    }

    public String getResponseURL() {
        if (!isReleased()) {
            this.close();
            
            //if not redirect or there is a render url parameter name
            if (!redirect || renderURLParamName != null) {
                final PortletURLProvider renderUrlProvider = this.portletUrlCreator.createRenderUrlProvider(this.portletWindow, this.containerRequest, this.containerResponse);
                if (redirect) {
                    try {
                        return this.redirectLocation + "?" + 
                            URLEncoder.encode(renderURLParamName, "UTF-8") + "=" + 
                            URLEncoder.encode(renderUrlProvider.toURL(), "UTF-8");
                    }
                    catch (UnsupportedEncodingException e) {
                        // Cannot happen: UTF-8 is a built-in/required encoder
                        return null;
                    }
                }

                return renderUrlProvider.toURL();
            }

            return this.redirectLocation;
        }
        
        return null;
    }

    public boolean isRedirect() {
        return redirect;
    }

    public void setRedirect(String location) {
        setRedirect(location, null);
    }

    public void setRedirect(String location, String renderURLParamName) {
        if (!isClosed()) {
            this.redirectLocation = location;
            this.renderURLParamName = renderURLParamName;
            this.redirect = true;
        }
    }
}
