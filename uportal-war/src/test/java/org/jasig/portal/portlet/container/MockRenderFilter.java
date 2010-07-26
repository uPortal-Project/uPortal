/**
 * 
 */
package org.jasig.portal.portlet.container;

import java.io.IOException;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.filter.FilterChain;
import javax.portlet.filter.FilterConfig;
import javax.portlet.filter.RenderFilter;

/**
 * @author Nicholas Blair, npblair@wisc.edu
 *
 */
public class MockRenderFilter implements RenderFilter {

	/* (non-Javadoc)
	 * @see javax.portlet.filter.RenderFilter#doFilter(javax.portlet.RenderRequest, javax.portlet.RenderResponse, javax.portlet.filter.FilterChain)
	 */
	@Override
	public void doFilter(RenderRequest request, RenderResponse response,
			FilterChain chain) throws IOException, PortletException {
		request.setAttribute("MockRenderFilter doFilter", "was here");
	}

	/* (non-Javadoc)
	 * @see javax.portlet.filter.PortletFilter#destroy()
	 */
	@Override
	public void destroy() {
	}

	/* (non-Javadoc)
	 * @see javax.portlet.filter.PortletFilter#init(javax.portlet.filter.FilterConfig)
	 */
	@Override
	public void init(FilterConfig filterConfig) throws PortletException {
	}

}
