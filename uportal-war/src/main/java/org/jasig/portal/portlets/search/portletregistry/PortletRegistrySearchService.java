/**
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
package org.jasig.portal.portlets.search.portletregistry;

import java.util.List;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.portlet.PortletUtils;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlets.groupselector.EntityEnum;
import org.jasig.portal.portlets.search.IPortalSearchService;
import org.jasig.portal.search.SearchRequest;
import org.jasig.portal.search.SearchResult;
import org.jasig.portal.search.SearchResults;
import org.jasig.portal.security.IAuthorizationService;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.url.IPortalUrlBuilder;
import org.jasig.portal.url.IPortalUrlProvider;
import org.jasig.portal.url.IPortletUrlBuilder;
import org.jasig.portal.url.UrlType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class PortletRegistrySearchService implements IPortalSearchService {

    private IPortletDefinitionRegistry portletDefinitionRegistry;
    private IPortalUrlProvider portalUrlProvider;
    private IPortletWindowRegistry portletWindowRegistry;
    private IPortalRequestUtils portalRequestUtils;
    private IAuthorizationService authorizationService;

    @Value("${org.jasig.portal.portlets.portletRegistry.search.result.type:Portlet List}")
    private String searchResultType = "Portlet List";

    @Autowired
    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }

    @Autowired
    public void setPortalUrlProvider(IPortalUrlProvider urlProvider) {
        this.portalUrlProvider = urlProvider;
    }

    @Autowired
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }

    @Autowired
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }

    @Autowired
    public void setAuthorizationService(IAuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Override
    public SearchResults getSearchResults(PortletRequest request,
            SearchRequest query) {
        
        final String queryString = query.getSearchTerms().toLowerCase();
        final List<IPortletDefinition> portlets = portletDefinitionRegistry.getAllPortletDefinitions();
        
        final HttpServletRequest httpServletRequest = this.portalRequestUtils.getPortletHttpRequest(request);
        
        final SearchResults results =  new SearchResults();
        for (IPortletDefinition portlet : portlets) {
            if (matches(queryString, portlet)) {
                final SearchResult result = new SearchResult();
                result.setTitle(portlet.getTitle());
                result.setSummary(portlet.getDescription());
                result.getType().add(searchResultType);

                final IPortletWindow portletWindow = this.portletWindowRegistry.getOrCreateDefaultPortletWindowByFname(httpServletRequest, portlet.getFName());
                // portletWindow is null if user does not have access to portlet.
                // If user does not have browse permission, exclude the portlet.
                if (portletWindow != null && authorizationService.canPrincipalBrowse(
                        authorizationService.newPrincipal(request.getRemoteUser(), EntityEnum.PERSON.getClazz()),
                        portlet)) {
                    final IPortletWindowId portletWindowId = portletWindow.getPortletWindowId();
                    final IPortalUrlBuilder portalUrlBuilder = this.portalUrlProvider.getPortalUrlBuilderByPortletFName(httpServletRequest, portlet.getFName(), UrlType.RENDER);
                    final IPortletUrlBuilder portletUrlBuilder = portalUrlBuilder.getPortletUrlBuilder(portletWindowId);
                    portletUrlBuilder.setWindowState(PortletUtils.getWindowState("maximized"));
                    result.setExternalUrl(portalUrlBuilder.getUrlString());
                    results.getSearchResult().add(result);
                }

            }
        }

        return results;
    }

    /**
     * Performs a case-insensitive comparison of the user's query against 
     * several important fields from the {@link IPortletDefinition}.
     * 
     * @param query The user's search terms, which seem to be forced lower-case
     * @param portlet
     * @return
     */
    protected boolean matches(final String query, final IPortletDefinition portlet) {
        /*
         * The query parameter is coming in lower case always (even when upper
         * or mixed case is entered by the user).  We really want a case-
         * insensitive comparison here anyway;  for safety, we will make certain
         * it is insensitive.
         */
        final String lcQuery = query.toLowerCase();
        final boolean titleMatch = portlet.getTitle().toLowerCase().contains(lcQuery);
        final boolean nameMatch = portlet.getName().toLowerCase().contains(lcQuery);
        final boolean descMatch = portlet.getDescription() != null && portlet.getDescription().toLowerCase().contains(lcQuery);
        final boolean fnameMatch = portlet.getFName().toLowerCase().contains(lcQuery);
        return titleMatch || nameMatch || descMatch || fnameMatch;
    }

}
