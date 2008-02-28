/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.container.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletRequest;

import org.apache.pluto.PortletContainerException;
import org.apache.pluto.PortletWindow;
import org.apache.pluto.spi.optional.UserInfoService;
import org.springframework.beans.factory.annotation.Required;

/**
 * Merges together the results of multiple instances of UserInfoService.
 * 
 * @author Jen Bourey
 * @version $Revision$
 */
public class MergingUserInfoService implements UserInfoService {

	private List<UserInfoService> userInfoServices;

	/**
	 * @param userInfoServices the list of UserInfoServices to be merged
	 */
	@Required
	public void setUserInfoServices(List<UserInfoService> userInfoServices) {
		this.userInfoServices = userInfoServices;
	}
	
	/**
	 * @return list of UserInfoServices
	 */
	public List<UserInfoService> getUserInfoServices() {
		return this.userInfoServices;
	}
	
    /* (non-Javadoc)
     * @see org.apache.pluto.spi.optional.UserInfoService#getUserInfo(javax.portlet.PortletRequest)
     */
    @Deprecated
    public Map<String, String> getUserInfo(PortletRequest request)
			throws PortletContainerException {
    	
    	Map<String, String> mergedInfo = new HashMap<String, String>();
    	
    	// iterate over all supplied user info services and add their
    	// resulting key/value pairs to our merged map
    	for (final UserInfoService service : this.userInfoServices){
    		
    		Map<String, String> userInfo = service.getUserInfo(request);
    		for (final Map.Entry<String, String> entry : userInfo.entrySet()) {
    			final String attributeName = entry.getKey();
    			final String valueObj = entry.getValue();
    			mergedInfo.put(attributeName, valueObj);
    		}
    		
    	}
    	
		return mergedInfo;
	}
    
    /* (non-Javadoc)
     * @see org.apache.pluto.spi.optional.UserInfoService#getUserInfo(javax.portlet.PortletRequest, org.apache.pluto.PortletWindow)
     */
	public Map<String, String> getUserInfo(PortletRequest request, PortletWindow portletWindow)
			throws PortletContainerException {

		Map<String, String> mergedInfo = new HashMap<String, String>();

		// iterate over all supplied user info services and add their
    	// resulting key/value pairs to our merged map
		for (final UserInfoService service : this.userInfoServices){
			
    		Map<String, String> userInfo = service.getUserInfo(request, portletWindow);
    		for (final Map.Entry<String, String> entry : userInfo.entrySet()) {
    			final String attributeName = entry.getKey();
    			final String valueObj = entry.getValue();
    			mergedInfo.put(attributeName, valueObj);
    		}
    		
    	}
    	
		return mergedInfo;
	}

}
