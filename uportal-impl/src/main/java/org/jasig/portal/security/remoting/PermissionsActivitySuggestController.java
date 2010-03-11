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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IPermissionStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/permissionsActivitySuggest")
public class PermissionsActivitySuggestController extends AbstractPermissionsController {

    private static final String ALL_OWNERS_MAP_KEY = PermissionsActivitySuggestController.class.getName() + ".ALL_OWNERS_MAP_KEY";
    
    private IPermissionStore permissionStore;
    
    /*
     * This Map is the central data structure of this controller.  We're 
     * lazy-initializing it in the firt request for 2 key reasons:
     *  - (1) Legacy permissions code uses the PortalApplicationContextLocator, 
     *        and therefore the Spring context can get caught in circular 
     *        dependencies and fail to load;  and
     *  - (2) In the (near) future, we expect to "refresh" this data 
     *        periodically in order to reflect data changes that may have 
     *        occurred since it was initialized.   
     */
    private Map<String,Map<String,String>> activitiesByOwner;
    
    /*
     * Public API.
     */

    public static final String OWNER_PARAMETER = "owner";
    public static final String TEXT_PARAMETER = "q";

    @Autowired(required=true)
    public void setPermissionStore(IPermissionStore permissionStore) {
        this.permissionStore = permissionStore;
    }

    /*
     * Protected API.
     */

    @Override
    protected ModelAndView invokeSensative(HttpServletRequest req, HttpServletResponse res) throws Exception {
        
        List<Map<String,String>> rslt = new ArrayList<Map<String,String>>();
        
        // Choose an entry in the activitiesByOwner Map based on owner 
        String ownerParam = req.getParameter(OWNER_PARAMETER);
        String ownerKey = ownerParam != null && ownerParam.trim().length() > 0 
                                ? ownerParam
                                : ALL_OWNERS_MAP_KEY;
        Map<String,String> activities = getActivitiesMap().get(ownerKey);
        
        // More work to do if we have a match at his point
        if (activities != null) {
            // Check which characters the user has entered
            String text = req.getParameter(TEXT_PARAMETER) != null
                                ? req.getParameter(TEXT_PARAMETER).toUpperCase().trim()
                                : null;
            // Echo what the user typed (if anything) b/c the data model accepts arbitrary text
            if (text != null) {
                Map<String,String> map = new HashMap<String,String>();
                map.put("name", text);
                map.put("id", text);
                rslt.add(map);
            }
            for (Map.Entry<String,String> y : activities.entrySet()) {
                if (text == null || y.getKey().contains(text)) {
                    Map<String,String> map = new HashMap<String,String>();
                    map.put("name", y.getValue());
                    map.put("id", y.getValue());
                    rslt.add(map);
                }
            }
        }

        // The user is authorized to see this data;  now gather parameters


        return new ModelAndView("jsonView", "suggestions", rslt);

    }
    
    private Map<String,Map<String,String>> getActivitiesMap() {
        
        if (activitiesByOwner == null) {
            // Initialize the activitiesByOwner Map
            Map<String,Map<String,String>> m = new HashMap<String,Map<String,String>>();
            Map<String,String> allOwnersMap = new TreeMap<String,String>();
            m.put(ALL_OWNERS_MAP_KEY, allOwnersMap);
            IPermission[] allPermissions = permissionStore.select(null, null, null, null, null);
            for (IPermission p : allPermissions) {
                Map<String,String> activities = m.get(p.getOwner());
                if (activities == null) {
                    // We need a Map sorted by keys
                    activities = new TreeMap<String,String>();
                    m.put(p.getOwner(), activities);
                }
                String activityKey = p.getActivity().toUpperCase();
                // Might already be there, but does no harm
                activities.put(activityKey, p.getActivity());
                allOwnersMap.put(activityKey, p.getActivity());
            }
                
            // Make all Maps unmodifiable for safe concurrency
            for (Map.Entry<String,Map<String,String>> y : m.entrySet()) {
                Map<String,String> activities = Collections.unmodifiableMap(y.getValue());
                y.setValue(activities);
            }
            activitiesByOwner = Collections.unmodifiableMap(m);
        }

        return activitiesByOwner;

    }

}
