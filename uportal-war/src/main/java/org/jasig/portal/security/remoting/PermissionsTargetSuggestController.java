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

package org.jasig.portal.security.remoting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.jasig.portal.permission.target.IPermissionTarget;
import org.jasig.portal.permission.target.IPermissionTargetProvider;
import org.jasig.portal.permission.target.IPermissionTargetProviderRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * PermissionTargetSuggestController provides a JSON view of IPermissionTargets
 * matching a supplied search string that is suitable for use with the 
 * jquery.tokeninput.js plugin.
 * 
 * @author Drew Wills
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 * @since 3.3
 */
@Controller
@RequestMapping("//permissionsTargetSuggest")
public class PermissionsTargetSuggestController extends AbstractPermissionsController {
    
    private IPermissionTargetProviderRegistry targetProviderRegistry;
    
    @Autowired(required = true)
    public void setPermissionTargetProviderRegistry(IPermissionTargetProviderRegistry registry) {
        this.targetProviderRegistry = registry;
    }

    /*
     * Public API.
     */

    public static final String TEXT_PARAMETER = "q";

    /*
     * Protected API.
     */

    @Override
    protected ModelAndView invokeSensitive(HttpServletRequest req, HttpServletResponse res) throws Exception {

        // initialize our JSON result object
        List<Map<String,String>> rslt = new ArrayList<Map<String,String>>();

        // if the search text parameter is non-blank, attempt to find matching
        // permission targets
        String text = req.getParameter(TEXT_PARAMETER);
        if (!StringUtils.isBlank(text)) {
            
            /*
             * First build up a sorted set of matching permission targets.  To
             * accomplish this we will need to get matching targets from
             * each registered target provider.
             */
            
            SortedSet<IPermissionTarget> matchingTargets = new TreeSet<IPermissionTarget>();
            for (IPermissionTargetProvider provider : targetProviderRegistry.getTargetProviders()) {
                // add matching results for this target provider to the set
                Collection<IPermissionTarget> targets = provider.searchTargets(text);
                matchingTargets.addAll(targets);
            }

            /*
             * Add the sorted permission targets to our result list
             */
            
            for (IPermissionTarget target : matchingTargets) {
                Map<String,String> map = new HashMap<String,String>();
                map.put("id", target.getKey());
                map.put("name", target.getName());
                rslt.add(map);
            }
            
        }

        return new ModelAndView("jsonView", "suggestions", rslt);

    }
    
}
