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
import org.jasig.portal.url.ILayoutPortalUrl;
import org.jasig.portal.url.IPortalUrlProvider;
import org.w3c.dom.Node;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class LayoutUrlXalanElements extends BaseUrlXalanElements<ILayoutPortalUrl> {
    
    public LayoutUrlXalanElements() {
        super(ILayoutPortalUrl.class);
    }

    /**
     * Creates the appropriate ILayoutPortalUrl
     * 
     * @param elem The extension element
     * @param transformer The Xalan transformer
     * @return a new ILayoutPortalUrl
     */
    @Override
    protected ILayoutPortalUrl createUrl(XSLProcessorContext context, ElemExtensionCall elem) throws TransformerException {
        final TransformerImpl transformer = context.getTransformer();
        
        final HttpServletRequest request = (HttpServletRequest)transformer.getParameter(CURRENT_PORTAL_REQUEST);
        final IPortalUrlProvider portalUrlProvider = (IPortalUrlProvider)transformer.getParameter(PORTAL_URL_PROVIDER_PARAMETER);
        
        final Node contextNode = context.getContextNode();
        final String layoutNodeId = elem.getAttribute("layoutId", contextNode, transformer);
        
        if (layoutNodeId == null) {
            throw new IllegalArgumentException("layoutId attribute is required and the only valid layout url attribute.");
        }
        
        return portalUrlProvider.getFolderUrlByNodeId(request, layoutNodeId);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.url.xml.BaseUrlXalanElements#postProcessUrl(org.jasig.portal.url.IBasePortalUrl, org.apache.xalan.extensions.XSLProcessorContext, org.apache.xalan.templates.ElemExtensionCall)
     */
    @Override
    protected void postProcessUrl(ILayoutPortalUrl url, XSLProcessorContext context, ElemExtensionCall elem) throws TransformerException {
        final Node contextNode = context.getContextNode();
        final TransformerImpl transformer = context.getTransformer();
        
        final String action = elem.getAttribute("action", contextNode, transformer);
        url.setAction(Boolean.parseBoolean(action));
        
        final String renderInNormal = elem.getAttribute("renderInNormal", contextNode, transformer);
        url.setRenderInNormal(Boolean.parseBoolean(renderInNormal));
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.xml.BaseUrlXalanElements#addParameter(org.jasig.portal.url.IBasePortalUrl, java.lang.String, java.lang.String)
     */
    @Override
    protected void addParameter(ILayoutPortalUrl url, String name, String value) {
        url.addLayoutParameter(name, value);
    }
}
