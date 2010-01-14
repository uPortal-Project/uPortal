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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.rendering.IPortalRenderingPipeline;
import org.springframework.beans.factory.annotation.Required;

/**
 * Class to facilitate enabling/disabling Resources aggregation.
 * 
 * @author Nicholas Blair, npblair@wisc.edu
 *
 */
public class ResourcesAggregationHelper {

	protected final Log logger = LogFactory.getLog(this.getClass());
	
	private IPortalRenderingPipeline portalRenderingPipeline;
	
	/**
	 * @param portalRenderingPipeline the portalRenderingPipeline to set
	 */
	@Required
	public void setPortalRenderingPipeline(
			IPortalRenderingPipeline portalRenderingPipeline) {
		this.portalRenderingPipeline = portalRenderingPipeline;
	}
	/**
	 * 
	 * @return true if aggregation is currently enabled.
	 */
	public boolean isAggregationEnabled() {
		return Boolean.parseBoolean(System.getProperty(ResourcesXalanElements.AGGREGATED_THEME_PARAMETER, ResourcesXalanElements.DEFAULT_AGGREGATION_ENABLED));
	}
	/**
	 * Invokes {@link Boolean#parseBoolean(String)} on the argument and passes the result
	 * into {@link #setAggregationEnabled(boolean)}.
	 * @see Boolean#parseBoolean(String)
	 * @param valueAsString 
	 */
	public void setAggregationEnabled(String valueAsString) {
		setAggregationEnabled(Boolean.parseBoolean(valueAsString));
	}
	/**
	 * Toggle resources aggregation (if and only if value parameter differs from current value).
	 * Additionally invokes {@link IPortalRenderingPipeline#clearSystemCharacterCache()}.
	 * @param value
	 */
	public void setAggregationEnabled(boolean value) {
		boolean currentValue = isAggregationEnabled();
		if(currentValue != value) {
			System.setProperty(ResourcesXalanElements.AGGREGATED_THEME_PARAMETER, Boolean.toString(value));
			logger.warn("resources aggregation set: " + value);
			portalRenderingPipeline.clearSystemCharacterCache();
		}
	}
	
	/**
	 * shortcut to {@link #setAggregationEnabled(boolean)} with true.
	 */
	public void enableAggregation() {
		setAggregationEnabled(true);
	}
	/**
	 * shortcut to {@link #setAggregationEnabled(boolean)} with false.
	 */
	public void disableAggregation() {
		setAggregationEnabled(false);
	}
	
	
}
