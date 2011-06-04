package org.jasig.portal.portlets.search;

import javax.portlet.PortletRequest;

import org.jasig.portal.search.SearchRequest;
import org.jasig.portal.search.SearchResults;

public interface IPortalSearchService {
    
    public SearchResults getSearchResults(PortletRequest request, SearchRequest query);

}
