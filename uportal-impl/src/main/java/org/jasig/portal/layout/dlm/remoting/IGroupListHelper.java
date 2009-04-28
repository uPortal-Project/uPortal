package org.jasig.portal.layout.dlm.remoting;

import java.util.Set;

/**
 * <p>Search logic for the Spring GroupListController.</p>
 * 
 * @author Drew Mazurek
 */
public interface IGroupListHelper {

	/**
	 * <p>Searches the GroupService for the given entity type, ID, and
	 * search term.</p>
	 * @param entityType either "category", "group", or "person"
	 * @param entityId key of entity to search for
	 * @param searchTerm term to search for.  Search type is substring match.
	 * @return Set of entities that match search criteria
	 */
	public Set<JsonEntityBean> search(String entityType, String entityId,
			String searchTerm);
}
