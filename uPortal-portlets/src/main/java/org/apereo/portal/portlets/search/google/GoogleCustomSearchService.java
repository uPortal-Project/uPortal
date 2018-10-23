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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestOperations;

/**
 * Service for searching using the Google Custom-Search API:
 * https://developers.google.com/custom-search/v1/overview
 */
public class GoogleCustomSearchService implements IPortalSearchService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleCustomSearchService.class);

    public static final String QUERY_PARAM = "q"; // required
    public static final String CUSTOM_SEARCH_PARAM = "cx"; // required
    public static final String KEY_PARAM = "key"; // required
    public static final String USER_IP_PARAM = "userIp";
    public static final String START_PARAM = "start";

    private static final String BASE_SEARCH_URL =
            "https://www.googleapis.com/customsearch/v1?"
                    + QUERY_PARAM
                    + "={"
                    + QUERY_PARAM
                    + "}&"
                    + KEY_PARAM
                    + "={"
                    + KEY_PARAM
                    + "}&"
                    + USER_IP_PARAM
                    + "={"
                    + USER_IP_PARAM
                    + "}&"
                    + START_PARAM
                    + "={"
                    + START_PARAM
                    + "}&"
                    + CUSTOM_SEARCH_PARAM
                    + "={"
                    + CUSTOM_SEARCH_PARAM
                    + "}";

    private String customSearchId;
    private String key;

    @Value("${org.apereo.portal.portlets.googleWebSearch.search.result.type:googleCustom}")
    private String resultType = "googleCustom";

    private RestOperations restOperations;

    public GoogleCustomSearchService() {
        logger.debug("GoogleCustomSearchService bean instantiated.");
    }

    public void setCustomSearchId(String customSearchId) {
        this.customSearchId = customSearchId;
    }

    public void setKey(String key) {
        this.key = key;
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

        parameters.put(KEY_PARAM, key);
        parameters.put(CUSTOM_SEARCH_PARAM, customSearchId);
        parameters.put(QUERY_PARAM, query.getSearchTerms());
        parameters.put(USER_IP_PARAM, request.getProperty("REMOTE_ADDR"));
        parameters.put(START_PARAM, query.getStartIndex() != null ? query.getStartIndex() : 1);

        logger.debug("search parameters: {}", parameters);

        final JsonNode googleResponse =
                this.restOperations.getForObject(BASE_SEARCH_URL, JsonNode.class, parameters);

        logger.debug("response: {}", googleResponse);

        final SearchResults searchResults = new SearchResults();
        searchResults.setQueryId(query.getQueryId());
        final List<SearchResult> searchResultList = searchResults.getSearchResult();

        final JsonNode items = googleResponse.get("items"); // .get("items");
        logger.debug("response items: {}", items);
        if (items != null) {
            for (final Iterator<JsonNode> resultItr = items.elements(); resultItr.hasNext(); ) {
                final JsonNode googleResult = resultItr.next();

                final SearchResult searchResult = new SearchResult();
                searchResult.setTitle(googleResult.get("title").asText());
                searchResult.setSummary(googleResult.get("snippet").asText());
                searchResult.setExternalUrl(googleResult.get("link").asText());
                searchResult.getType().add(resultType);

                logger.debug("uPortal search result item: {}", searchResult);
                searchResultList.add(searchResult);
            }
        }

        return searchResults;
    }
}
