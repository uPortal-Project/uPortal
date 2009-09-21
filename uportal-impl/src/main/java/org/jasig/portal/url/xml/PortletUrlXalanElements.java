/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.url.xml;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.TransformerException;

import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.templates.ElemExtensionCall;
import org.apache.xalan.transformer.TransformerImpl;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.url.IPortalPortletUrl;
import org.jasig.portal.url.IPortalUrlProvider;
import org.w3c.dom.Node;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletUrlXalanElements extends BaseUrlXalanElements<IPortalPortletUrl> {
    public static final String PORTLET_WINDOW_REGISTRY_PARAMETER = IPortletWindowRegistry.class.getName();
    
    public PortletUrlXalanElements() {
        super(IPortalPortletUrl.class);
    }

    public String url(XSLProcessorContext context, ElemExtensionCall elem) throws TransformerException {
        final TransformerImpl transformer = context.getTransformer();
        
        try {
            // retrieve configuration
            final IPortalPortletUrl portletUrl = this.createPortalPortletUrl(context, elem);
            
            this.transform(portletUrl, transformer, elem);
            
            final Node contextNode = context.getContextNode();
            
            final String state = elem.getAttribute("state", contextNode, transformer);
            if (state != null) {
                portletUrl.setWindowState(new WindowState(state));
            }
            
            final String mode = elem.getAttribute("mode", contextNode, transformer);
            if (mode != null) {
                portletUrl.setPortletMode(new PortletMode(mode));
            }
            
            final String action = elem.getAttribute("action", contextNode, transformer);
            portletUrl.setAction(Boolean.parseBoolean(action));
            
            return portletUrl.getUrlString();
        }
        catch (Throwable t) {
            if (t instanceof TransformerException) {
                throw (TransformerException)t;
            }
            
            final RuntimeException re;
            if (t instanceof RuntimeException) {
                re = (RuntimeException)t;
            }
            else {
                re = new RuntimeException(t);
            }
            
            transformer.setExceptionThrown(re);
            throw re;
        }
    }
    
    public void param(XSLProcessorContext context, ElemExtensionCall elem) throws TransformerException {
        final TransformerImpl transformer = context.getTransformer();
        
        try {
            // retrieve configuration
            final IPortalPortletUrl portletUrl = this.getCurrentPortalUrl(transformer);
            
            final Node contextNode = context.getContextNode();
            final String name = elem.getAttribute("name", contextNode, transformer);
            String value = elem.getAttribute("value", contextNode, transformer);
            
            //No value attribute, try running any nested part of the XSLT
            if (value == null) {
                final StringBuildingContentHandler contentHandler = new StringBuildingContentHandler(transformer);
                transformer.executeChildTemplates(elem, contentHandler);
                value = contentHandler.toString();
            }

            portletUrl.addPortletParameter(name, value);
        }
        catch (Throwable t) {
            if (t instanceof TransformerException) {
                throw (TransformerException)t;
            }
            
            final RuntimeException re;
            if (t instanceof RuntimeException) {
                re = (RuntimeException)t;
            }
            else {
                re = new RuntimeException(t);
            }
            
            transformer.setExceptionThrown(re);
            throw re;
        }
    }


    /**
     * Creates the appropriate IPortalPortletUrl
     * 
     * @param elem The extension element
     * @param transformer The Xalan transformer
     * @return a new IPortalPortletUrl
     */
    protected IPortalPortletUrl createPortalPortletUrl(XSLProcessorContext context, ElemExtensionCall elem) throws TransformerException {
        final TransformerImpl transformer = context.getTransformer();
        
        final HttpServletRequest request = (HttpServletRequest)transformer.getParameter(CURRENT_PORTAL_REQUEST);
        final IPortletWindowRegistry portletWindowRegistry = (IPortletWindowRegistry)transformer.getParameter(PORTLET_WINDOW_REGISTRY_PARAMETER);
        final IPortalUrlProvider portalUrlProvider = (IPortalUrlProvider)transformer.getParameter(PORTAL_URL_PROVIDER_PARAMETER);
        
        final Node contextNode = context.getContextNode();
        final String fname = elem.getAttribute("fname", contextNode, transformer);
        final String windowId = elem.getAttribute("windowId", contextNode, transformer);
        final String layoutNodeId = elem.getAttribute("layoutId", contextNode, transformer);
        
        final IPortalPortletUrl portletUrl;
        if (fname != null && windowId == null && layoutNodeId == null) {
            portletUrl = portalUrlProvider.getPortletUrlByFName(request, fname);
        }
        else if (fname == null && windowId != null && layoutNodeId == null) {
            final IPortletWindowId portletWindowId = portletWindowRegistry.getPortletWindowId(windowId);
            portletUrl = portalUrlProvider.getPortletUrl(request, portletWindowId);
        }
        else if (fname == null && windowId == null && layoutNodeId != null) {
            portletUrl = portalUrlProvider.getPortletUrlByNodeId(request, layoutNodeId);
        }
        else {
            throw new IllegalArgumentException("One and only one target attribute is allowed. Please specify one of: 'fname', 'windowId', 'layoutId'");
        }
        return portletUrl;
    }
}
