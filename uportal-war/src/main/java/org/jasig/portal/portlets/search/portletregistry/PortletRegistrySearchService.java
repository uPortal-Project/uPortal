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
import org.jasig.portal.portlets.search.IPortalSearchService;
import org.jasig.portal.search.SearchRequest;
import org.jasig.portal.search.SearchResult;
import org.jasig.portal.search.SearchResults;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.url.IPortalUrlBuilder;
import org.jasig.portal.url.IPortalUrlProvider;
import org.jasig.portal.url.IPortletUrlBuilder;
import org.jasig.portal.url.UrlType;
import org.springframework.beans.factory.annotation.Autowired;

public class PortletRegistrySearchService implements IPortalSearchService {

    private IPortletDefinitionRegistry portletDefinitionRegistry;
    private IPortalUrlProvider portalUrlProvider;
    private IPortletWindowRegistry portletWindowRegistry;
    private IPortalRequestUtils portalRequestUtils;

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
                result.getType().add("Portal Content");

                final IPortletWindow portletWindow = this.portletWindowRegistry.getOrCreateDefaultPortletWindowByFname(httpServletRequest, portlet.getFName());
                if (portletWindow != null) {
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
    
    protected boolean matches(String query, IPortletDefinition portlet) {
        return portlet.getTitle().toLowerCase().contains(query) ||
                portlet.getName().toLowerCase().contains(query) ||
                (portlet.getDescription() != null && portlet.getDescription().toLowerCase().contains(query)) ||
                portlet.getFName().toLowerCase().contains(query);
    }

}
