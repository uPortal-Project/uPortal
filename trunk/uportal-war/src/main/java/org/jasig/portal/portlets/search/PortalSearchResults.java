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
