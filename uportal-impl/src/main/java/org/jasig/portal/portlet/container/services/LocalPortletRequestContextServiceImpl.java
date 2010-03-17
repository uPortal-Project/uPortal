/**
 * 
 */
package org.jasig.portal.portlet.container.services;

import javax.portlet.PortletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.container.PortletActionResponseContext;
import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletContainerException;
import org.apache.pluto.container.PortletEventResponseContext;
import org.apache.pluto.container.PortletRenderResponseContext;
import org.apache.pluto.container.PortletRequestContext;
import org.apache.pluto.container.PortletRequestContextService;
import org.apache.pluto.container.PortletResourceRequestContext;
import org.apache.pluto.container.PortletResourceResponseContext;
import org.apache.pluto.container.PortletWindow;
import org.apache.pluto.container.driver.PortletContextService;
import org.apache.pluto.container.om.portlet.PortletDefinition;
import org.jasig.portal.url.IPortalRequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.ServletContextAware;

/**
 * @author Nicholas Blair, npblair@wisc.edu
 *
 */
@Service("portletRequestContextService")
public class LocalPortletRequestContextServiceImpl implements
PortletRequestContextService, ServletContextAware {

	protected final Log log = LogFactory.getLog(this.getClass());
	private ServletContext servletContext;
	private PortletContextService portletContextService;
	private IPortalRequestUtils portalRequestUtils;
	/*
	 * (non-Javadoc)
	 * @see org.springframework.web.context.ServletContextAware#setServletContext(javax.servlet.ServletContext)
	 */
	@Autowired(required=true)
	@Override
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	/**
	 * @param portletContextService the portletContextService to set
	 */
	@Autowired(required=true)
	public void setPortletContextService(PortletContextService portletContextService) {
		this.portletContextService = portletContextService;
	}

	/**
	 * @param portalRequestUtils the portalRequestUtils to set
	 */
	@Autowired(required=true)
	public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
		this.portalRequestUtils = portalRequestUtils;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContextService#getPortletActionRequestContext(org.apache.pluto.container.PortletContainer, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.pluto.container.PortletWindow)
	 */
	@Override
	public PortletRequestContext getPortletActionRequestContext(
			PortletContainer container, HttpServletRequest containerRequest,
			HttpServletResponse containerResponse, PortletWindow window) {
		PortletRequestContextImpl result = new PortletRequestContextImpl(container, containerRequest, containerResponse, window);
		return result;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContextService#getPortletActionResponseContext(org.apache.pluto.container.PortletContainer, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.pluto.container.PortletWindow)
	 */
	@Override
	public PortletActionResponseContext getPortletActionResponseContext(
			PortletContainer container, HttpServletRequest containerRequest,
			HttpServletResponse containerResponse, PortletWindow window) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContextService#getPortletEventRequestContext(org.apache.pluto.container.PortletContainer, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.pluto.container.PortletWindow)
	 */
	@Override
	public PortletRequestContext getPortletEventRequestContext(
			PortletContainer container, HttpServletRequest containerRequest,
			HttpServletResponse containerResponse, PortletWindow window) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContextService#getPortletEventResponseContext(org.apache.pluto.container.PortletContainer, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.pluto.container.PortletWindow)
	 */
	@Override
	public PortletEventResponseContext getPortletEventResponseContext(
			PortletContainer container, HttpServletRequest containerRequest,
			HttpServletResponse containerResponse, PortletWindow window) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContextService#getPortletRenderRequestContext(org.apache.pluto.container.PortletContainer, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.pluto.container.PortletWindow)
	 */
	@Override
	public PortletRequestContext getPortletRenderRequestContext(
			PortletContainer container, HttpServletRequest containerRequest,
			HttpServletResponse containerResponse, PortletWindow window) {

		PortletDefinition portletDefinition = window.getPortletDefinition();
		PortletConfig config;
		try {
			config = this.portletContextService.getPortletConfig(portletDefinition.getApplication().getName(), portletDefinition.getPortletName());
			HttpServletRequest servletRequest = this.portalRequestUtils.getCurrentPortalRequest();
			HttpServletResponse servletResponse = this.portalRequestUtils.getOriginalPortalResponse(containerRequest);

			PortletRequestContextImpl result = new PortletRequestContextImpl(container, containerRequest, containerResponse, window);
			result.init(config, servletContext, servletRequest, servletResponse);
			return result;
		} catch (PortletContainerException e) {
			log.error("exception from portletContextService#getPortletConfig for portletDefinition " + portletDefinition, e);
			// TODO return null or throw a RuntimeException?
			return null;
		}


	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContextService#getPortletRenderResponseContext(org.apache.pluto.container.PortletContainer, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.pluto.container.PortletWindow)
	 */
	@Override
	public PortletRenderResponseContext getPortletRenderResponseContext(
			PortletContainer container, HttpServletRequest containerRequest,
			HttpServletResponse containerResponse, PortletWindow window) {
		HttpServletRequest servletRequest = this.portalRequestUtils.getCurrentPortalRequest();
		HttpServletResponse servletResponse = this.portalRequestUtils.getOriginalPortalResponse(containerRequest);
		PortletRenderResponseContextImpl result = new PortletRenderResponseContextImpl(container, containerRequest, containerResponse, window);
		result.init(servletRequest, servletResponse);

		return result;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContextService#getPortletResourceRequestContext(org.apache.pluto.container.PortletContainer, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.pluto.container.PortletWindow)
	 */
	@Override
	public PortletResourceRequestContext getPortletResourceRequestContext(
			PortletContainer container, HttpServletRequest containerRequest,
			HttpServletResponse containerResponse, PortletWindow window) {
		PortletDefinition portletDefinition = window.getPortletDefinition();
		PortletConfig config;
		try {
			config = this.portletContextService.getPortletConfig(portletDefinition.getApplication().getName(), portletDefinition.getPortletName());
			HttpServletRequest servletRequest = this.portalRequestUtils.getCurrentPortalRequest();
			HttpServletResponse servletResponse = this.portalRequestUtils.getOriginalPortalResponse(containerRequest);

			PortletResourceRequestContextImpl result = new PortletResourceRequestContextImpl(container, containerRequest, containerResponse, window);
			result.init(config, servletContext, servletRequest, servletResponse);
			// TODO set cacheability and resourceId on PortletResourceRequestContextImpl result
			return result;
		} catch (PortletContainerException e) {
			log.error("exception from portletContextService#getPortletConfig for portletDefinition " + portletDefinition, e);
			// TODO return null or throw a RuntimeException?
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContextService#getPortletResourceResponseContext(org.apache.pluto.container.PortletContainer, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.pluto.container.PortletWindow)
	 */
	@Override
	public PortletResourceResponseContext getPortletResourceResponseContext(
			PortletContainer container, HttpServletRequest containerRequest,
			HttpServletResponse containerResponse, PortletWindow window) {
		
		PortletResourceResponseContextImpl result = new PortletResourceResponseContextImpl(container, containerRequest, containerResponse, window);
		HttpServletRequest servletRequest = this.portalRequestUtils.getCurrentPortalRequest();
		HttpServletResponse servletResponse = this.portalRequestUtils.getOriginalPortalResponse(containerRequest);

		result.init(servletRequest, servletResponse);
		return result;
	}



}
