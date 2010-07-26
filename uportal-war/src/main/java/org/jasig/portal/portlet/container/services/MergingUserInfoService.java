/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.portlet.container.services;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.portlet.PortletRequest;

import org.apache.pluto.container.PortletContainerException;
import org.apache.pluto.container.PortletWindow;
import org.apache.pluto.container.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Merges together the results of multiple instances of UserInfoService.
 * 
 * @author Jen Bourey
 * @version $Revision$
 */
@Service
@Qualifier("main")
public class MergingUserInfoService implements UserInfoService {

	private Set<UserInfoService> userInfoServices = Collections.emptySet();

	/**
	 * @param userInfoServices the list of UserInfoServices to be merged
	 */
	@Autowired
	public void setUserInfoServices(Set<UserInfoService> userInfoServices) {
		this.userInfoServices = userInfoServices;
	}
	
	/**
	 * @return list of UserInfoServices
	 */
	public Set<UserInfoService> getUserInfoServices() {
		return this.userInfoServices;
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
    		if (userInfo != null) {
	    		for (final Map.Entry<String, String> entry : userInfo.entrySet()) {
	    			final String attributeName = entry.getKey();
	    			final String valueObj = entry.getValue();
	    			mergedInfo.put(attributeName, valueObj);
	    		}
    		}
    		
    	}
    	
		return mergedInfo;
	}

}
