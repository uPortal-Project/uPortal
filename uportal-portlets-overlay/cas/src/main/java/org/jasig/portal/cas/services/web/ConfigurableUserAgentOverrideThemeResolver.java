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
package org.jasig.portal.cas.services.web;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.web.support.ArgumentExtractor;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.theme.AbstractThemeResolver;

/**
 * ThemeResolver to determine the theme for CAS based on the service provided
 * and the current browser user agent. The theme resolver will extract the 
 * service parameter from the Request object and attempt to match the URL 
 * provided to a Service Id. If the service is found, the theme associated with 
 * it will be used. If not, these is associated with the service or the service 
 * was not found, a default theme will be used.
 * 
 * Once this service theme name is resolved, this implementation will look in 
 * its overrides map to determine if an alternate theme name is configured for
 * the current user agent.  This class is designed to allow us to define 
 * alternate themes for mobile devices on a per-service basis. 
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class ConfigurableUserAgentOverrideThemeResolver extends AbstractThemeResolver {

    /** The ServiceRegistry to look up the service. */
    private ServicesManager servicesManager;

    private List<ArgumentExtractor> argumentExtractors;

	private Map<String,Map<Pattern,String>> overrides;
	

	/*
	 * (non-Javadoc)
	 * @see org.springframework.web.servlet.ThemeResolver#resolveThemeName(javax.servlet.http.HttpServletRequest)
	 */
	public String resolveThemeName(HttpServletRequest request) {
		
		// get the theme name indicated by the service
		String themeName = resolveServiceThemeName(request);
		
		/* 
		 * If the overrides map contains overrides for this theme name, iterate
		 * through the mapped user agent regexes for this theme name.  If we 
		 * find a matching regex, set the theme name to the one mapped to that
		 * regex.
		 */
		
		if (overrides.containsKey(themeName)) {
			
			// retrieve the user agent string from the request
			String userAgent = request.getHeader("User-Agent");
			
			for (Entry<Pattern,String> entry : overrides.get(themeName).entrySet()) {
				if (entry.getKey().matcher(userAgent).matches()) {
					return entry.getValue();
				}
			}
		}
		
		// if no override was found for the current theme and user agent, 
		// return the default theme for this service
		return themeName;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.web.servlet.ThemeResolver#setThemeName(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String)
	 */
	public void setThemeName(HttpServletRequest request,
			HttpServletResponse response, String themeName) {
		// nothing to do here
	}

	
	/**
	 * Resolve the theme for the service.  This method's logic is taken from
	 * ServiceThemeResolver.
	 * 
	 * @param request
	 * @return			configured theme for this service
	 */
	protected String resolveServiceThemeName(HttpServletRequest request) {
        if (this.servicesManager == null) {
            return getDefaultThemeName();
        }

        final Service service = WebUtils.getService(this.argumentExtractors,
            request);

        final RegisteredService rService = this.servicesManager
            .findServiceBy(service);

        return service != null && rService != null && StringUtils.hasText(rService.getTheme()) ? rService
            .getTheme() : getDefaultThemeName();

	}
	
    public void setServicesManager(final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    public void setArgumentExtractors(
        final List<ArgumentExtractor> argumentExtractors) {
        this.argumentExtractors = argumentExtractors;
    }

    /**
     * Set the map of theme name overrides.  This map is of the format
     * { service theme name -> { user agent regular expression -> override theme name } }
     * 
     * @param overrides
     */
	public void setOverrides(Map<String,Map<String,String>> overrides) {
		// initialize the overrides variable to an empty map
		this.overrides = new HashMap<String,Map<Pattern,String>>();
		
		// convert the provided map's regular expressions to Pattern objects
		for (Entry<String,Map<String,String>> themeMapping : overrides.entrySet()) {
			Map<Pattern,String> mappings = new LinkedHashMap<Pattern,String>();
			for (Entry<String,String> browserMapping : themeMapping.getValue().entrySet()) {
				Pattern p = Pattern.compile(browserMapping.getKey());
				mappings.put(p, browserMapping.getValue());
			}
			this.overrides.put(themeMapping.getKey(), mappings);
		}
	}
}
