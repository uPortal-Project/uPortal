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
import javax.portlet.PortletSession;

import org.jasig.portal.layout.dlm.ConfigurationLoader;
import org.jasig.portal.layout.dlm.FragmentDefinition;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IAuthorizationService;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.mvc.LoginController;
import org.jasig.portal.security.provider.AuthorizationImpl;
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

	private static final String UP_USERS = "UP_USERS";
	private static final String IMPERSONATE = "IMPERSONATE";
	private ConfigurationLoader configurationLoader;
	private IAuthorizationService authorizationService;
	
	/**
	 * @param legacyConfigurationLoader the legacyConfigurationLoader to set
	 */
	@Autowired
	public void setConfigurationLoader(
			ConfigurationLoader configurationLoader) {
		this.configurationLoader = configurationLoader;
	}

	@Autowired
	public void setAuthorizationService(IAuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }


    /**
	 * 
	 * @param remoteUser
	 * @return
	 */
	public Map<String, String> getAuthorizedDlmFragments(String remoteUser) {
		List<FragmentDefinition> fragments = this.configurationLoader.getFragments();
		IAuthorizationPrincipal principal = authorizationService.newPrincipal(remoteUser, IPerson.class);
		Map<String, String> results = new TreeMap<String, String>();
		for(FragmentDefinition frag: fragments) {
			if(principal.hasPermission(UP_USERS, IMPERSONATE, frag.getOwnerId())) {
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
		IAuthorizationPrincipal principal = authorizationService.newPrincipal(remoteUser, IPerson.class);
		if(principal.hasPermission(UP_USERS, IMPERSONATE, targetFragmentOwner)) {
			PortletRequest portletRequest = (PortletRequest) requestContext.getExternalContext().getNativeRequest();
			PortletSession session = portletRequest.getPortletSession();
			session.setAttribute(LoginController.SWAP_TARGET_UID, targetFragmentOwner, javax.portlet.PortletSession.APPLICATION_SCOPE);
			return "yes";
		}
		return "no";
	}
}
