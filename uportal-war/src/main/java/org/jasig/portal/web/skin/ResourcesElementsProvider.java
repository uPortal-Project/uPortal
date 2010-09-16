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
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.NodeList;

/**
 * Used by Xalan to transform a {@link Resources} to the correct HTML head elements
 * (script tags for javascript and link tags for css).
 * 
 * @author Nicholas Blair, npblair@wisc.edu
 *
 */
@Service("resourcesElementsProvider")
public class ResourcesElementsProvider {

	protected final Log logger = LogFactory.getLog(this.getClass());
	
	public static final String RESOURCES_ELEMENTS_PROVIDER = "RESOURCES_ELEMENTS_PROVIDER";
	
	
	/**
     * File name for default skin configuration (non-aggregated).
     */
	public static final String DEFAULT_SKIN_FILENAME = "skin.xml";
	/**
	 * File name for aggregated skin configuraiton.
	 */
	public static final String AGGREGATED_SKIN_FILENAME = "uportal3_aggr.skin.xml";
	
	public static ResourcesElementsProvider getElmenentsProvider(Object object) {
	    return (ResourcesElementsProvider) object;
	}
    
    public static HttpServletRequest getHttpServletRequest(Object request) {
        return (HttpServletRequest)request;
    }

    
	private ResourcesDao resourcesDao;
	
	@Autowired
	public void setResourcesDao(ResourcesDao resourcesDao) {
        this.resourcesDao = resourcesDao;
    }

    /**
	 */
	public NodeList output(HttpServletRequest request, String path) throws ParserConfigurationException {
		final String relativeRoot = FilenameUtils.getPath(path);
		if(logger.isDebugEnabled()) {
			logger.debug("relativeRoot from element path: " + relativeRoot);
		}
		
		boolean aggregatedThemeEnabled = Boolean.parseBoolean(System.getProperty(ResourcesAggregationHelper.AGGREGATED_THEME_PARAMETER, ResourcesAggregationHelper.DEFAULT_AGGREGATION_ENABLED));
		
        final String primaryPath;
        final String secondaryPath;
        if (aggregatedThemeEnabled) {
            primaryPath = path + AGGREGATED_SKIN_FILENAME;
            secondaryPath = path + DEFAULT_SKIN_FILENAME;
        }
        else {
            primaryPath = path + DEFAULT_SKIN_FILENAME;
            secondaryPath = path + AGGREGATED_SKIN_FILENAME;
        }
		
		String portalContextPath = request.getContextPath();
		if (!portalContextPath.endsWith("/")) {
		    portalContextPath = portalContextPath + "/";
		}
		
		NodeList headFragment = resourcesDao.getResourcesFragment(primaryPath, portalContextPath + relativeRoot);
        
        if (null == headFragment) {
            if (logger.isWarnEnabled()) {
                logger.warn(primaryPath.toString() + " not found, attempting " + secondaryPath.toString());
            }
            headFragment = resourcesDao.getResourcesFragment(secondaryPath, portalContextPath + relativeRoot);
        }
        
        // if it's still null, we have to bail out
        if (null == headFragment) {
            throw new IllegalStateException("no skin configuration found at " + primaryPath.toString() + " or " + secondaryPath.toString());
        }
		
        return headFragment;
	}
}
