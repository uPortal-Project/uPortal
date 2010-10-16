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

/**
 * 
 */
package org.jasig.portal.web.skin;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.templates.ElemExtensionCall;
import org.apache.xalan.transformer.TransformerImpl;
import org.jasig.resource.aggr.om.Resources;
import org.jasig.resource.aggr.util.ResourcesElementsProvider;
import org.w3c.dom.NodeList;

/**
 * Used by Xalan to transform a {@link Resources} to the correct HTML head elements
 * (script tags for javascript and link tags for css).
 * 
 * @author Nicholas Blair, npblair@wisc.edu
 *
 */
public class ResourcesXalanElements {

	protected final Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * Name of {@link Transformer} parameter used to retrieve the {@link ResourcesElementsProvider}.
	 */
	public static final String SKIN_RESOURCESDAO_PARAMETER_NAME = ResourcesXalanElements.class.getName() + ".SKIN_RESOURCESDAO";
	
	public static final String CURRENT_REQUEST = ResourcesXalanElements.class.getName() + ".CURRENT_REQUEST";
	
	public String parameter(XSLProcessorContext context, ElemExtensionCall elem) throws TransformerException {
	    final TransformerImpl transformer = context.getTransformer();

        final String path = elem.getAttribute("path", context.getContextNode(), transformer);
        final String name = elem.getAttribute("name", context.getContextNode(), transformer);
        
        final ResourcesElementsProvider elementsProvider = (ResourcesElementsProvider) transformer.getParameter(SKIN_RESOURCESDAO_PARAMETER_NAME);
        final HttpServletRequest request = (HttpServletRequest) transformer.getParameter(CURRENT_REQUEST);
        
        return elementsProvider.getResourcesParameter(request, path, name);
	}
	
	public NodeList output(XSLProcessorContext context, ElemExtensionCall elem) throws TransformerException {
		final TransformerImpl transformer = context.getTransformer();

		final String path = elem.getAttribute("path", context.getContextNode(), transformer);
		
		final ResourcesElementsProvider elementsProvider = (ResourcesElementsProvider) transformer.getParameter(SKIN_RESOURCESDAO_PARAMETER_NAME);
		final HttpServletRequest request = (HttpServletRequest) transformer.getParameter(CURRENT_REQUEST);
		
		return elementsProvider.getResourcesXmlFragment(request, path);
	}
}
