/**
 * 
 */
package org.jasig.portal.portlet.container.services;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.container.PortletActionResponseContext;
import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletEventResponseContext;
import org.apache.pluto.container.PortletRenderResponseContext;
import org.apache.pluto.container.PortletRequestContext;
import org.apache.pluto.container.PortletRequestContextService;
import org.apache.pluto.container.PortletResourceRequestContext;
import org.apache.pluto.container.PortletResourceResponseContext;
import org.apache.pluto.container.PortletWindow;
import org.apache.pluto.driver.services.container.PortletActionResponseContextImpl;
import org.apache.pluto.driver.services.container.PortletEventResponseContextImpl;
import org.apache.pluto.driver.services.container.PortletRenderResponseContextImpl;
import org.apache.pluto.driver.services.container.PortletRequestContextImpl;
import org.apache.pluto.driver.services.container.PortletResourceRequestContextImpl;
import org.apache.pluto.driver.services.container.PortletResourceResponseContextImpl;
import org.springframework.stereotype.Service;

/**
 * @author Nicholas Blair, npblair@wisc.edu
 *
 */
@Service("portletRequestContextService")
public class LocalPortletRequestContextServiceImpl implements
PortletRequestContextService {
	

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContextService#getPortletActionRequestContext(org.apache.pluto.container.PortletContainer, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.pluto.container.PortletWindow)
	 */
	@Override
	public PortletRequestContext getPortletActionRequestContext(
			PortletContainer container, HttpServletRequest containerRequest,
			HttpServletResponse containerResponse, PortletWindow window) {
		PortletRequestContextImpl result = new PortletRequestContextImpl(container, containerRequest, containerResponse, window, true);
		return result;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContextService#getPortletActionResponseContext(org.apache.pluto.container.PortletContainer, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.pluto.container.PortletWindow)
	 */
	@Override
	public PortletActionResponseContext getPortletActionResponseContext(
			PortletContainer container, HttpServletRequest containerRequest,
			HttpServletResponse containerResponse, PortletWindow window) {
		PortletActionResponseContextImpl result = new PortletActionResponseContextImpl(container, containerRequest, containerResponse, window);
		return result;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContextService#getPortletEventRequestContext(org.apache.pluto.container.PortletContainer, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.pluto.container.PortletWindow)
	 */
	@Override
	public PortletRequestContext getPortletEventRequestContext(
			PortletContainer container, HttpServletRequest containerRequest,
			HttpServletResponse containerResponse, PortletWindow window) {
		PortletRequestContextImpl result = new PortletRequestContextImpl(container, containerRequest, containerResponse, window, false);
		return result;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContextService#getPortletEventResponseContext(org.apache.pluto.container.PortletContainer, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.pluto.container.PortletWindow)
	 */
	@Override
	public PortletEventResponseContext getPortletEventResponseContext(
			PortletContainer container, HttpServletRequest containerRequest,
			HttpServletResponse containerResponse, PortletWindow window) {
		
		PortletEventResponseContextImpl result = new PortletEventResponseContextImpl(container, containerRequest, containerResponse, window);
		return result;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContextService#getPortletRenderRequestContext(org.apache.pluto.container.PortletContainer, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.pluto.container.PortletWindow)
	 */
	@Override
	public PortletRequestContext getPortletRenderRequestContext(
			PortletContainer container, HttpServletRequest containerRequest,
			HttpServletResponse containerResponse, PortletWindow window) {
		PortletRequestContextImpl result = new PortletRequestContextImpl(container, containerRequest, containerResponse, window, false);
		return result;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContextService#getPortletRenderResponseContext(org.apache.pluto.container.PortletContainer, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.pluto.container.PortletWindow)
	 */
	@Override
	public PortletRenderResponseContext getPortletRenderResponseContext(
			PortletContainer container, HttpServletRequest containerRequest,
			HttpServletResponse containerResponse, PortletWindow window) {
		PortletRenderResponseContextImpl result = new PortletRenderResponseContextImpl(container, containerRequest, containerResponse, window);
		return result;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContextService#getPortletResourceRequestContext(org.apache.pluto.container.PortletContainer, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.pluto.container.PortletWindow)
	 */
	@Override
	public PortletResourceRequestContext getPortletResourceRequestContext(
			PortletContainer container, HttpServletRequest containerRequest,
			HttpServletResponse containerResponse, PortletWindow window) {
		PortletResourceRequestContextImpl result = new PortletResourceRequestContextImpl(container, containerRequest, containerResponse, window);
		return result;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContextService#getPortletResourceResponseContext(org.apache.pluto.container.PortletContainer, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.pluto.container.PortletWindow)
	 */
	@Override
	public PortletResourceResponseContext getPortletResourceResponseContext(
			PortletContainer container, HttpServletRequest containerRequest,
			HttpServletResponse containerResponse, PortletWindow window) {

		PortletResourceResponseContextImpl result = new PortletResourceResponseContextImpl(container, containerRequest, containerResponse, window);
		return result;
	}



}
