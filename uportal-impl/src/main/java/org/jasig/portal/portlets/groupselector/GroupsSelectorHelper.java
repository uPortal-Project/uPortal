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
