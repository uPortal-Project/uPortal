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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private List<String> attributes;
    
    public SearchPortletController() {
        // TODO: allow configuration of person directory display attributes
        this.attributes = new ArrayList<String>();
        this.attributes.add("mail");
        this.attributes.add("homePhone");
        this.attributes.add("mobile");
        this.attributes.add("telephoneNumber");
        this.attributes.add("postalAddress");
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
        
        // if no search has been supplied, simply show the search form
        if (StringUtils.isBlank(query)) {
            return new ModelAndView("/jsp/Search/mobileSearch", model);
        }
        
        // determine which search types are enabled for this portlet configuration
        PortletPreferences prefs = request.getPreferences();
        boolean gsaEnabled = Boolean.valueOf(prefs.getValue("gsaEnabled", "false"));
        boolean directoryEnabled = Boolean.valueOf(prefs.getValue("directoryEnabled", "false"));
        boolean portletRegistryEnabled = Boolean.valueOf(prefs.getValue("portletRegistryEnabled", "false"));

        /*
         * If directory search is enabled, find people matching the search query.
         */
        if (directoryEnabled && (engine != null && "directory".equals(engine))) {

            // TODO: allow configuration of search query attributes
            final Map<String, Object> queryAttributes = new HashMap<String, Object>();
            queryAttributes.put("displayName", query);
            queryAttributes.put("given", query);
            queryAttributes.put("sn", query);
            queryAttributes.put("uid", query);

            final List<IPersonAttributes> people;

            // get an authorization principal for the current requesting user
            HttpServletRequest servletRequest = portalRequestUtils.getOriginalPortalRequest(request);
            IPerson currentUser = personManager.getPerson(servletRequest);

            // get the set of people matching the search query
            people = this.lookupHelper.searchForPeople(currentUser, queryAttributes);
            
            model.put("people", people);
            model.put("attributeNames", this.attributes);
            model.put("directoryEnabled", true);
        }
        
        /*
         * If GSA search is enabled, get GSA results for the current query
         */
        if (gsaEnabled && (engine != null && "campus-web".equals(engine))) {
            
            // get the GSA search configuration from the portlet preferences
            String baseUrl = prefs.getValue("gsaBaseUrl", null);
            String site = prefs.getValue("gsaSite", null);
            
            GsaResults gsaResults = gsaSearchService.search(query, baseUrl, site);
            model.put("gsaResults", gsaResults);
            model.put("gsaEnabled", true);
        }
        
        model.put("portletRegistryEnabled", portletRegistryEnabled);
        
        model.put("query", query);
        model.put("engine", engine);

        String viewName = getViewName(request);
        return new ModelAndView(viewName, model);
    }
 
    public String getViewName(PortletRequest request) {
        // TODO: use theme to select view
        String userAgent = request.getProperty("user-agent");
        if (userAgent.contains("iPhone") || userAgent.contains("Android")) {
            return "/jsp/Search/mobileSearch";
        } else {
            return "/jsp/Search/search";
        }
    }
    
}
