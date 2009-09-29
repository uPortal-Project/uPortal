/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.portlets.groupselector;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface GroupsSelectorHelper {
	
	public Map<String, String> getGroupNames(List<String> groupKeys);
	
	public String getGroupName(String key);
	
	@SuppressWarnings("unchecked")
	public Set<Class> getPersonTypeSet();
}
