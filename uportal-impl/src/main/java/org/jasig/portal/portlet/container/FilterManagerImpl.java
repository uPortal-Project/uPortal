/**
 * 
 */
package org.jasig.portal.portlet.container;

import java.io.IOException;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventPortlet;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.ResourceServingPortlet;

import org.apache.pluto.container.FilterManager;
import org.apache.pluto.container.PortletWindow;
import org.apache.pluto.container.om.portlet.Filter;
import org.apache.pluto.container.om.portlet.FilterMapping;
import org.apache.pluto.container.om.portlet.PortletApplicationDefinition;
import org.apache.pluto.container.om.portlet.PortletDefinition;

/**
 * @author Nicholas Blair, npblair@wisc.edu
 *
 */
public class FilterManagerImpl implements FilterManager {

	public static final String WILDCARD = "*";
	private final PortletApplicationDefinition portletApplicationDefinition;
	private final String targetLifeCycle;
	private final String portletName;
	private final FilterChainImpl filterChain;
	
	/**
	 * 
	 * @param portletWindow
	 * @param targetLifeCycle
	 */
	public FilterManagerImpl(PortletWindow portletWindow, String targetLifeCycle) {
		this.targetLifeCycle = targetLifeCycle;
		PortletDefinition portletDefinition = portletWindow.getPortletDefinition();
		this.portletApplicationDefinition = portletDefinition.getApplication();
		this.portletName = portletWindow.getPortletDefinition().getPortletName();
		portletApplicationDefinition.getFilters();
		filterChain = new FilterChainImpl(this.targetLifeCycle);
		setupFilterChain();
	}

	/**
	 * 
	 */
	protected void setupFilterChain() {
		List<? extends FilterMapping> filterMappings = this.portletApplicationDefinition.getFilterMappings();
		if(null != filterMappings) {
			for(FilterMapping filterMapping : filterMappings) {
				if(doesFilterMappingApplyToPortletName(filterMapping, portletName)) {
					// filter applies to portlet, now check for match with lifecycle
					Filter filter = this.portletApplicationDefinition.getFilter(filterMapping.getFilterName());
					List<String> filterLifecycles = filter.getLifecycles();
					if(filterLifecycles.contains(targetLifeCycle)) {
						// we've found a match!
						filterChain.addFilter(filter);
					}
				}
			}
		}
	}
	/* (non-Javadoc)
	 * @see org.apache.pluto.container.FilterManager#processFilter(javax.portlet.ActionRequest, javax.portlet.ActionResponse, javax.portlet.Portlet, javax.portlet.PortletContext)
	 */
	@Override
	public void processFilter(ActionRequest req, ActionResponse res,
			Portlet portlet, PortletContext portletContext)
	throws PortletException, IOException {
	    filterChain.setPortlet(portlet);
		filterChain.doFilter(req, res);
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.FilterManager#processFilter(javax.portlet.RenderRequest, javax.portlet.RenderResponse, javax.portlet.Portlet, javax.portlet.PortletContext)
	 */
	@Override
	public void processFilter(RenderRequest req, RenderResponse res,
			Portlet portlet, PortletContext portletContext)
	throws PortletException, IOException {
        filterChain.setPortlet(portlet);
		filterChain.doFilter(req, res);
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.FilterManager#processFilter(javax.portlet.ResourceRequest, javax.portlet.ResourceResponse, javax.portlet.ResourceServingPortlet, javax.portlet.PortletContext)
	 */
	@Override
	public void processFilter(ResourceRequest req, ResourceResponse res,
			ResourceServingPortlet resourceServingPortlet,
			PortletContext portletContext) throws PortletException, IOException {
		filterChain.setResourceServingPortlet(resourceServingPortlet);
		filterChain.doFilter(req, res);
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.FilterManager#processFilter(javax.portlet.EventRequest, javax.portlet.EventResponse, javax.portlet.EventPortlet, javax.portlet.PortletContext)
	 */
	@Override
	public void processFilter(EventRequest req, EventResponse res,
			EventPortlet eventPortlet, PortletContext portletContext)
	throws PortletException, IOException {
		filterChain.setEventPortlet(eventPortlet);
		filterChain.doFilter(req, res);
	}

	/**
	 * {@link FilterMapping} supports the use of '*' as a wildcard.
	 * 
	 * @param filterMapping
	 * @param portletName
	 * @return true if the {@link FilterMapping} is applicable to the specified {@link String} portletName
	 */
	protected boolean doesFilterMappingApplyToPortletName(final FilterMapping filterMapping, final String portletName) {
		List<String> applicablePortletNames = filterMapping.getPortletNames();
		for(String portletNameFromFilterList : applicablePortletNames) {
			if(WILDCARD.equals(portletNameFromFilterList)) {
				return true;
			}  else if (portletNameFromFilterList.equals(portletName)) {
				return true;
			} else if (portletNameFromFilterList.endsWith(WILDCARD)){
				portletNameFromFilterList = portletNameFromFilterList.substring(0, portletNameFromFilterList.length()-1);
				if (portletName.length()>= portletNameFromFilterList.length()){
					if (portletName.substring(0, portletNameFromFilterList.length()).equals(portletNameFromFilterList)){
						return true;
					}
				}
			}
		}
		return false;

	}

}
