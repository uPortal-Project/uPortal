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

package org.jasig.portal.layout.dlm.remoting;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.AuthorizationException;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.services.AuthorizationService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class FindEntityController  extends AbstractController {

	private static final Log log = LogFactory.getLog(FindEntityController.class);
	private IGroupListHelper groupListHelper;
	private IPersonManager personManager;
	
	public FindEntityController() {
		// for security reasons, we only want to allow POST access to this
		// service
		this.setSupportedMethods(new String[]{"POST"});
	}

	@Override
	public ModelAndView handleRequestInternal(HttpServletRequest request,
		HttpServletResponse response) throws Exception {
		
		/* Make sure the user is an admin. */
		IPerson person = personManager.getPerson(request);
		
		String entityType = request.getParameter("entityType");
		String entityId = request.getParameter("entityId");

		if(StringUtils.isBlank(entityType)) {
			return new ModelAndView("jsonView", "error", "No entityType specified.");
		}
		
		if (StringUtils.isBlank(entityId)) {
			return new ModelAndView("jsonView", "error", "No entityId specified.");
		}

		EntityIdentifier ei = person.getEntityIdentifier();
	    IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
	    if (!ap.hasPermission("org.jasig.portal.channels.groupsmanager.CGroupsManager", "VIEW", entityId)) {
			throw new AuthorizationException("User " + person.getUserName() + 
					" does not have view permissions on entity " + entityId);
		}


		JsonEntityBean result = groupListHelper.getEntity(entityType, entityId, true);

		return new ModelAndView("jsonView", "result", result);	
	}

	/**
	 * <p>For injection of the group list helper.</p>
	 * @param groupListHelper IGroupListHelper instance
	 */
	public void setGroupListHelper(IGroupListHelper groupListHelper) {
		this.groupListHelper = groupListHelper;
	}

	/**
	 * <p>For injection of the person manager.  Used for authorization.</p>
	 * @param personManager IPersonManager instance
	 */
	public void setPersonManager(IPersonManager personManager) {
		this.personManager = personManager;
	}
}
