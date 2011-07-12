package org.jasig.portal.portlets.search;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jasig.portal.search.SearchResult;

public class PortalSearchResults {
    private static final List<String> DEFAULT_TYPES = Arrays.asList("Default");
    private final Map<String, Map<String, SearchResult>> results;
    
    public PortalSearchResults() {
        this.results = new LinkedHashMap<String, Map<String, SearchResult>>();
    }
    
    public Map<String, Map<String, SearchResult>> getResults() {
        return this.results;
    }
    
    public synchronized void addPortletSearchResults(String url, SearchResult result) {
        final List<String> types = this.getTypes(result);
        for (final String type : types) {
            
            final Map<String, SearchResult> typeResults;
            if (!results.containsKey(type)) {
                typeResults = new LinkedHashMap<String, SearchResult>();
                results.put(type, typeResults);
            }
            else {
                typeResults = results.get(type);
            }
        
            typeResults.put(url, result);
        }
    }
    
    protected List<String> getTypes(SearchResult result) {
        final List<String> type = result.getType();
        if (type != null && !type.isEmpty()) {
            return type;
        }
        
        return DEFAULT_TYPES;
    }
}
