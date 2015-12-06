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
package org.jasig.portal.portlets.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Event;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.jasig.portal.portlet.PortletUtils;
import org.jasig.portal.portlet.container.properties.ThemeNameRequestPropertiesManager;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.search.PortletUrl;
import org.jasig.portal.search.PortletUrlParameter;
import org.jasig.portal.search.PortletUrlType;
import org.jasig.portal.search.SearchConstants;
import org.jasig.portal.search.SearchRequest;
import org.jasig.portal.search.SearchResult;
import org.jasig.portal.search.SearchResults;
import org.jasig.portal.spring.spel.IPortalSpELService;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.url.IPortalUrlBuilder;
import org.jasig.portal.url.IPortalUrlProvider;
import org.jasig.portal.url.IPortletUrlBuilder;
import org.jasig.portal.url.UrlType;
import org.jasig.portal.utils.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelParseException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.EventMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

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
    private static final String SEARCH_RESULTS_CACHE_NAME = SearchPortletController.class.getName() + ".searchResultsCache";
    private static final String SEARCH_COUNTER_NAME = SearchPortletController.class.getName() + ".searchCounter";
    private static final String SEARCH_HANDLED_CACHE_NAME = SearchPortletController.class.getName() + ".searchHandledCache";
    private static final String SEARCH_LAST_QUERY_ID = SearchPortletController.class.getName() + ".searchLastQueryId";
    private static final String AJAX_MAX_QUERIES_URL = "/scripts/search/hitMaxQueriesGive404Result.json";   // URL to nonexistent file
    private static final String SEARCH_LAUNCH_FNAME = "searchLaunchFname";
    private static final String AJAX_RESPONSE_RESOURCE_ID = "retrieveSearchJSONResults";
    private static final List<String> UNDEFINED_SEARCH_RESULT_TYPE = Arrays.asList(new String[] {"UndefinedResultType"});
    private static final String AUTOCOMPLETE_MAX_TEXT_LENGTH_PREF_NAME = "autocompleteMaxTextSize";

    protected final Logger logger = LoggerFactory.getLogger(getClass());    
    
    private IPortalUrlProvider portalUrlProvider;
    private IPortletWindowRegistry portletWindowRegistry;
    private IPortalRequestUtils portalRequestUtils;
    private List<IPortalSearchService> searchServices;
    
    // Map from result-type -> Set<tab-key>
    private Map<String, Set<String>> resultTypeMappings = Collections.emptyMap();
    private List<String> tabKeys = Collections.emptyList();
    private String defaultTabKey = "portal.results";
    private int maximumSearchesPerMinute = 18;

    private int maxAutocompleteSearchResults = 10;
    // Map of (search result type, priority) to prioritize search autocomplete results.  0 is default priority.
    // > 0 is higher priority, < 0 is lower priority.
    private Map<String, Integer> autocompleteResultTypeToPriorityMap = new HashMap<String, Integer>();

    private IPortalSpELService spELService;

    // TODO: It would be better to revise the search event to have a set of ignored types and expect the
    // search event listeners to voluntarily ignore the search event if they are one of the ignored types (and
    // again filtering here in case the search event listener doesn't respect the ignore set).
    // This requires changing the SearchEvent and unfortunately there is not time for that now.
    private Set<String> autocompleteIgnoreResultTypes = new HashSet<String>();
    
    @Resource(name="searchServices")
    public void setPortalSearchServices(List<IPortalSearchService> searchServices) {
        this.searchServices = searchServices;
    }
    
    /**
     * The messages property key to use for the default results tab
     */
    @Value("${org.jasig.portal.portlets.searchSearchPortletController.defaultTabKey:portal.results}")
    public void setDefaultTabKey(String defaultTabKey) {
        this.defaultTabKey = defaultTabKey;
    }

    /**
     * Set the maximum number of a user can execute per minute
     */
    @Value("${org.jasig.portal.portlets.searchSearchPortletController.maximumSearchesPerMinute:18}")
    public void setMaximumSearchesPerMinute(int maximumSearchesPerMinute) {
        this.maximumSearchesPerMinute = maximumSearchesPerMinute;
    }

    public int getMaxAutocompleteSearchResults() {
        return maxAutocompleteSearchResults;
    }

    /**
     * Set the maximum number of autocomplete results for a search
     */
    @Value("${org.jasig.portal.portlets.searchSearchPortletController.autocompleteSearchResults:10}")
    public void setMaxAutocompleteSearchResults(int maxAutocompleteSearchResults) {
        this.maxAutocompleteSearchResults = maxAutocompleteSearchResults;
    }

    public Map<String, Integer> getAutocompleteResultTypeToPriorityMap() {
        return autocompleteResultTypeToPriorityMap;
    }

    @Resource(name = "searchAutocompletePriorityMap")
    public void setAutocompleteResultTypeToPriorityMap(Map<String, Integer> autocompleteResultTypeToPriorityMap) {
        this.autocompleteResultTypeToPriorityMap = autocompleteResultTypeToPriorityMap;
    }

    @Resource(name = "searchAutocompleteIgnoreResultTypes")
    public void setAutocompleteIgnoreResultTypes(Set<String> autocompleteIgnoreResultTypes) {
        this.autocompleteIgnoreResultTypes = autocompleteIgnoreResultTypes;
    }

    @Autowired
    @Qualifier(value = "portalSpELServiceImpl")
    public void setSpELService(IPortalSpELService spELService) {
        this.spELService = spELService;
    }

    /**
     * Set the mappings from TabKey to ResultType. The map keys must be strings but the values
     * can be either String or Collection<String>
     */
    @SuppressWarnings("unchecked")
    @Resource(name="searchTabs")
    //Map of tab-key to string or collection<string> of search result types
    public void setSearchTabs(Map<String, Object> searchTabMappings) {
        final Map<String, Set<String>> resultTypeMappingsBuilder = new LinkedHashMap<String, Set<String>>();
        final List<String> tabKeysBuilder = new ArrayList<String>(searchTabMappings.size());
        
        for (final Map.Entry<String, Object> tabMapping : searchTabMappings.entrySet()) {
            final String tabKey = tabMapping.getKey();
            tabKeysBuilder.add(tabKey);
            
            final Object resultTypes = tabMapping.getValue();
            if (resultTypes instanceof Collection) {
                for (final String resultType : (Collection<String>)resultTypes) {
                    addTabKey(resultTypeMappingsBuilder, tabKey, resultType);
                }
            }
            else {
                final String resultType = (String)resultTypes;
                addTabKey(resultTypeMappingsBuilder, tabKey, resultType);
            }
        }
        
        this.resultTypeMappings = resultTypeMappingsBuilder;
        this.tabKeys = tabKeysBuilder;
    }
    protected void addTabKey(final Map<String, Set<String>> resultTypeMappingsBuilder, final String tabKey, final String resultType) {
        Set<String> tabKeys = resultTypeMappingsBuilder.get(resultType);
        if (tabKeys == null) {
            tabKeys = new LinkedHashSet<String>();
            resultTypeMappingsBuilder.put(resultType, tabKeys);
        }
        tabKeys.add(tabKey);
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
        Validate.notNull(portalRequestUtils);
        this.portalRequestUtils = portalRequestUtils;
    }

    @SuppressWarnings("unchecked")
    @ActionMapping
    public void performSearch(@RequestParam(value = "query") String query, 
            ActionRequest request, ActionResponse response,
            @RequestParam(value="ajax", required=false) final boolean ajax)
            throws IOException {
        final PortletSession session = request.getPortletSession();
        
        final String queryId = RandomStringUtils.randomAlphanumeric(32);
        
        Cache<String, Boolean> searchCounterCache;
        synchronized (org.springframework.web.portlet.util.PortletUtils.getSessionMutex(session)) {
            searchCounterCache = (Cache<String, Boolean>)session.getAttribute(SEARCH_COUNTER_NAME);
            if (searchCounterCache == null) {
                searchCounterCache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).<String, Boolean>build(); 
                session.setAttribute(SEARCH_COUNTER_NAME, searchCounterCache);
            }
        }
        
        //Store the query id to track number of searches/minute
        searchCounterCache.put(queryId, Boolean.TRUE);
        if (searchCounterCache.size() > this.maximumSearchesPerMinute) {
            //Make sure old data is expired
            searchCounterCache.cleanUp();
            
            //Too many searches in the last minute, fail the search
            if (searchCounterCache.size() > this.maximumSearchesPerMinute) {
                logger.debug("Rejecting search for '{}', exceeded max queries per minute for user", query);

                if (!ajax) {
                    response.setRenderParameter("hitMaxQueries", Boolean.TRUE.toString());
                    response.setRenderParameter("query", query);
                } else {
                    // For Ajax return to a nonexistent file to generate the 404 error since it was easier for the
                    // UI to have an error response.
                    final String contextPath = request.getContextPath();
                    response.sendRedirect(contextPath + AJAX_MAX_QUERIES_URL);
                }
                return;
            }
        }

        // construct a new search query object from the string query
        final SearchRequest queryObj = new SearchRequest();
        queryObj.setQueryId(queryId);
        queryObj.setSearchTerms(query);
        
        // Create the session-shared results object
        final PortalSearchResults results = new PortalSearchResults(defaultTabKey, resultTypeMappings);
        
        // place the portal search results object in the session using the queryId to namespace it
        Cache<String, PortalSearchResults> searchResultsCache;
        synchronized (org.springframework.web.portlet.util.PortletUtils.getSessionMutex(session)) {
            searchResultsCache = (Cache<String, PortalSearchResults>)session.getAttribute(SEARCH_RESULTS_CACHE_NAME);
            if (searchResultsCache == null) {
                searchResultsCache = CacheBuilder.newBuilder().maximumSize(20).expireAfterAccess(5, TimeUnit.MINUTES).<String, PortalSearchResults>build(); 
                session.setAttribute(SEARCH_RESULTS_CACHE_NAME, searchResultsCache);
            }
            // Save the last queryId for an ajax autocomplete search response.
            session.setAttribute(SEARCH_LAST_QUERY_ID, queryId);
        }
        searchResultsCache.put(queryId, results);

        /*
         * TODO:  For autocomplete I wish we didn't have to go through a whole render phase just
         * to trigger the events-based features of the portlet, but atm I don't
         * see a way around it, since..
         *
         *   - (1) You can only start an event chain in the Action phase;  and
         *   - (2) You can only return JSON in a Resource phase;  and
         *   - (3) An un-redirected Action phase leads to a Render phase, not a
         *     Resource phase :(
         *
         * It would be awesome either (first choice) to do Action > Event > Resource,
         * or Action > sendRedirect() followed by a Resource request.
         *
         * As it stands, this implementation will trigger a complete render on
         * the portal needlessly.
         */

        // send a search query event
        response.setEvent(SearchConstants.SEARCH_REQUEST_QNAME, queryObj);

        logger.debug("Query initiated for queryId {}, query {}", queryId, query);
        response.setRenderParameter("queryId", queryId);
        response.setRenderParameter("query", query);

    }

    /**
     * Performs a search of the explicitly configured {@link IPortalSearchService}s. This
     * is done as an event handler so that it can run concurrently with the other portlets
     * handling the search request
     */
    @SuppressWarnings("unchecked")
    @EventMapping(SearchConstants.SEARCH_REQUEST_QNAME_STRING)
    public void handleSearchRequest(EventRequest request, EventResponse response) {
        // UP-3887 Design flaw.  Both the searchLauncher portlet instance and the search portlet instance receive
        // searchRequest and searchResult events because they are in the same portlet code base (to share
        // autosuggest_handler.jsp and because we have to calculate the search portlet url for the ajax call)
        // and share the portlet.xml which defines the event handling behavior.
        // If this instance is the searchLauncher, ignore the searchResult. The search was submitted to the search
        // portlet instance.
        final String searchLaunchFname = request.getPreferences().getValue(SEARCH_LAUNCH_FNAME, null);
        if (searchLaunchFname != null) {
            // Noisy in debug mode so commented out log statement
            // logger.debug("SearchLauncher does not participate in SearchRequest events so discarding message");
            return;
        }

        final Event event = request.getEvent();
        final SearchRequest searchQuery = (SearchRequest)event.getValue();
        
        //Map used to track searches that have been handled, used so that one search doesn't get duplicate results
        ConcurrentMap<String, Boolean> searchHandledCache;
        final PortletSession session = request.getPortletSession();
        synchronized (org.springframework.web.portlet.util.PortletUtils.getSessionMutex(session)) {
            searchHandledCache = (ConcurrentMap<String, Boolean>)session.getAttribute(SEARCH_HANDLED_CACHE_NAME, PortletSession.APPLICATION_SCOPE);
            if (searchHandledCache == null) {
                searchHandledCache = CacheBuilder.newBuilder().maximumSize(20).expireAfterAccess(5, TimeUnit.MINUTES).<String, Boolean>build().asMap(); 
                session.setAttribute(SEARCH_HANDLED_CACHE_NAME, searchHandledCache, PortletSession.APPLICATION_SCOPE);
            }
        }
        
        final String queryId = searchQuery.getQueryId();
        if (searchHandledCache.putIfAbsent(queryId, Boolean.TRUE) != null) {
            //Already handled this search request
            return;
        }
        
        //Create the results
        final SearchResults results = new SearchResults();
        results.setQueryId(queryId);
        results.setWindowId(request.getWindowID());
        final List<SearchResult> searchResultList = results.getSearchResult();
        
        //Run the search for each service appending the results
        for (IPortalSearchService searchService : searchServices) {
            try {
                logger.debug("For queryId {}, query '{}', searching search service {}", queryId,
                        searchQuery.getSearchTerms(), searchService.getClass().toString());
                final SearchResults serviceResults = searchService.getSearchResults(request, searchQuery);
                logger.debug("For queryId {}, obtained {} results from search service {}", queryId,
                        serviceResults.getSearchResult().size(), searchService.getClass().toString());
                searchResultList.addAll(serviceResults.getSearchResult());
            }
            catch (Exception e) {
                logger.warn(searchService.getClass() + " threw an exception when searching, it will be ignored. " + searchQuery, e);
            }
        }
        
        //Respond with a results event if results were found
        if (!searchResultList.isEmpty()) {
            response.setEvent(SearchConstants.SEARCH_RESULTS_QNAME, results);
        }
    }
    
    /**
     * Handles all the SearchResults events coming back from portlets
     */
    @EventMapping(SearchConstants.SEARCH_RESULTS_QNAME_STRING)
    public void handleSearchResult(EventRequest request) {

        // UP-3887 Design flaw.  Both the searchLauncher portlet instance and the search portlet instance receive
        // searchRequest and searchResult events because they are in the same portlet code base (to share
        // autosuggest_handler.jsp and because we have to calculate the search portlet url for the ajax call)
        // and share the portlet.xml which defines the event handling behavior.
        // If this instance is the searchLauncher, ignore the searchResult. The search was submitted to the search
        // portlet instance.
        final String searchLaunchFname = request.getPreferences().getValue(SEARCH_LAUNCH_FNAME, null);
        if (searchLaunchFname != null) {
            // Noisy in debug mode so commenting out debug message
            // logger.debug("SearchLauncher does not process SearchResponse events so discarding message");
            return;
        }

        final Event event = request.getEvent();
        final SearchResults portletSearchResults = (SearchResults) event.getValue();

        // get the existing portal search result from the session and append
        // the results for this event
        final String queryId = portletSearchResults.getQueryId();
        final PortalSearchResults results = this.getPortalSearchResults(request, queryId);
        if (results == null) {
            this.logger.warn("No PortalSearchResults found for queryId {}, ignoring search results from {}",
                    queryId, getSearchResultsSource(portletSearchResults));
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("For queryId {}, adding {} search results from {}", queryId,
                    portletSearchResults.getSearchResult().size(), getSearchResultsSource(portletSearchResults));
        }
        
        final String windowId = portletSearchResults.getWindowId();
        final HttpServletRequest httpServletRequest = this.portalRequestUtils.getPortletHttpRequest(request);
        final IPortletWindowId portletWindowId = this.portletWindowRegistry.getPortletWindowId(httpServletRequest, windowId);
        
        //Add the other portlet's results to the main search results object
        this.addSearchResults(portletSearchResults, results, httpServletRequest, portletWindowId);
    }

    /**
     * Return the first search source (type string) in the result, else the string 'unknown'.
     * @param portletSearchResults Search results
     * @return String identifying the search result source if it was populated, else 'unknown'
     */
    private String getSearchResultsSource(SearchResults portletSearchResults) {
        // Return the first source in the result.
        List<SearchResult> portletResults = portletSearchResults.getSearchResult();
        String source = "unknown";
        if (portletResults.size() > 0
                && portletResults.get(0).getType() != null
                && !portletResults.get(0).getType().isEmpty()) {
            source = portletResults.get(0).getType().get(0);
        }
        return source;
    }

    /**
     * Display a search form
     */
    @RequestMapping
    public ModelAndView showSearchForm(RenderRequest request, RenderResponse response) {
        final Map<String,Object> model = new HashMap<String, Object>();

        // Determine if this portlet displays the search launch view or regular search view.
        PortletPreferences prefs = request.getPreferences();
        final boolean isMobile = isMobile(request);
        String viewName = isMobile ? "/jsp/Search/mobileSearch" : "/jsp/Search/search";

        // If this search portlet is configured to be the searchLauncher, calculate the URLs to the indicated
        // search portlet.
        final String searchLaunchFname = prefs.getValue(SEARCH_LAUNCH_FNAME, null);
        if (searchLaunchFname != null) {
            model.put("searchLaunchUrl", calculateSearchLaunchUrl(request, response));
            model.put("autocompleteUrl", calculateAutocompleteResourceUrl(request, response));
            viewName = "/jsp/Search/searchLauncher";
        }
        return new ModelAndView(viewName, model);
    }

    /**
     * Create an actionUrl for the indicated portlet
     * The resource URL is for the ajax typing search results response.
     * @param request render request
     * @param response render response
     */
    private String calculateSearchLaunchUrl(RenderRequest request, RenderResponse response) {
        final HttpServletRequest httpRequest = this.portalRequestUtils.getPortletHttpRequest(request);
        final IPortalUrlBuilder portalUrlBuilder = this.portalUrlProvider.getPortalUrlBuilderByPortletFName(httpRequest, "search", UrlType.ACTION);
        return portalUrlBuilder.getUrlString();

    }

    /**
     * Create a resourceUrl for <code>AJAX_RESPONSE_RESOURCE_ID</code>.
     * The resource URL is for the ajax typing search results response.
     * @param request render request
     * @param response render response
     */
    private String calculateAutocompleteResourceUrl(RenderRequest request, RenderResponse response) {
        final HttpServletRequest httpRequest = this.portalRequestUtils.getPortletHttpRequest(request);
        final IPortalUrlBuilder portalUrlBuilder = this.portalUrlProvider.getPortalUrlBuilderByPortletFName(httpRequest, "search", UrlType.RESOURCE);
        final IPortletUrlBuilder portletUrlBuilder = portalUrlBuilder.getPortletUrlBuilder(portalUrlBuilder.getTargetPortletWindowId());
        portletUrlBuilder.setResourceId(AJAX_RESPONSE_RESOURCE_ID);
        return portletUrlBuilder.getPortalUrlBuilder().getUrlString();

    }

    /**
     * Display search results
     */
    @RequestMapping(params = { "query", "queryId" })
    public ModelAndView showSearchResults(PortletRequest request,
            @RequestParam(value = "query") String query,
            @RequestParam(value = "queryId") String queryId
            ) {
        
        final Map<String,Object> model = new HashMap<String, Object>();
        model.put("query", query);

        ConcurrentMap<String, List<Tuple<SearchResult, String>>> results = new ConcurrentHashMap<>();
        final PortalSearchResults portalSearchResults = this.getPortalSearchResults(request, queryId);
        if (portalSearchResults != null) {
            results = portalSearchResults.getResults();
        }
        model.put("results", results);
        model.put("defaultTabKey", this.defaultTabKey);
        model.put("tabKeys", this.tabKeys);

        final boolean isMobile = isMobile(request);
        String viewName = isMobile ? "/jsp/Search/mobileSearch" : "/jsp/Search/search";
        
        return new ModelAndView(viewName, model);
    }
    
    /**
     * Display search results
     */
    @RequestMapping(params = { "query", "hitMaxQueries" })
    public ModelAndView showSearchError(PortletRequest request,
            @RequestParam(value = "query") String query,
            @RequestParam(value = "hitMaxQueries") boolean hitMaxQueries
            ) {
        
        final Map<String,Object> model = new HashMap<String, Object>();
        model.put("query", query);
        model.put("hitMaxQueries", hitMaxQueries);

        final boolean isMobile = isMobile(request);
        String viewName = isMobile ? "/jsp/Search/mobileSearch" : "/jsp/Search/search";
        
        return new ModelAndView(viewName, model);
    }

    /**
     * Display AJAX autocomplete search results for the last query
     */
    @ResourceMapping(value = "retrieveSearchJSONResults")
    public ModelAndView showJSONSearchResults(PortletRequest request) {

        PortletPreferences prefs = request.getPreferences();
        int maxTextLength = Integer.parseInt(prefs.getValue(AUTOCOMPLETE_MAX_TEXT_LENGTH_PREF_NAME, "180"));

        final Map<String,Object> model = new HashMap<String, Object>();
        List<AutocompleteResultsModel> results = new ArrayList<AutocompleteResultsModel>();

        final PortletSession session = request.getPortletSession();
        String queryId = (String) session.getAttribute(SEARCH_LAST_QUERY_ID);
        if (queryId != null) {
            final PortalSearchResults portalSearchResults = this.getPortalSearchResults(request, queryId);
            if (portalSearchResults != null) {
                final ConcurrentMap<String, List<Tuple<SearchResult, String>>> resultsMap = portalSearchResults.getResults();
                results = collateResultsForAutoCompleteResponse(resultsMap, maxTextLength);
            }
        }
        model.put("results", results);
        model.put("count", results.size());
        return new ModelAndView("json", model);
    }

    /**
     * Accepts a map (tab name, List of tuple search results) and returns a prioritized list of results for the ajax
     * autocomplete feature.  The computing impact of moving the list items around should be fairly small since there
     * are not too many search results (unless we get a lot of search providers, in which case the results could be
     * put into the appropriate format in the Event SEARCH_RESULTS_QNAME_STRING handling).
     *
     * Note that the method (as well as the SearchResults Event handler) do not impose a consistent ordering on results.
     * The results are ordered by priority, but within a particular priority the same search may have results ordered
     * differently based upon when the SearchResults Event handler receives the search results event list.  Also if
     * a search result is in multiple category types, even within the same priority, the search result will show up
     * multiple times.  Currently all results are in a single category so it is not worth adding extra complexity to
     * handle a situation that is not present.
     *
     * This method also cleans up and trims down the amount of data shipped so the feature is more responsive,
     * especially on mobile networks.
     * @param resultsMap
     * @return
     */
    private List<AutocompleteResultsModel> collateResultsForAutoCompleteResponse
            (ConcurrentMap<String, List<Tuple<SearchResult, String>>> resultsMap, int maxTextLength) {
        SortedMap<Integer, List<AutocompleteResultsModel>> prioritizedResultsMap =
                getCleanedAndSortedMapResults(resultsMap, maxTextLength);

        // Consolidate the results into a single, ordered list of max entries.
        List<AutocompleteResultsModel> results = new ArrayList<AutocompleteResultsModel>();
        for (List<AutocompleteResultsModel> items : prioritizedResultsMap.values()) {
            results.addAll(items);
            if (results.size() >= maxAutocompleteSearchResults) {
                break;
            }
        }
        return results.subList(0,
                results.size() > maxAutocompleteSearchResults ? maxAutocompleteSearchResults : results.size());
    }

    /**
     * Return the search results in a sorted map based on priority of the search result type
     * @param resultsMap Search results map
     * @return Sorted map of search results ordered on search result type priority
     */
    private SortedMap<Integer, List<AutocompleteResultsModel>> getCleanedAndSortedMapResults(
            ConcurrentMap<String, List<Tuple<SearchResult, String>>> resultsMap, int maxTextLength) {
        SortedMap<Integer, List<AutocompleteResultsModel>> prioritizedResultsMap = createAutocompletePriorityMap();

        // Put the results into the map of <priority,list>
        for (Map.Entry<String, List<Tuple<SearchResult, String>>> entry : resultsMap.entrySet()) {
            for (Tuple<SearchResult, String> tupleSearchResult : entry.getValue()) {
                SearchResult searchResult = tupleSearchResult.getFirst();
                List<String> resultTypes = searchResult.getType();
                // If the search result doesn't have a type defined, use the undefined result type.
                if (resultTypes.size() == 0) {
                    resultTypes = UNDEFINED_SEARCH_RESULT_TYPE;
                }
                for (String category : resultTypes) {
                    // Exclude the result if it is a result type that's in the ignore list.
                    if (!autocompleteIgnoreResultTypes.contains(category)) {
                        int priority = calculatePriorityFromCategory(category);
                        AutocompleteResultsModel result = new AutocompleteResultsModel(
                                cleanAndTrimString(searchResult.getTitle(), maxTextLength),
                                cleanAndTrimString(searchResult.getSummary(), maxTextLength),
                                tupleSearchResult.getSecond(),
                                category);
                        prioritizedResultsMap.get(priority).add(result);
                    }
                }
            }
        }
        return prioritizedResultsMap;
    }

    // Remove extraneous spaces, newlines, returns, tabs, etc. and limit the length.  This helps improve performance
    // for slower network connections and makes autocomplete UI results smaller/shorter.
    private String cleanAndTrimString(String text, int maxTextLength) {
        if (StringUtils.isNotBlank(text)) {
            String cleaned = text.trim().replaceAll("[\\s]+"," ");
            return cleaned.length() <= maxTextLength ? cleaned : cleaned.substring(0, maxTextLength) + " ...";
        }
        return text;
    }

    private SortedMap<Integer, List<AutocompleteResultsModel>> createAutocompletePriorityMap() {
        SortedMap<Integer, List<AutocompleteResultsModel>> resultsMap = new TreeMap<Integer, List<AutocompleteResultsModel>>();
        for (Map.Entry<String, Integer> entry : autocompleteResultTypeToPriorityMap.entrySet()) {
            if (!resultsMap.containsKey(entry.getValue())) {
                resultsMap.put(entry.getValue(), new ArrayList<AutocompleteResultsModel>());
            }
        }
        // Insure there is always a default entry of priority 0.
        resultsMap.put(0, new ArrayList<AutocompleteResultsModel>());
        return resultsMap;
    }

    private int calculatePriorityFromCategory(String category) {
        Integer priority = autocompleteResultTypeToPriorityMap.get(category);
        return  priority != null ? priority : 0;
    }


    /**
     * Get the {@link PortalSearchResults} for the specified query id from the session. If there are no results null
     * is returned. 
     */
    private PortalSearchResults getPortalSearchResults(PortletRequest request, String queryId) {
        final PortletSession session = request.getPortletSession();
        @SuppressWarnings("unchecked")
        final Cache<String, PortalSearchResults> searchResultsCache = (Cache<String, PortalSearchResults>)session.getAttribute(SEARCH_RESULTS_CACHE_NAME);
        if (searchResultsCache == null) {
            return null;
        }
        
        return searchResultsCache.getIfPresent(queryId);
    }

    
    /**
     * @param portletSearchResults Results from a portlet
     * @param results Results collating object
     * @param httpServletRequest current request
     * @param portletWindowId Id of the portlet window that provided the results
     */
    private void addSearchResults(SearchResults portletSearchResults, PortalSearchResults results,
            final HttpServletRequest httpServletRequest, final IPortletWindowId portletWindowId) {

        for (SearchResult result : portletSearchResults.getSearchResult()) {
            final String resultUrl = this.getResultUrl(httpServletRequest, result, portletWindowId);
            this.logger.debug("Created {} with from {}", resultUrl, result.getTitle());
            modifySearchResultLinkTitle(result, httpServletRequest, portletWindowId);
            results.addPortletSearchResults(resultUrl, result);
        }
    }

    /**
     * Since portlets don't have access to the portlet definition to create a useful search results link using
     * something like the portlet definition's title, post-process the link text and for those portlets whose
     * type is present in the substitution set, replace the title with the portlet definition's title.
     * @param result Search results object (may be modified)
     * @param httpServletRequest HttpServletRequest
     * @param portletWindowId Portlet Window ID
     */
    protected void modifySearchResultLinkTitle(SearchResult result, final HttpServletRequest httpServletRequest,
                                               final IPortletWindowId portletWindowId) {
        // If the title contains a SpEL expression, parse it with the portlet definition in the evaluation context.
        if (result.getType().size() > 0 && result.getTitle().contains("${")) {
            final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpServletRequest,
                    portletWindowId);
            final IPortletEntity portletEntity = portletWindow.getPortletEntity();
            final IPortletDefinition portletDefinition = portletEntity.getPortletDefinition();
            final SpELEnvironmentRoot spelEnvironment = new SpELEnvironmentRoot(portletDefinition);
            try {
                result.setTitle(spELService.getValue(result.getTitle(), spelEnvironment));
            } catch (SpelParseException | SpelEvaluationException e) {
                result.setTitle("(Invalid portlet title) - see details in log file");
                logger.error("Invalid Spring EL expression {} in search result portlet title", result.getTitle(), e);
            }
        }
    }
    
    /**
     * Determine the url for the search result 
     */
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
 
    public boolean isMobile(PortletRequest request) {
        String themeName = request.getProperty(ThemeNameRequestPropertiesManager.THEME_NAME_PROPERTY);
        return "UniversalityMobile".equals(themeName);
    }

    /**
     * Limited-use POJO representing the root of a SpEL environment.  For Search we're only using
     * the portlet object in the evaluation context.
     */
    @SuppressWarnings("unused")
    private class SpELEnvironmentRoot {

        private final IPortletDefinition portlet;

        /**
         * Create a new SpEL environment root for use in a SpEL evaluation context.
         *
         * @param portletDefinition  portlet definition
         */
        private SpELEnvironmentRoot(IPortletDefinition portletDefinition) {
            this.portlet = portletDefinition;
        }

        /**
         * Get the portlet associated with this environment root.
         */
        public IPortletDefinition getPortlet() {
            return portlet;
        }

    }
}
