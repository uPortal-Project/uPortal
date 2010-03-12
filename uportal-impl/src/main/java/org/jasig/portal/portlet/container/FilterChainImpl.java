/**
 * 
 */
package org.jasig.portal.portlet.container;

import java.io.IOException;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.filter.ActionFilter;
import javax.portlet.filter.EventFilter;
import javax.portlet.filter.FilterChain;
import javax.portlet.filter.RenderFilter;
import javax.portlet.filter.ResourceFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.container.om.portlet.Filter;

/**
 * 
 * 
 * @author Nicholas Blair, npblair@wisc.edu
 *
 */
public class FilterChainImpl implements FilterChain {

	protected final Log logger = LogFactory.getLog(this.getClass());
	private final List<Filter> filters;
	private final String lifeCycle;
	private final PortletContext portletContext;
	
	/**
	 * 
	 * @param filters
	 * @param lifeCycle
	 * @param portletContext
	 */
	public FilterChainImpl(List<Filter> filters, String lifeCycle, PortletContext portletContext) {
		this.filters = filters;
		this.lifeCycle = lifeCycle;
		this.portletContext = portletContext;
	}
	
	/**
	 * @return the filters
	 */
	public List<Filter> getFilters() {
		return filters;
	}

	/**
	 * @return the lifeCycle
	 */
	public String getLifeCycle() {
		return lifeCycle;
	}

	/* (non-Javadoc)
	 * @see javax.portlet.filter.FilterChain#doFilter(javax.portlet.ActionRequest, javax.portlet.ActionResponse)
	 */
	@Override
	public void doFilter(ActionRequest request, ActionResponse response)
			throws IOException, PortletException {
		
		ClassLoader classLoader = getCurrentThreadClassLoader();
		for(Filter filter: filters) {
			final String filterClass = filter.getFilterClass();
			try {
				Class<?> filterClazz = classLoader.loadClass(filterClass);
				ActionFilter actionFilter = (ActionFilter) filterClazz.newInstance();
				FilterConfigImpl config = new FilterConfigImpl(filter.getFilterName(), filter.getInitParams(), portletContext);
				
				// Begin Filter lifecycle: init, doFilter, destroy
				actionFilter.init(config);
				actionFilter.doFilter(request, response, this);
				actionFilter.destroy();
				// End Filter lifecycle
				
			} catch (ClassNotFoundException e) {
				logger.error(e);
			} catch (InstantiationException e) {
				logger.error(e);
			} catch (IllegalAccessException e) {
				logger.error(e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.portlet.filter.FilterChain#doFilter(javax.portlet.EventRequest, javax.portlet.EventResponse)
	 */
	@Override
	public void doFilter(EventRequest request, EventResponse response)
			throws IOException, PortletException {
		ClassLoader classLoader = getCurrentThreadClassLoader();
		for(Filter filter: filters) {
			final String filterClass = filter.getFilterClass();
			try {
				Class<?> filterClazz = classLoader.loadClass(filterClass);
				EventFilter eventFilter = (EventFilter) filterClazz.newInstance();
				FilterConfigImpl config = new FilterConfigImpl(filter.getFilterName(), filter.getInitParams(), portletContext);
				
				// Begin Filter lifecycle: init, doFilter, destroy
				eventFilter.init(config);
				eventFilter.doFilter(request, response, this);
				eventFilter.destroy();
				// End Filter lifecycle
				
			} catch (ClassNotFoundException e) {
				logger.error(e);
			} catch (InstantiationException e) {
				logger.error(e);
			} catch (IllegalAccessException e) {
				logger.error(e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.portlet.filter.FilterChain#doFilter(javax.portlet.RenderRequest, javax.portlet.RenderResponse)
	 */
	@Override
	public void doFilter(RenderRequest request, RenderResponse response)
			throws IOException, PortletException {
		ClassLoader classLoader = getCurrentThreadClassLoader();
		for(Filter filter: filters) {
			final String filterClass = filter.getFilterClass();
			try {
				Class<?> filterClazz = classLoader.loadClass(filterClass);
				RenderFilter renderFilter = (RenderFilter) filterClazz.newInstance();
				FilterConfigImpl config = new FilterConfigImpl(filter.getFilterName(), filter.getInitParams(), portletContext);
				
				// Begin Filter lifecycle: init, doFilter, destroy
				renderFilter.init(config);
				renderFilter.doFilter(request, response, this);
				renderFilter.destroy();
				// End Filter lifecycle
				
			} catch (ClassNotFoundException e) {
				logger.error(e);
			} catch (InstantiationException e) {
				logger.error(e);
			} catch (IllegalAccessException e) {
				logger.error(e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.portlet.filter.FilterChain#doFilter(javax.portlet.ResourceRequest, javax.portlet.ResourceResponse)
	 */
	@Override
	public void doFilter(ResourceRequest request, ResourceResponse response)
			throws IOException, PortletException {
		ClassLoader classLoader = getCurrentThreadClassLoader();
		for(Filter filter: filters) {
			final String filterClass = filter.getFilterClass();
			try {
				Class<?> filterClazz = classLoader.loadClass(filterClass);
				ResourceFilter resourceFilter = (ResourceFilter) filterClazz.newInstance();
				FilterConfigImpl config = new FilterConfigImpl(filter.getFilterName(), filter.getInitParams(), portletContext);
				
				// Begin Filter lifecycle: init, doFilter, destroy
				resourceFilter.init(config);
				resourceFilter.doFilter(request, response, this);
				resourceFilter.destroy();
				// End Filter lifecycle
				
			} catch (ClassNotFoundException e) {
				logger.error(e);
			} catch (InstantiationException e) {
				logger.error(e);
			} catch (IllegalAccessException e) {
				logger.error(e);
			}
		}
	}

	private ClassLoader getCurrentThreadClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}
}
