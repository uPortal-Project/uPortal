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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jasig.portal.search.SearchResult;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Used to collate search results for the SearchPortletController
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortalSearchResults implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final List<String> DEFAULT_TYPES = Arrays.asList("Default");
    
    //Map of <result type, <url, result>>
    private final LoadingCache<String, ConcurrentMap<String, SearchResult>> results;
    
    public PortalSearchResults() {
        this.results = CacheBuilder.newBuilder().<String, ConcurrentMap<String, SearchResult>>build(new CacheLoader<String, ConcurrentMap<String, SearchResult>>() {
            @Override
            public ConcurrentMap<String, SearchResult> load(String key) throws Exception {
                return new ConcurrentHashMap<String, SearchResult>();
            }
        });
    }
    
    public ConcurrentMap<String, ConcurrentMap<String, SearchResult>> getResults() {
        return this.results.asMap();
    }
    
    public void addPortletSearchResults(String url, SearchResult result) {
        final List<String> types = this.getTypes(result);
        for (final String type : types) {
            final Map<String, SearchResult> typeResults = this.results.getUnchecked(type);
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
