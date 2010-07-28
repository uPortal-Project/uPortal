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

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.TransformerException;

import org.apache.pluto.container.PortletURLProvider.TYPE;
import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.templates.ElemExtensionCall;
import org.apache.xalan.transformer.TransformerImpl;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.url.IPortalUrlProvider;
import org.jasig.portal.url.IPortletPortalUrl;
import org.w3c.dom.Node;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletUrlXalanElements extends BaseUrlXalanElements<IPortletPortalUrl> {
    public static final String PORTLET_WINDOW_REGISTRY_PARAMETER = IPortletWindowRegistry.class.getName();
    
    public PortletUrlXalanElements() {
        super(IPortletPortalUrl.class);
    }


    /**
     * Creates the appropriate IPortletPortalUrl
     * 
     * @param elem The extension element
     * @param transformer The Xalan transformer
     * @return a new IPortalPortletUrl
     */
    @Override
    protected IPortletPortalUrl createUrl(XSLProcessorContext context, ElemExtensionCall elem) throws TransformerException {
        final TransformerImpl transformer = context.getTransformer();
        
        final HttpServletRequest request = (HttpServletRequest)transformer.getParameter(CURRENT_PORTAL_REQUEST);
        final IPortletWindowRegistry portletWindowRegistry = (IPortletWindowRegistry)transformer.getParameter(PORTLET_WINDOW_REGISTRY_PARAMETER);
        final IPortalUrlProvider portalUrlProvider = (IPortalUrlProvider)transformer.getParameter(PORTAL_URL_PROVIDER_PARAMETER);
        
        final Node contextNode = context.getContextNode();
        
        final String typeName = elem.getAttribute("type", contextNode, transformer);
        final TYPE type;
        if (typeName != null) {
            try {
                type = TYPE.valueOf(typeName.toUpperCase());
            }
            catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("The specified portlet url type '" + typeName + "' is not supported.", e);
            }
        }
        else {
            type = TYPE.RENDER;
        }
        
        final String fname = elem.getAttribute("fname", contextNode, transformer);
        final String windowId = elem.getAttribute("windowId", contextNode, transformer);
        final String layoutNodeId = elem.getAttribute("layoutId", contextNode, transformer);
        
        final IPortletPortalUrl portletUrl;
        if (fname != null && windowId == null && layoutNodeId == null) {
            portletUrl = portalUrlProvider.getPortletUrlByFName(type, request, fname);
        }
        else if (fname == null && windowId != null && layoutNodeId == null) {
            final IPortletWindowId portletWindowId = portletWindowRegistry.getPortletWindowId(windowId);
            portletUrl = portalUrlProvider.getPortletUrl(type, request, portletWindowId);
        }
        else if (fname == null && windowId == null && layoutNodeId != null) {
            portletUrl = portalUrlProvider.getPortletUrlByNodeId(type, request, layoutNodeId);
        }
        else {
            throw new IllegalArgumentException("One and only one target attribute is allowed. Please specify one of: 'fname', 'windowId', 'layoutId'");
        }
        return portletUrl;
    }
    
    

    /* (non-Javadoc)
     * @see org.jasig.portal.url.xml.BaseUrlXalanElements#postProcessUrl(org.jasig.portal.url.IBasePortalUrl, org.apache.xalan.extensions.XSLProcessorContext, org.apache.xalan.templates.ElemExtensionCall)
     */
    @Override
    protected void postProcessUrl(IPortletPortalUrl url, XSLProcessorContext context, ElemExtensionCall elem) throws TransformerException {
        final Node contextNode = context.getContextNode();
        final TransformerImpl transformer = context.getTransformer();
        
        final String state = elem.getAttribute("state", contextNode, transformer);
        if (state != null) {
            url.setWindowState(new WindowState(state));
        }
        
        final String mode = elem.getAttribute("mode", contextNode, transformer);
        if (mode != null) {
            url.setPortletMode(new PortletMode(mode));
        }
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.url.xml.BaseUrlXalanElements#addParameter(org.jasig.portal.url.IBasePortalUrl, java.lang.String, java.lang.String)
     */
    @Override
    protected void addParameter(IPortletPortalUrl url, String name, String value) {
        url.addPortletParameter(name, value);
    }
}
