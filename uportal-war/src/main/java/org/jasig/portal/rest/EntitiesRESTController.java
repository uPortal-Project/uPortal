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

package org.jasig.portal.rest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.layout.dlm.remoting.IGroupListHelper;
import org.jasig.portal.layout.dlm.remoting.JsonEntityBean;
import org.jasig.portal.portlets.groupadmin.GroupAdministrationHelper;
import org.jasig.portal.portlets.groupselector.EntityEnum;
import org.jasig.portal.portlets.lookup.IPersonLookupHelper;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.services.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * EntitiesRESTController
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
@Controller
public class EntitiesRESTController  {

	//private static final Log log = LogFactory.getLog(FindEntityController.class);
	private IGroupListHelper groupListHelper;
	private IPersonManager personManager;

	/**
	 * 
	 * @param request
	 * @param response
	 * @param entityType
	 * @param entityId
	 * @return
	 */
    @RequestMapping(value="/entities/{entityType}/{entityId}.json", method = RequestMethod.GET)
	public ModelAndView findEntity(HttpServletRequest request,
			HttpServletResponse response,
			@PathVariable("entityType") String entityType,
			@PathVariable("entityId") String entityId) {

		final IPerson person = personManager.getPerson(request);
		final EntityIdentifier ei = person.getEntityIdentifier();
	    final IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
	    
		JsonEntityBean result = groupListHelper.getEntity(entityType, entityId, true);

		if (result == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
		}

		// get the type for this entity
		EntityEnum type = result.getEntityType();

		// if the located entity is a group, check to make sure the requesting
		// user has the view group permission
        if (type.isGroup()
                && !ap.hasPermission(GroupAdministrationHelper.GROUPS_OWNER,
                        GroupAdministrationHelper.VIEW_PERMISSION,
                        result.getPrincipalString())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        } 
        
		// if the located entity is a person, check to make sure the requesting
		// user has the view person permission
        else if (type.equals(EntityEnum.PERSON)
                && !ap.hasPermission(IPersonLookupHelper.USERS_OWNER,
                        IPersonLookupHelper.VIEW_USER_PERMISSION,
                        result.getPrincipalString())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
            
		}

        // TODO: check permissions for portlets

		// if a valid entity was returned and all permission checks passed,
		// return the entity as a JSON object
    	else {
			ModelAndView mv = new ModelAndView();
			mv.addObject("entity", result);
			mv.setViewName("json");
			return mv;
		} 
		
	}

    /**
     * 
     * @param request
     * @param response
     * @param query
     * @param entityTypes
     * @return
     */
    @RequestMapping(value="/entities.json", method = RequestMethod.GET)
	public ModelAndView doSearch(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam("q") String query,
			@RequestParam("entityType[]") List<String> entityTypes) {

    	// get the authorization principal for this user
		final IPerson person = personManager.getPerson(request);
		final EntityIdentifier ei = person.getEntityIdentifier();
	    final IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());

	    // initialize a set of entity beans
		Set<JsonEntityBean> results = new HashSet<JsonEntityBean>();
		
		// search each entity type for matching entities
		for(String entityType : entityTypes) {
			Set<JsonEntityBean> entities = groupListHelper.search(entityType, query);
			// filter the matching entities according to the current user's permissions
			for (JsonEntityBean entity : entities) {
				if (ap.hasPermission(GroupAdministrationHelper.GROUPS_OWNER, GroupAdministrationHelper.VIEW_PERMISSION, entity.getPrincipalString())) {
					results.add(entity);
				}
			}
		}
		
		return new ModelAndView("jsonView", "entities", results);	
	}

	/**
	 * <p>For injection of the group list helper.</p>
	 * @param groupListHelper IGroupListHelper instance
	 */
	@Autowired
	public void setGroupListHelper(IGroupListHelper groupListHelper) {
		this.groupListHelper = groupListHelper;
	}

	/**
	 * <p>For injection of the person manager.  Used for authorization.</p>
	 * @param personManager IPersonManager instance
	 */
	@Autowired
	public void setPersonManager(IPersonManager personManager) {
		this.personManager = personManager;
	}
}
