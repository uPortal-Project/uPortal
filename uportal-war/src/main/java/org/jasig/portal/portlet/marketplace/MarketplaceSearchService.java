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

package org.jasig.portal.portlet.marketplace;

import java.util.List;
import java.util.Set;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.portlet.PortletUtils;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletCategoryRegistry;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlets.search.IPortalSearchService;
import org.jasig.portal.search.SearchResult;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.url.IPortalUrlBuilder;
import org.jasig.portal.url.IPortalUrlProvider;
import org.jasig.portal.url.IPortletUrlBuilder;
import org.jasig.portal.url.UrlType;
import org.jasig.portal.search.PortletUrl;
import org.jasig.portal.search.PortletUrlType;
import org.jasig.portal.search.SearchResults;
import org.jasig.portal.search.SearchRequest;
import org.jasig.portal.search.PortletUrlParameter;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * The search service that captures marketplace entries.
 * @author vertein
 */
public class MarketplaceSearchService implements IPortalSearchService {

    private IMarketplaceService marketplaceService;
    private IPortalUrlProvider portalUrlProvider;
    private IPortletWindowRegistry portletWindowRegistry;
    private IPortalRequestUtils portalRequestUtils;

    private IPersonManager personManager;

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
    public void setMarketplaceService(IMarketplaceService marketplaceService) {
        this.marketplaceService = marketplaceService;
    }

    @Autowired
    public void setPersonManager(final IPersonManager personManager) {
        this.personManager = personManager;
    }
    
    /**
     * Returns a list of search results that pertain to the marketplace
     * query is the query to search
     * will search name, title, description, fname, and captions
     */
    @Override
    public SearchResults getSearchResults(PortletRequest request,
            SearchRequest query) {
        
        final String queryString = query.getSearchTerms().toLowerCase();

        final HttpServletRequest portletServletRequest =
            this.portalRequestUtils.getPortletHttpRequest(request);
        final HttpServletRequest portalServletRequest =
            this.portalRequestUtils.getOriginalPortalRequest(portletServletRequest);
        final IPerson user = personManager.getPerson(portalServletRequest);

        final Set<MarketplacePortletDefinition> browseableDefinitions =
            marketplaceService.marketplacePortletDefinitionsBrowseableBy(user);
        
        final SearchResults results =  new SearchResults();
        for (final MarketplacePortletDefinition portlet : browseableDefinitions) {
            if (this.matches(queryString, portlet)) {
                final SearchResult result = new SearchResult();
                result.setTitle(portlet.getTitle());
                result.setSummary(portlet.getDescription());
                result.getType().add("marketplace");

                final IPortletWindow portletWindow =
                    this.portletWindowRegistry.getOrCreateDefaultPortletWindowByFname(
                        portletServletRequest, portlet.getFName());
                if (portletWindow != null) {
                    final IPortletWindowId portletWindowId = portletWindow.getPortletWindowId();
                    final IPortalUrlBuilder portalUrlBuilder =
                        this.portalUrlProvider.getPortalUrlBuilderByPortletFName(
                            portletServletRequest, portlet.getFName(), UrlType.RENDER);
                    final IPortletUrlBuilder portletUrlBuilder = portalUrlBuilder.getPortletUrlBuilder(portletWindowId);
                    portletUrlBuilder.setWindowState(PortletUtils.getWindowState("maximized"));
                    result.setExternalUrl(portalUrlBuilder.getUrlString());                    
                }
                
                PortletUrl url = new PortletUrl();
                url.setType(PortletUrlType.RENDER);
                url.setPortletMode("VIEW");
                url.setWindowState("maximized");
                PortletUrlParameter actionParam = new PortletUrlParameter();
                actionParam.setName("action");
                actionParam.getValue().add("view");
                url.getParam().add(actionParam);
                PortletUrlParameter fNameParam = new PortletUrlParameter();
                fNameParam.setName("fName");
                fNameParam.getValue().add(portlet.getFName());
                url.getParam().add(fNameParam);
                result.setPortletUrl(url);
                //Add the result to list to return
                results.getSearchResult().add(result);
            }
    	}
        return results;
    }
    
    /**
     * @param query
     * @param portlet
     * @return boolean whether query matched criteria in the marketplace portlet definition
     */
    protected boolean matches(String query, MarketplacePortletDefinition portlet) {
        return portlet.getTitle().contains(query) ||
                portlet.getName().contains(query) ||
                (portlet.getDescription() != null && portlet.getDescription().contains(query)) ||
                portlet.getFName().contains(query) ||
                this.captionMatches(query, portlet.getScreenShots()) ||
                this.releaseNotesMatches(query, portlet.getPortletReleaseNotes())	
                ;
    }
    
    /**
     * @param query
     * @param screenShots
     * @return boolean whether caption matches.  Used by matches method
     */
    protected boolean captionMatches(String query, List<ScreenShot> screenShots){
    	for(ScreenShot screenShot: screenShots){
    		for(String caption: screenShot.getCaptions()){
    			if(caption.contains(query)){
    				return true;
    			}
    		}
    	}
    	return false;
    }
    
    /**
     * @param query
     * @param portletReleaseNotes
     * @return boolean whether releaseNotes notes matches.  Used by matches method
     */
    protected boolean releaseNotesMatches(String query, PortletReleaseNotes portletReleaseNotes){
    	if(portletReleaseNotes.getReleaseNotes()!=null){
    		for(String notes : portletReleaseNotes.getReleaseNotes()){
    			if(notes.contains(query)){
    				return true;
    			}
    		}
    	}
    	return false;
    }
}
