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
package org.jasig.portal.portlets.fragmentadmin;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.portlet.PortletRequest;

import org.jasig.portal.layout.dlm.ConfigurationLoader;
import org.jasig.portal.layout.dlm.FragmentDefinition;
import org.jasig.portal.security.IdentitySwapperManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.webflow.execution.RequestContext;

/**
 * Helper class for the FragmentAdministration web flow.
 * 
 * @author Nicholas Blair, npblair@wisc.edu
 *
 */
@Service("fragmentAdministrationHelper")
public class FragmentAdministrationHelper {

	private ConfigurationLoader configurationLoader;
	private IdentitySwapperManager identitySwapperManager;
	
	@Autowired
	public void setConfigurationLoader(
			ConfigurationLoader configurationLoader) {
		this.configurationLoader = configurationLoader;
	}
	
	@Autowired
    public void setIdentitySwapperManager(IdentitySwapperManager identitySwapperManager) {
        this.identitySwapperManager = identitySwapperManager;
    }

    /**
	 * 
	 * @param remoteUser
	 * @return
	 */
	public Map<String, String> getAuthorizedDlmFragments(String remoteUser) {
		List<FragmentDefinition> fragments = this.configurationLoader.getFragments();
		Map<String, String> results = new TreeMap<String, String>();
		for(FragmentDefinition frag: fragments) {
			if(this.identitySwapperManager.canImpersonateUser(remoteUser, frag.getOwnerId())) {
				results.put(frag.getOwnerId(), frag.getName());
			}
		}
		return results;
	}
	
	/**
	 * 
	 * @param remoteUser
	 * @param targetFragmentOwner
	 * @return "yes" for success, "no" otherwise
	 */
	public String swapToFragmentOwner(final String remoteUser, final String targetFragmentOwner, RequestContext requestContext) {
		PortletRequest portletRequest = (PortletRequest) requestContext.getExternalContext().getNativeRequest();
		this.identitySwapperManager.impersonateUser(portletRequest, remoteUser, targetFragmentOwner);
		return "yes";
	}
}
