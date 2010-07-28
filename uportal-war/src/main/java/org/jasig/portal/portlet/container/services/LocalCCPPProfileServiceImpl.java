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
