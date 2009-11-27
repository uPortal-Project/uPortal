/**
 * 
 */
package org.jasig.portal.web.skin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.rendering.StaticRenderingPipeline;

/**
 * Class to facilitate enabling/disabling Resources aggregation.
 * 
 * @author Nicholas Blair, npblair@wisc.edu
 *
 */
public class ResourcesAggregationHelper {

	private Log log = LogFactory.getLog(this.getClass());
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
	 * Additionally invokes {@link StaticRenderingPipeline#clearSystemCharacterCache()}.
	 * @param value
	 */
	public void setAggregationEnabled(boolean value) {
		boolean currentValue = isAggregationEnabled();
		if(currentValue != value) {
			System.setProperty(ResourcesXalanElements.AGGREGATED_THEME_PARAMETER, Boolean.toString(value));
			log.warn("resources aggregation set: " + value);
			StaticRenderingPipeline.clearSystemCharacterCache();
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
