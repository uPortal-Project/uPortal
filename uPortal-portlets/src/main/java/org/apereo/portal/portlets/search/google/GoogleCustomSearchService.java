/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.portlets.search.google;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.portlet.PortletRequest;
import org.apereo.portal.portlets.search.IPortalSearchService;
import org.apereo.portal.search.SearchRequest;
import org.apereo.portal.search.SearchResult;
import org.apereo.portal.search.SearchResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestOperations;

/**
 * Service for searching using the Google Custom-Search API:
 * https://developers.google.com/custom-search/v1/overview
 */
public class GoogleCustomSearchService implements IPortalSearchService {
    public static final String QUERY_PARAM = "q";
    public static final String VERSION_PARAM = "v"; // in new url
    public static final String USER_IP_PARAM = "userip"; // remove
    public static final String RESULT_SIZE_PARAM = "rsz"; // num
    public static final String START_PARAM = "start";
    public static final String CUSTOM_SEARCH_PARAM = "cx";
    public static final String VERSION = "1.0";

    private static final String BASE_SEARCH_URL =
            "http://ajax.googleapis.com/ajax/services/search/web?"
                    + QUERY_PARAM
                    + "={"
                    + QUERY_PARAM
                    + "}&"
                    + VERSION_PARAM
                    + "={"
                    + VERSION_PARAM
                    + "}&"
                    + USER_IP_PARAM
                    + "={"
                    + USER_IP_PARAM
                    + "}&"
                    + RESULT_SIZE_PARAM
                    + "={"
                    + RESULT_SIZE_PARAM
                    + "}&"
                    + CUSTOM_SEARCH_PARAM
                    + "={"
                    + CUSTOM_SEARCH_PARAM
                    + "}";

    private String resultSize = "large";
    private String customSearchId;

    @Value("${org.apereo.portal.portlets.googleWebSearch.search.result.type:googleCustom}")
    private String resultType = "googleCustom";

    private RestOperations restOperations;

    public void setResultSize(String resultSize) {
        this.resultSize = resultSize;
    }

    public void setCustomSearchId(String customSearchId) {
        this.customSearchId = customSearchId;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    @Autowired
    public void setRestOperations(RestOperations restOperations) {
        this.restOperations = restOperations;
    }

    @Override
    public SearchResults getSearchResults(PortletRequest request, SearchRequest query) {
        final Map<String, Object> parameters = new LinkedHashMap<>();

        parameters.put(VERSION_PARAM, VERSION);
        parameters.put(RESULT_SIZE_PARAM, resultSize);
        parameters.put(CUSTOM_SEARCH_PARAM, customSearchId);
        parameters.put(QUERY_PARAM, query.getSearchTerms());
        parameters.put(USER_IP_PARAM, request.getProperty("REMOTE_ADDR"));
        parameters.put(START_PARAM, query.getStartIndex());

        final JsonNode googleResponse =
                this.restOperations.getForObject(BASE_SEARCH_URL, JsonNode.class, parameters);

        final SearchResults searchResults = new SearchResults();
        searchResults.setQueryId(query.getQueryId());
        final List<SearchResult> searchResultList = searchResults.getSearchResult();

        final JsonNode results = googleResponse.get("responseData").get("results");
        for (final Iterator<JsonNode> resultItr = results.elements(); resultItr.hasNext(); ) {
            final JsonNode googleResult = resultItr.next();

            final SearchResult searchResult = new SearchResult();
            searchResult.setTitle(googleResult.get("title").asText());
            searchResult.setSummary(googleResult.get("content").asText());
            searchResult.setExternalUrl(googleResult.get("clicktrackUrl").asText());
            searchResult.getType().add(resultType);

            searchResultList.add(searchResult);
        }

        return searchResults;
    }
}
