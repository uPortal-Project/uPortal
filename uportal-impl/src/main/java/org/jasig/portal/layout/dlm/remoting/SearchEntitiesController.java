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

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.AuthorizationException;
import org.jasig.portal.security.AdminEvaluator;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * <p>A Spring controller that returns a JSON view of the desired
 * user, group, or category.  Request parameters:</p>
 * <ul>
 *   <li>entityType (required): a string representing the desired entity to
 *   look for (category, group, or person).  The user may supply multiple
 *   entityTypes and all will be queried.</li>
 *   <li>entityId (optional): the key or id of the single entity to be
 *   retrieved</li>
 *   <li>searchTerm (optional): a string representing a search term to use to
 *   retrieve a list of entities.  Note that if the entityType is "category",
 *   search is not supported.</li>
 * </ul>
 * 
 * <p>If neither an entityId nor a searchTerm is provided, the search will
 * return the root category or group ("All Categories" or "Everyone",
 * respectively).</p> 
 *
 * @author Drew Mazurek
 */
@Controller
@RequestMapping("/searchEntities")
public class SearchEntitiesController {

	//private static final Log log = LogFactory.getLog(SearchEntitiesController.class);
	private IGroupListHelper groupListHelper;
	private IPersonManager personManager;
	
	/**
	 * 
	 * @param request
	 * @param searchTerm
	 * @param entityTypes
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST)
	public ModelAndView doSearch(HttpServletRequest request, 
			@RequestParam("searchTerm") String searchTerm, @RequestParam("entityType") String [] entityTypes) {
		/* Make sure the user is an admin. */
		IPerson person = personManager.getPerson(request);
		if(!AdminEvaluator.isAdmin(person)) {
			throw new AuthorizationException("User " + person.getUserName() + " not an administrator.");
		}
		if(entityTypes == null || entityTypes.length == 0) {
			return new ModelAndView("jsonView", "error", "No entityType specified.");
		}
		Set<JsonEntityBean> results = new HashSet<JsonEntityBean>();
		
		for(String entityType : entityTypes) {
			results.addAll(groupListHelper.search(entityType, searchTerm));
		}
		return new ModelAndView("jsonView", "results", results);	
	}

	/**
	 * <p>For injection of the group list helper.</p>
	 * @param groupListHelper IGroupListHelper instance
	 */
	@Autowired(required=true)
	public void setGroupListHelper(IGroupListHelper groupListHelper) {
		this.groupListHelper = groupListHelper;
	}

	/**
	 * <p>For injection of the person manager.  Used for authorization.</p>
	 * @param personManager IPersonManager instance
	 */
	@Autowired(required=true)
	public void setPersonManager(IPersonManager personManager) {
		this.personManager = personManager;
	}
}
