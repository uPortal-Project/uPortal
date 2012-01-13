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

import org.jasig.portal.layout.dlm.remoting.IGroupListHelper;
import org.jasig.portal.layout.dlm.remoting.JsonEntityBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * EntitiesRESTController
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
@Controller
public class EntitiesRESTController  {

	private IGroupListHelper groupListHelper;

	/**
     * <p>For injection of the group list helper.</p>
     * @param groupListHelper IGroupListHelper instance
     */
    @Autowired
    public void setGroupListHelper(IGroupListHelper groupListHelper) {
        this.groupListHelper = groupListHelper;
    }


	/**
	 * 
	 * @param request
	 * @param response
	 * @param entityType
	 * @param entityId
	 * @return
	 */
    @PostAuthorize("hasPermission(returnObject, 'VIEW')")
    @RequestMapping(value="/entities/{entityType}/{entityId}.json", method = RequestMethod.GET)
	public JsonEntityBean findEntity(HttpServletRequest request,
			HttpServletResponse response,
			@PathVariable("entityType") String entityType,
			@PathVariable("entityId") String entityId) {

		final JsonEntityBean result = groupListHelper.getEntity(entityType, entityId, true);
		return result;

	}
    
    /**
     * 
     * @param request
     * @param response
     * @param query
     * @param entityTypes
     * @return
     */
    @PostFilter("hasPermission(filterObject, 'VIEW')")
    @RequestMapping(value="/entities.json", method = RequestMethod.GET)
	public Set<JsonEntityBean> doSearch(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam("q") String query,
			@RequestParam("entityType[]") List<String> entityTypes) {

	    // initialize a set of entity beans
		Set<JsonEntityBean> results = new HashSet<JsonEntityBean>();
		
		// search each entity type for matching entities
		for(String entityType : entityTypes) {
			Set<JsonEntityBean> entities = groupListHelper.search(entityType, query);
			results.addAll(entities);
		}
		
		return results;	
	}

}
