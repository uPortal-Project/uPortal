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

package org.jasig.portal.url.xml;

import javax.servlet.http.HttpServletRequest;

import org.apache.pluto.container.PortletURLProvider.TYPE;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.url.IBasePortalUrl;
import org.jasig.portal.url.ILayoutPortalUrl;
import org.jasig.portal.url.IPortalUrlProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Wrapper class for {@link IPortalUrlProvider} that makes use easier in XSL
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service("xslPortalUrlProvider")
public class XsltPortalUrlProvider {
    public static final String XSLT_PORTAL_URL_PROVIDER = "XSLT_PORTAL_URL_PROVIDER";
    public static final String CURRENT_REQUEST = "CURRENT_REQUEST";
    
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    public static XsltPortalUrlProvider getUrlProvider(Object urlProvider) {
        return (XsltPortalUrlProvider)urlProvider;
    }
    
    public static HttpServletRequest getHttpServletRequest(Object request) {
        return (HttpServletRequest)request;
    }
    
    private IPortalUrlProvider urlProvider;
    private IPortletWindowRegistry portletWindowRegistry;
    

    @Autowired
    public void setUrlProvider(IPortalUrlProvider urlProvider) {
        this.urlProvider = urlProvider;
    }

    @Autowired
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }

    /**
     * @see IPortalUrlProvider#getDefaultUrl(HttpServletRequest)
     */
    public XsltBasePortalUrl getDefaultUrl(HttpServletRequest request) {
        final IBasePortalUrl defaultUrl = this.urlProvider.getDefaultUrl(request);
        return new XsltBasePortalUrl(defaultUrl);
    }

    /**
     * @see IPortalUrlProvider#getFolderUrlByNodeId(HttpServletRequest, String)
     */
    public XsltLayoutPortalUrl getFolderUrlByNodeId(HttpServletRequest request, String folderNodeId) {
        final ILayoutPortalUrl folderUrl = this.urlProvider.getFolderUrlByNodeId(request, folderNodeId);
        return new XsltLayoutPortalUrl(folderUrl);
    }
    
    /**
     * @see IPortalUrlProvider#getPortletUrl(TYPE, HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindowId)
     * @see IPortalUrlProvider#getPortletUrlByFName(TYPE, HttpServletRequest, String)
     * @see IPortalUrlProvider#getPortletUrlByNodeId(TYPE, HttpServletRequest, String)
     */
    public XsltPortletPortalUrl getPortletUrl(HttpServletRequest request, String type) {
        TYPE urlType = TYPE.RENDER;
        if (type != null && (type = type.trim()).length() > 0) {
            try {
                urlType = TYPE.valueOf(type.toUpperCase());
            }
            catch (IllegalArgumentException e) {
                this.logger.warn("Invalid PortletURLProvider.TYPE specified '{}', defaulting to RENDER", type);
            }
        }
        
        return new XsltPortletPortalUrl(request, urlProvider, portletWindowRegistry, urlType);
    }
}
