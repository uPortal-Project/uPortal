/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.portlets.groupselector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IEntityNameFinder;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.EntityNameFinderService;

public class GroupsSelectorHelperImpl implements GroupsSelectorHelper {

	private Log log = LogFactory.getLog(GroupsSelectorHelperImpl.class);
	
	public static final String MODE_ENTITY = "entity";
	public static final String MODE_GROUP = "group";
	public static final String MODE_BOTH = "both";

	@SuppressWarnings("unchecked")
	public Set<Class> getPersonTypeSet() {
		Set<Class> set = new HashSet<Class>();
		set.add(IPerson.class);
		return set;
	}

	public Map<String, String> getGroupNames(List<String> groupKeys) {
		Map<String, String> names = new HashMap<String, String>();
		for (String key : groupKeys) {
			names.put(key, getGroupName(key));
		}
		return names;
	}
	
	public String getGroupName(String key) {
		String name = null;
		try {
			// first assume this key is for an IEntityGroup (group or channel)
			IEntityNameFinder finder = EntityNameFinderService.instance()
					.getNameFinder(IEntityGroup.class);
			try {
				name = finder.getName(key);
			} catch (NullPointerException e) {
				// this might be a person rather than a group
				finder = EntityNameFinderService.instance()
					.getNameFinder(IPerson.class);
				name = finder.getName(key);
			}
		} catch (Exception e) {
			log.warn("Failed to retrieve group name for key " + key, e);
		}
		return name;
	}
}
