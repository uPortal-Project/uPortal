/**
 * 
 */
package org.jasig.portal.portlet.container.services;

import org.apache.pluto.container.FilterManager;
import org.apache.pluto.container.FilterManagerService;
import org.apache.pluto.container.PortletWindow;
import org.jasig.portal.portlet.container.FilterManagerImpl;
import org.springframework.stereotype.Service;

/**
 * @author Nicholas Blair, npblair@wisc.edu
 *
 */
@Service("filterManagerService")
public class LocalFilterManagerServiceImpl implements FilterManagerService {

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.FilterManagerService#getFilterManager(org.apache.pluto.container.PortletWindow, java.lang.String)
	 */
	@Override
	public FilterManager getFilterManager(PortletWindow window, String lifeCycle) {
		FilterManagerImpl filterManager = new FilterManagerImpl(window, lifeCycle);
		return filterManager;
	}

}
