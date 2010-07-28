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
import javax.xml.transform.TransformerException;

import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.templates.ElemExtensionCall;
import org.apache.xalan.transformer.TransformerImpl;
import org.jasig.portal.url.IBasePortalUrl;
import org.jasig.portal.url.IPortalUrlProvider;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortalUrlXalanElements extends BaseUrlXalanElements<IBasePortalUrl> {
    public PortalUrlXalanElements() {
        super(IBasePortalUrl.class);
    }

    /**
     * Creates the appropriate IBasePortalUrl
     * 
     * @param elem The extension element
     * @param transformer The Xalan transformer
     * @return a new IPortalPortletUrl
     */
    @Override
    protected IBasePortalUrl createUrl(XSLProcessorContext context, ElemExtensionCall elem) throws TransformerException {
        final TransformerImpl transformer = context.getTransformer();
        
        final HttpServletRequest request = (HttpServletRequest)transformer.getParameter(CURRENT_PORTAL_REQUEST);
        final IPortalUrlProvider portalUrlProvider = (IPortalUrlProvider)transformer.getParameter(PORTAL_URL_PROVIDER_PARAMETER);
        
        final Node contextNode = context.getContextNode();
        final NamedNodeMap attributes = contextNode.getAttributes();
        if (attributes.getLength() != 0) {
            throw new IllegalArgumentException("No attributes are allowed on a portal URL element.");
        }
        
        return portalUrlProvider.getDefaultUrl(request);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.url.xml.BaseUrlXalanElements#addParameter(org.jasig.portal.url.IBasePortalUrl, java.lang.String, java.lang.String)
     */
    @Override
    protected void addParameter(IBasePortalUrl url, String name, String value) {
        url.addPortalParameter(name, value);
    }
}
