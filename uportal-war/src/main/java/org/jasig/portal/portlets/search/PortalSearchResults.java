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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.jasig.portal.search.SearchResult;
import org.jasig.portal.utils.Tuple;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;

/**
 * Used to collate search results for the SearchPortletController
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortalSearchResults implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Set<String> defaultTab;
    //Map of <result type, Set<result tabs>>
    private final Map<String, Set<String>> resultTypeMappings;
    
    //Map of <tab-key, List<result, url>>
    private final LoadingCache<String, List<Tuple<SearchResult, String>>> results;
    
    public PortalSearchResults(String defaultTab, Map<String, Set<String>> resultTypeMappings) {
        this.defaultTab = ImmutableSet.of(defaultTab);
        this.resultTypeMappings = resultTypeMappings;
        
        this.results = CacheBuilder.newBuilder().<String, List<Tuple<SearchResult, String>>>build(new CacheLoader<String, List<Tuple<SearchResult, String>>>() {
            @Override
            public List<Tuple<SearchResult, String>> load(String key) throws Exception {
                return Collections.synchronizedList(new LinkedList<Tuple<SearchResult,String>>());
            }
        });
    }
    
    /**
     * @return Map of tabKey -> Tuple<result, url>
     */
    public ConcurrentMap<String, List<Tuple<SearchResult, String>>> getResults() {
        return this.results.asMap();
    }
    
    public void addPortletSearchResults(String url, SearchResult result) {
        final Set<String> tabs = this.getTabs(result);
        for (final String tab : tabs) {
            final List<Tuple<SearchResult, String>> typeResults = this.results.getUnchecked(tab);
            typeResults.add(new Tuple<SearchResult, String>(result, url));
        }
    }
    
    protected Set<String> getTabs(SearchResult result) {
        final List<String> types = result.getType();
        
        //Result set no type, use the default tab
        if (types == null || types.isEmpty()) {
            return this.defaultTab;
        }
        
        final Set<String> tabs = new HashSet<String>();
        
        //For each type the search result declares lookup the tab(s) mapped to that type
        for (final String type : types) {
            final Set<String> mappedTabs = this.resultTypeMappings.get(type);
            if (mappedTabs != null) {
                tabs.addAll(mappedTabs);
            }
            else {
                tabs.addAll(this.defaultTab);
            }
        }
        
        return tabs;
    }
}
