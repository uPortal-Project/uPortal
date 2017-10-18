/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apereo.portal.portlets.search.portletregistry;

import java.util.List;
import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.registry.IPortletDefinitionRegistry;
import org.apereo.portal.portlets.search.IPortalSearchService;
import org.apereo.portal.portlets.search.PortletRegistryUtil;
import org.apereo.portal.search.SearchRequest;
import org.apereo.portal.search.SearchResult;
import org.apereo.portal.search.SearchResults;
import org.apereo.portal.url.IPortalRequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * Return portlet search results based on matching specific portlet definition values. Currently,
 * the values tested are:
 *
 * <ul>
 *   <li>name
 *   <li>fname
 *   <li>title
 *   <li>description
 * </ul>
 *
 * <p>Portlet content is not matched.
 *
 * <p>Results comply with requester's permissions.
 */
public class PortletRegistrySearchService implements IPortalSearchService {

    @Autowired private IPortletDefinitionRegistry portletDefinitionRegistry;

    @Autowired private IPortalRequestUtils portalRequestUtils;

    @Autowired private PortletRegistryUtil portletRegistryUtil;

    @Value("${org.apereo.portal.portlets.portletRegistry.search.result.type:Portlet List}")
    private String searchResultType = "Portlet List";

    @Override
    public SearchResults getSearchResults(PortletRequest request, SearchRequest query) {
        final String queryString = query.getSearchTerms().toLowerCase();
        final List<IPortletDefinition> portlets =
                portletDefinitionRegistry.getAllPortletDefinitions();
        final HttpServletRequest httpServletRequest =
                this.portalRequestUtils.getPortletHttpRequest(request);

        final SearchResults results = new SearchResults();
        for (IPortletDefinition portlet : portlets) {
            if (portletRegistryUtil.matches(queryString, portlet)) {
                final SearchResult result = new SearchResult();
                result.setTitle(portlet.getTitle());
                result.setSummary(portlet.getDescription());
                result.getType().add(searchResultType);

                String url = portletRegistryUtil.buildPortletUrl(httpServletRequest, portlet);
                if (url != null) {
                    result.setExternalUrl(url);
                    results.getSearchResult().add(result);
                }
            }
        }

        return results;
    }
}
