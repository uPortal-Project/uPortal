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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.jasig.portal.portlets.lookup.PersonLookupHelperImpl;
import org.jasig.portal.portlets.search.gsa.GsaResults;
import org.jasig.portal.portlets.search.gsa.GsaSearchService;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.services.persondir.IPersonAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.portlet.ModelAndView;

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

    protected static final String DIRECTORY_ENGINE = "directory";
    protected static final String CAMPUS_WEB_ENGINE = "campus-web";
    
    private GsaSearchService gsaSearchService;

    @Autowired
    public void setGsaSearchService(GsaSearchService gsaSearchService) {
        this.gsaSearchService = gsaSearchService;
    }
    
    private IPortalRequestUtils portalRequestUtils;
    
    @Autowired(required = true)
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }
    
    private IPersonManager personManager;
    
    @Autowired(required = true)
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    private PersonLookupHelperImpl lookupHelper;
    
    @Autowired(required = true)
    public void setPersonLookupHelper(PersonLookupHelperImpl lookupHelper) {
        this.lookupHelper = lookupHelper;
    }

    private Map<String, DirectoryAttributeType> displayAttributes;

    @Resource(name="directoryDisplayAttributes")
    public void setDirectoryDisplayAttributes(Map<String, DirectoryAttributeType> attributes) {
        this.displayAttributes = attributes;
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
            @RequestParam(value = "engine", required = false) String engine) {
        
        final Map<String,Object> model = new HashMap<String, Object>();

        final boolean isMobile = isMobile(request);
        String viewName = isMobile ? "/jsp/Search/mobileSearch" : "/jsp/Search/search";
        
        // determine which search types are enabled for this portlet configuration
        PortletPreferences prefs = request.getPreferences();

        // get the list of search enginges for this portlet configuration
        List<String> searchEngines = Arrays.asList(prefs.getValues("searchEngines", new String[]{"directory"}));
        model.put("searchEngines", searchEngines);
        
        // if only one search engine is configured, automatically set it as
        // selected
        int numEngines = searchEngines.size();
        if (numEngines == 1) {
            engine = searchEngines.get(0);
        }
        model.put("engineCount", numEngines);

        // if the selected search engine isn't actually enabled, unselect it
        if (engine != null && !searchEngines.contains(engine)) {
            engine = null;
        }
        model.put("engine", engine);

        // if no search has been supplied, simply show the search form
        if (StringUtils.isBlank(query)) {
            return new ModelAndView(viewName, model);
        }
        
        /*
         * If directory search is enabled, find people matching the search query.
         */
        if (DIRECTORY_ENGINE.equals(engine) || (!isMobile && searchEngines.contains(DIRECTORY_ENGINE))) {

            // TODO: allow configuration of search query displayAttributes
            final Map<String, Object> queryAttributes = new HashMap<String, Object>();
            queryAttributes.put("cn", query);

            final List<IPersonAttributes> people;

            // get an authorization principal for the current requesting user
            HttpServletRequest servletRequest = portalRequestUtils.getOriginalPortalRequest(request);
            IPerson currentUser = personManager.getPerson(servletRequest);

            // get the set of people matching the search query
            people = this.lookupHelper.searchForPeople(currentUser, queryAttributes);
            
            model.put("people", people);
            model.put("attributeNames", this.displayAttributes);
        }
        
        /*
         * If GSA search is enabled, get GSA results for the current query
         */
        if (CAMPUS_WEB_ENGINE.equals(engine) || (!isMobile && searchEngines.contains(CAMPUS_WEB_ENGINE))) {
            
            // get the GSA search configuration from the portlet preferences
            String baseUrl = prefs.getValue("gsaBaseUrl", null);
            String site = prefs.getValue("gsaSite", null);
            
            GsaResults gsaResults = gsaSearchService.search(query, baseUrl, site);
            model.put("gsaResults", gsaResults);
            model.put("gsaEnabled", true);
        }
                
        model.put("query", query);

        return new ModelAndView(viewName, model);
    }
 
    public boolean isMobile(PortletRequest request) {
        // TODO: use theme to select view
        String userAgent = request.getProperty("user-agent");
        if (userAgent.contains("iPhone") || userAgent.contains("Android")) {
            return true;
        } else {
            return false;
        }
    }
    
}
