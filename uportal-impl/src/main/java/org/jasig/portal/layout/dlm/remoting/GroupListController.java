/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.layout.dlm.remoting;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.AuthorizationException;
import org.jasig.portal.security.AdminEvaluator;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.springframework.web.servlet.mvc.AbstractController;
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
public class GroupListController extends AbstractController {

	private static final Log log = LogFactory.getLog(GroupListController.class);
	private IGroupListHelper groupListHelper;
	private IPersonManager personManager;
	
	public GroupListController() {
		// for security reasons, we only want to allow POST access to this
		// service
		this.setSupportedMethods(new String[]{"POST"});
	}

	@Override
	public ModelAndView handleRequestInternal(HttpServletRequest request,
		HttpServletResponse response) throws Exception {
		
		/* Make sure the user is an admin. */
		IPerson user = personManager.getPerson(request);
		if(!AdminEvaluator.isAdmin(user)) {
			throw new AuthorizationException("User " + user.getUserName() + " not an administrator.");
		}
		
		String[] entityTypes = request.getParameterValues("entityType");
		String entityId = request.getParameter("entityId");
		String searchTerm = request.getParameter("searchTerm");

		if(entityTypes == null || entityTypes.length == 0) {
			return new ModelAndView("jsonView", "error", "No entityType specified.");
		}
		
		Set<JsonEntityBean> results = new HashSet<JsonEntityBean>();
		
		for(String entityType : entityTypes) {
			results.addAll(groupListHelper.search(entityType, entityId, searchTerm));
		}

		return new ModelAndView("jsonView", "results", results);	
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
