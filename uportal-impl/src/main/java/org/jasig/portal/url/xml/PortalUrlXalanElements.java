/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.url.xml;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.TransformerException;

import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.templates.ElemExtensionCall;
import org.apache.xalan.transformer.TransformerImpl;
import org.jasig.portal.url.IBasePortalUrl;
import org.jasig.portal.url.IPortalUrlProvider;
import org.w3c.dom.Node;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortalUrlXalanElements extends BaseUrlXalanElements<IBasePortalUrl> {
    public PortalUrlXalanElements() {
        super(IBasePortalUrl.class);
    }
    
    public String url(XSLProcessorContext context, ElemExtensionCall elem) throws TransformerException {
        final TransformerImpl transformer = context.getTransformer();
        
        try {
            // retrieve configuration
            final IBasePortalUrl basePortalUrl = this.createBasePortalUrl(context, elem);
            
            this.transform(basePortalUrl, transformer, elem);

            return basePortalUrl.getUrlString();
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
            final IBasePortalUrl portletUrl = this.getCurrentPortalUrl(transformer);
            
            final Node contextNode = context.getContextNode();
            final String name = elem.getAttribute("name", contextNode, transformer);
            String value = elem.getAttribute("value", contextNode, transformer);
            
            //No value attribute, try running any nested part of the XSLT
            if (value == null) {
                final StringBuildingContentHandler contentHandler = new StringBuildingContentHandler(transformer);
                transformer.executeChildTemplates(elem, contentHandler);
                value = contentHandler.toString();
            }

            portletUrl.addPortalParameter(name, value);
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
     * Creates the appropriate IBasePortalUrl
     * 
     * @param elem The extension element
     * @param transformer The Xalan transformer
     * @return a new IPortalPortletUrl
     */
    protected IBasePortalUrl createBasePortalUrl(XSLProcessorContext context, ElemExtensionCall elem) throws TransformerException {
        final TransformerImpl transformer = context.getTransformer();
        
        final HttpServletRequest request = (HttpServletRequest)transformer.getParameter(CURRENT_PORTAL_REQUEST);
        final IPortalUrlProvider portalUrlProvider = (IPortalUrlProvider)transformer.getParameter(PORTAL_URL_PROVIDER_PARAMETER);
        
        final Node contextNode = context.getContextNode();
        final String layoutNodeId = elem.getAttribute("layoutId", contextNode, transformer);
        
        final IBasePortalUrl basePortalUrl;
        if (layoutNodeId != null) {
            basePortalUrl = portalUrlProvider.getFolderUrlByNodeId(request, layoutNodeId);
        }
        else {
            basePortalUrl = portalUrlProvider.getDefaultUrl(request);
        }
        
        return basePortalUrl;
    }
}
