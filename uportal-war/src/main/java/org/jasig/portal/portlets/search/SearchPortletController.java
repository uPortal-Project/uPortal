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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Event;
import javax.portlet.EventRequest;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.Validate;
import org.jasig.portal.portlet.PortletUtils;
import org.jasig.portal.portlet.container.properties.ThemeNameRequestPropertiesManager;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.search.PortletUrl;
import org.jasig.portal.search.PortletUrlParameter;
import org.jasig.portal.search.PortletUrlType;
import org.jasig.portal.search.SearchConstants;
import org.jasig.portal.search.SearchRequest;
import org.jasig.portal.search.SearchResult;
import org.jasig.portal.search.SearchResults;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.url.IPortalUrlBuilder;
import org.jasig.portal.url.IPortalUrlProvider;
import org.jasig.portal.url.IPortletUrlBuilder;
import org.jasig.portal.url.UrlType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.EventMapping;

import com.google.common.cache.CacheBuilder;
import com.google.common.collect.MapMaker;

/**
 * SearchPortletController produces both a search form and results for configured
 * search services.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
@Controller
@RequestMapping("VIEW")
public class SearchPortletController {
    /**
     * 
     */
    private static final String SEARCH_RESULTS_CACHE_NAME = "searchResultsCache";

    protected final Logger logger = LoggerFactory.getLogger(getClass());    
    
    private List<IPortalSearchService> searchServices;
    
    @Resource(name="searchServices")
    public void setPortalSearchServices(List<IPortalSearchService> searchServices) {
        this.searchServices = searchServices;
    }

    private IPortalUrlProvider portalUrlProvider;

    @Autowired
    public void setPortalUrlProvider(IPortalUrlProvider urlProvider) {
        this.portalUrlProvider = urlProvider;
    }

    private IPortletWindowRegistry portletWindowRegistry;
    
    @Autowired
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }

    private IPortalRequestUtils portalRequestUtils;

    @Autowired(required = true)
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        Validate.notNull(portalRequestUtils);
        this.portalRequestUtils = portalRequestUtils;
    }

    @ActionMapping
    public void performSearch(@RequestParam(value = "query") String query, 
            ActionRequest request, ActionResponse response) {

        // construct a new search query object from the string query
        SearchRequest queryObj = new SearchRequest();
        String queryId = RandomStringUtils.randomAlphanumeric(32);
        queryObj.setQueryId(queryId);
        queryObj.setSearchTerms(query);
        
        final HttpServletRequest httpServletRequest = this.portalRequestUtils.getPortletHttpRequest(request);
        final IPortletWindowId portletWindowId = this.portletWindowRegistry.getPortletWindowId(httpServletRequest, request.getWindowID());

        // add search results from each portal search service to a new portal
        // search results object
        PortalSearchResults results = new PortalSearchResults();
        for (IPortalSearchService searchService : searchServices) {
            SearchResults serviceResults = searchService.getSearchResults(request, queryObj);
            addSearchResults(serviceResults, results, httpServletRequest, portletWindowId);
        }
        
        // place the portal search results object in the session using the queryId to namespace it
        PortletSession session = request.getPortletSession();
        Map<String, PortalSearchResults> searchResultsCache;
        synchronized (org.springframework.web.portlet.util.PortletUtils.getSessionMutex(session)) {
            searchResultsCache = (Map<String, PortalSearchResults>)session.getAttribute(SEARCH_RESULTS_CACHE_NAME);
            if (searchResultsCache == null) {
                searchResultsCache = CacheBuilder.newBuilder().maximumSize(50).<String, PortalSearchResults>build().asMap(); 
                session.setAttribute(SEARCH_RESULTS_CACHE_NAME, searchResultsCache);
            }
        }
        searchResultsCache.put(queryId, results);
        
        // send a search query event
        response.setEvent(SearchConstants.SEARCH_REQUEST_QNAME, queryObj);
        response.setRenderParameter("queryId", queryId);
    }
    
    @EventMapping(SearchConstants.SEARCH_RESULTS_QNAME_STRING)
    public void handleSearchResult(EventRequest request) {
        
        // get the portlet search results from the event
        Event event = request.getEvent();
        SearchResults portletSearchResults = (SearchResults) event.getValue();

        // get the existing portal search result from the session and append
        // the results for this event
        String queryId = portletSearchResults.getQueryId();
        
        PortletSession session = request.getPortletSession();
        final Map<String, PortalSearchResults> searchResultsCache = (Map<String, PortalSearchResults>)session.getAttribute(SEARCH_RESULTS_CACHE_NAME);
        if (searchResultsCache == null) {
            this.logger.warn("No searchResultsCache Map in the session, ignoring search results: " + event);
            return;
        }
        
        final PortalSearchResults results = searchResultsCache.get(queryId);
        if (results == null) {
            this.logger.warn("No PortalSearchResults found for queryId " + queryId + ", ignoring search results: " + event);
            return;
        }
        
        final String windowId = portletSearchResults.getWindowId();
        final HttpServletRequest httpServletRequest = this.portalRequestUtils.getPortletHttpRequest(request);
        final IPortletWindowId portletWindowId = this.portletWindowRegistry.getPortletWindowId(httpServletRequest, windowId);
        
        addSearchResults(portletSearchResults, results, httpServletRequest, portletWindowId);
    }

    private void addSearchResults(SearchResults portletSearchResults, PortalSearchResults results,
            final HttpServletRequest httpServletRequest, final IPortletWindowId portletWindowId) {
        for (SearchResult result : portletSearchResults.getSearchResult()) {
            final String resultUrl = getResultUrl(httpServletRequest, result, portletWindowId);
            this.logger.debug("Created {} with from {}", resultUrl, result.getTitle());
            results.addPortletSearchResults(resultUrl, result); 
        }
    }
    
    protected String getResultUrl(HttpServletRequest httpServletRequest, SearchResult result, IPortletWindowId portletWindowId) {
        final String externalUrl = result.getExternalUrl();
        if (externalUrl != null) {
            return externalUrl;
        }
        
        
        UrlType urlType = UrlType.RENDER;

        final PortletUrl portletUrl = result.getPortletUrl();
        if (portletUrl != null) {
            final PortletUrlType type = portletUrl.getType();
            if (type != null) {
                switch (type) {
                    case ACTION: {
                        urlType = UrlType.ACTION;
                        break;
                    }
                    default:
                    case RENDER: {
                        urlType = UrlType.RENDER;
                        break;
                    }
                    case RESOURCE: {
                        urlType = UrlType.RESOURCE;
                        break;
                    }
                }
            }
        }
        
        final IPortalUrlBuilder portalUrlBuilder = this.portalUrlProvider.getPortalUrlBuilderByPortletWindow(httpServletRequest, portletWindowId, urlType);
        final IPortletUrlBuilder portletUrlBuilder = portalUrlBuilder.getTargetedPortletUrlBuilder();
        
        if (portletUrl != null) {
            final String portletMode = portletUrl.getPortletMode();
            if (portletMode != null) {
                portletUrlBuilder.setPortletMode(PortletUtils.getPortletMode(portletMode));
            }
            final String windowState = portletUrl.getWindowState();
            if (windowState != null) {
                portletUrlBuilder.setWindowState(PortletUtils.getWindowState(windowState));
            }
            for (final PortletUrlParameter param : portletUrl.getParam()) {
                final String name = param.getName();
                final List<String> values = param.getValue();
                portletUrlBuilder.addParameter(name, values.toArray(new String[values.size()]));
            }
        }
        
        return portalUrlBuilder.getUrlString();
    }


    /**
     * Display a search form and show the results of a search query, if supplied.
     * 
     * @param request   portlet request
     * @param query     optional search query string
     * @return
     */
    @RequestMapping
    public ModelAndView getSearchResults(PortletRequest request,
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "queryId", required = false) String queryId
            ) {
        
        final Map<String,Object> model = new HashMap<String, Object>();
        model.put("query", query);

        if (queryId != null) {
	        PortletSession session = request.getPortletSession();
	        final Map<String, PortalSearchResults> searchResultsCache = (Map<String, PortalSearchResults>)session.getAttribute(SEARCH_RESULTS_CACHE_NAME);
	        if (searchResultsCache != null) {
	            final PortalSearchResults results = searchResultsCache.get(queryId);
	            model.put("results", results);
	        }
        }

        final boolean isMobile = isMobile(request);
        String viewName = isMobile ? "/jsp/Search/mobileSearch" : "/jsp/Search/search";
        
        return new ModelAndView(viewName, model);
    }
 
    public boolean isMobile(PortletRequest request) {
        String themeName = request.getProperty(ThemeNameRequestPropertiesManager.THEME_NAME_PROPERTY);
        return "UniversalityMobile".equals(themeName);
    }
    
    public static final class SearchResultWrapper {
        private final String portletUrl;
        private final String externalUrl;
        private final String title;
        private final String summary;
        private final List<String> types;

        public SearchResultWrapper(SearchResult result, String url) {
            this.title = result.getTitle();
            this.summary = result.getSummary();
            this.externalUrl = result.getExternalUrl();
            this.portletUrl = url;
            this.types = result.getType();
        }

        public String getPortletUrl() {
            return portletUrl;
        }

        public String getExternalUrl() {
            return externalUrl;
        }

        public String getTitle() {
            return title;
        }

        public String getSummary() {
            return summary;
        }

        public List<String> getTypes() {
            return types;
        }
        
    }
    
}
