package org.jasig.portal.portlets.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jasig.portal.search.SearchResult;
import org.jasig.portal.search.SearchResults;

public class PortalSearchResults {

    private Map<String, List<SearchResult>> results;
    
    public PortalSearchResults() {
        this.results = new HashMap<String, List<SearchResult>>();
    }
    
    public Map<String, List<SearchResult>> getResults() {
        return this.results;
    }
    
    public void addPortletSearchResults(SearchResults portletSearchResults) {
        for (SearchResult result : portletSearchResults.getSearchResult()) {
            final List<String> types = result.getType();
            if (types == null || types.isEmpty()) {
                addPortletSearchResult("Default", result);
            }
            else {
                for (String type : types) {
                    addPortletSearchResult(type, result);
                }
            }
        }
    }
    
    protected void addPortletSearchResult(String type, SearchResult result) {
        if (!results.containsKey(type)) {
            results.put(type, new ArrayList<SearchResult>());
        }
        results.get(type).add(result);
    }
    
}
