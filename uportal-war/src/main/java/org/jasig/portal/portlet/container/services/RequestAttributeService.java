package org.jasig.portal.portlet.container.services;

import javax.servlet.http.HttpServletRequest;

import org.apache.pluto.container.PortletRequestContext;
import org.apache.pluto.container.PortletWindow;

/**
 * This interface hooks into the {@link PortletRequestContext} implementation for uPortal
 * and provides a mechanism for finding additional attributes.
 * 
 * @author Nicholas Blair
 * @version $Id$
 */
public interface RequestAttributeService {

	/**
	 * 
	 * @param httpServletRequest
	 * @param plutoPortletWindow
	 * @param name
	 * @return the corresponding request attribute, or null if none found
	 */
	public Object getAttribute(HttpServletRequest httpServletRequest,
			PortletWindow plutoPortletWindow, String name);

}