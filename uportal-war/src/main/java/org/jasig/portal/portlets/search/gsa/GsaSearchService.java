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

package org.jasig.portal.portlets.search.gsa;

import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletRequest;

import org.jasig.portal.portlets.search.IPortalSearchService;
import org.jasig.portal.search.SearchRequest;
import org.jasig.portal.search.SearchResult;
import org.jasig.portal.search.SearchResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

public class GsaSearchService implements IPortalSearchService {

    private RestTemplate restTemplate;
    
    @Autowired(required = true)
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    private String urlTemplate = "{baseUrl}?q={query}&site={site}&entqr=0&ud=1&sort=date%3AD%3AL%3Ad1&output=xml_no_dtd&oe=UTF-8&ie=UTF-8&proxyreload=1&entsp=0";
    
    public void setUrlTemplate(String urlTemplate) {
        this.urlTemplate = urlTemplate;
    }
    
    private String gsaBaseUrl;
    
    public void setBaseUrl(String gsaBaseUrl) {
        this.gsaBaseUrl = gsaBaseUrl;
    }
    
    private String gsaSite;
    
    public void setSite(String gsaSite) {
        this.gsaSite = gsaSite;
    }
    
    private String resultType = "googleAppliance";
    
    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    @Override
    public SearchResults getSearchResults(PortletRequest request,
            SearchRequest query) {
        final SearchResults results = search(query.getSearchTerms());
        results.setQueryId(query.getQueryId());
        results.setWindowId(request.getWindowID());
        return results;
    }
    
    protected SearchResults search(String query) {
        
        Map<String, String> vars = new HashMap<String, String>();
        vars.put("query", query);
        vars.put("baseUrl", gsaBaseUrl);
        vars.put("site", gsaSite);
        
        GsaResults gsaResults = restTemplate.getForObject(urlTemplate, GsaResults.class, vars);
        SearchResults results =  new SearchResults();
        for (GsaSearchResult gsaResult : gsaResults.getSearchResults()) {
            SearchResult result = new SearchResult();
            result.setTitle(gsaResult.getTitle());
            result.setExternalUrl(gsaResult.getLink());
            result.setSummary(gsaResult.getSnippet());
            result.getType().add(this.resultType);
            results.getSearchResult().add(result);
        }
        return results;
    }

}
