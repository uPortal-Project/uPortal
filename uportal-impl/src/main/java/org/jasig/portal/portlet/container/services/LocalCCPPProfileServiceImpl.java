/**
 * 
 */
package org.jasig.portal.portlet.container.services;

import javax.ccpp.Profile;
import javax.ccpp.ProfileFactory;
import javax.servlet.http.HttpServletRequest;

import org.apache.pluto.container.CCPPProfileService;
import org.springframework.stereotype.Service;

/**
 * Simple {@link CCPPProfileService}. Calls out to
 * {@link ProfileFactory#getInstance()}, which returns
 * null if no implementation is available.
 * 
 * @author Nicholas Blair, npblair@wisc.edu
 *
 */
@Service("cCPPProfileService")
public class LocalCCPPProfileServiceImpl implements CCPPProfileService {

	/** 
	 * Returns null if no CCPP implementation is available. If a CCPP implementation
	 * is available, may return null if no {@link Profile} can be identified from the
	 * request.
	 * 
	 * @see org.apache.pluto.container.CCPPProfileService#getCCPPProfile(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public Profile getCCPPProfile(HttpServletRequest httpServletRequest) {
		ProfileFactory profileFactory = ProfileFactory.getInstance();
		if(null == profileFactory) {
			// no CCPP implementation available, just return null
			return null;
		} else {
			Profile result = profileFactory.newProfile(httpServletRequest);
			return result;
		}
	}

}
