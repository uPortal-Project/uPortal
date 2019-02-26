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
package org.apereo.portal.rest.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * REST Search endpoint that aggregates search results from pluggable strategies.
 *
 * <p>JSON results example (whitespace added for clarity):
 *
 * <pre><code>
 * {
 *   "people": [
 *     {
 *       "telephoneNumber": [
 *         "(555) 555-5555"
 *       ],
 *       "mail": [
 *         "amy.administrator@university.edu"
 *       ],
 *       "displayName": [
 *         "Amy Administrator"
 *       ],
 *       "givenName": [
 *         "Amy"
 *       ],
 *       "sn": [
 *         "Administrator"
 *       ],
 *       "title": [
 *         "Portal Administrator"
 *       ],
 *       "department": [
 *         "IT Services"
 *       ],
 *       "username": [
 *         "admin"
 *       ]
 *     },
 *     {
 *       "telephoneNumber": [
 *         "(555) 555-5555"
 *       ],
 *       "mail": [
 *         "samuel.staff@example.org"
 *       ],
 *       "displayName": [
 *         "Samuel Staff"
 *       ],
 *       "givenName": [
 *         "Samuel"
 *       ],
 *       "sn": [
 *         "Staff"
 *       ],
 *       "title": [
 *         "Database Administrator"
 *       ],
 *       "department": [
 *         "IT Services"
 *       ],
 *       "username": [
 *         "staff"
 *       ]
 *     }
 *   ],
 *   "portlets": [
 *     {
 *       "description": "Campus News",
 *       "fname": "campus-news",
 *       "name": "Campus News",
 *       "title": "Campus News",
 *       "url": "/uPortal/f/news-fav-collection/p/campus-news.u33l1n8/max/render.uP"
 *     },
 *     {
 *       "description": "Top stories from The Chronicle of Higher Education: Wired Campus Edition",
 *       "fname": "chronicle-wired",
 *       "name": "The Chronicle: Wired Campus",
 *       "title": "The Chronicle: Wired Campus",
 *       "url": "/uPortal/f/news-fav-collection/p/chronicle-wired.u33l1n9/max/render.uP"
 *     },
 *     {
 *       "description": "Calendar of campus, academic, and personal events.",
 *       "fname": "calendar",
 *       "name": "Calendar",
 *       "title": "Calendar",
 *       "url": "/uPortal/f/welcome/p/calendar.u17l1n13/max/render.uP"
 *     }
 *   ]
 * }
 * </code></pre>
 *
 * @since 5.0
 */
@Controller
@RequestMapping("/v5-0/portal/search")
public class SearchRESTController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ObjectMapper jsonMapper = new ObjectMapper();

    @Autowired private Set<ISearchStrategy> searchStrategies;

    @RequestMapping(method = RequestMethod.GET)
    public void search(
            @RequestParam("q") String query,
            @RequestParam(value = "type", required = false) Set<String> types,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        final Map<String, List<?>> searchResults = new TreeMap<>();
        if (logger.isDebugEnabled()) {
            logger.debug("Searching with q={}, type={}", query, ArrayUtils.toString(types.toArray()));
        }

        for (ISearchStrategy strategy : searchStrategies) {
            if(types == null || types.isEmpty() || types.contains("") || types.contains(strategy.getResultTypeName())) {
                searchResults.put(strategy.getResultTypeName(), strategy.search(query, request));
            }
        }

        if (searchResults.isEmpty()) {
            logger.debug("Nothing found for query string: {}", query);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            jsonMapper.writeValue(response.getOutputStream(), searchResults);
        }
    }
}
