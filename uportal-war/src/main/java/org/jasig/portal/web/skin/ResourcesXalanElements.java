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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.templates.ElemExtensionCall;
import org.apache.xalan.transformer.TransformerImpl;
import org.w3c.dom.DocumentFragment;

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
	 * Name of {@link Transformer} parameter used to retrieve the {@link ResourcesDao}.
	 */
	public static final String SKIN_RESOURCESDAO_PARAMETER_NAME = ResourcesXalanElements.class.getName() + "SKIN_RESOURCESDAO";
	/**
	 * Name of {@link System} property used to toggle default/aggregated skin output.
	 */
	public static final String AGGREGATED_THEME_PARAMETER = "org.jasig.portal.web.skin.aggregated_theme";
	public static final String DEFAULT_AGGREGATION_ENABLED = "true";
    /**
     * File name for default skin configuration (non-aggregated).
     */
	public static final String DEFAULT_SKIN_FILENAME = "skin.xml";
	/**
	 * File name for aggregated skin configuraiton.
	 */
	public static final String AGGREGATED_SKIN_FILENAME = "uportal3_aggr.skin.xml";
	
	/**
	 * Pulls the {@link Resources} to render from the Transformer parameter
	 * named {@link #SKIN_RESOURCESDAO_PARAMETER_NAME}.
	 * 
	 * @param context
	 * @param elem
	 * @return
	 * @throws TransformerException
	 * @throws ParserConfigurationException
	 */
	public DocumentFragment output(XSLProcessorContext context, ElemExtensionCall elem) throws TransformerException, ParserConfigurationException {
		final TransformerImpl transformer = context.getTransformer();

		final String path = elem.getAttribute("path", context.getContextNode(), transformer);
		final String relativeRoot = FilenameUtils.getPath(path);
		if(logger.isDebugEnabled()) {
			logger.debug("relativeRoot from element path: " + relativeRoot);
		}
		
		boolean aggregatedThemeEnabled = Boolean.parseBoolean(System.getProperty(AGGREGATED_THEME_PARAMETER, DEFAULT_AGGREGATION_ENABLED));
		
		final String primaryPath;
		final String secondaryPath;
		if(aggregatedThemeEnabled) {
			primaryPath = path + AGGREGATED_SKIN_FILENAME;
			secondaryPath = path + DEFAULT_SKIN_FILENAME;
		} else {
		    primaryPath = path + DEFAULT_SKIN_FILENAME;
			secondaryPath = path + AGGREGATED_SKIN_FILENAME;
		}
		
		final ResourcesDao resourcesDao = (ResourcesDao) transformer.getParameter(SKIN_RESOURCESDAO_PARAMETER_NAME);
		String portalContextPath = (String) transformer.getParameter("CONTEXT_PATH");
		if (!portalContextPath.endsWith("/")) {
		    portalContextPath = portalContextPath + "/";
		}
		
        DocumentFragment headFragment = resourcesDao.getResourcesFragment(primaryPath, portalContextPath + relativeRoot);
		if(null == headFragment) {		
			if(logger.isWarnEnabled()) {
				logger.warn(primaryPath.toString() + " not found, attempting " + secondaryPath.toString());
			}
			headFragment = resourcesDao.getResourcesFragment(secondaryPath, portalContextPath + relativeRoot);
		}
		// if it's still null, we have to bail out
		if(null == headFragment) {
			throw new IllegalStateException("no skin configuration found at " + primaryPath.toString() + " or " + secondaryPath.toString());
		}
		
		return headFragment;
	}
}
