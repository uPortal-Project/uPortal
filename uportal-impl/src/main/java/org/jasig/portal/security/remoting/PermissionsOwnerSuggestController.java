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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
@RequestMapping("/permissionsOwnerSuggest")
public class PermissionsOwnerSuggestController extends AbstractPermissionsController {

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
    private Map<String,String> owners;
    
    /*
     * Public API.
     */

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

        // The user is authorized to see this data;  now gather parameters
        String text = req.getParameter(TEXT_PARAMETER);
        if (text != null && text.trim().length() > 0) {
            text = text.trim().toUpperCase();
            // Echo what the user typed (if anything) b/c the data model accepts arbitrary text
            Map<String,String> echo = new HashMap<String,String>();
            echo.put("name", text);
            echo.put("id", text);
            rslt.add(echo);
            for (Map.Entry<String,String> y : getOwnersMap().entrySet()) {
                if (y.getKey().contains(text)) {
                    Map<String,String> map = new HashMap<String,String>();
                    map.put("name", y.getValue());
                    map.put("id", y.getValue());
                    rslt.add(map);
                }
            }
            
        }

        return new ModelAndView("jsonView", "suggestions", rslt);

    }
    
    private Map<String,String> getOwnersMap() {
        
        if (owners == null) {
            // Gather owners in a set to remove duplicates...
            Set<String> set = new HashSet<String>();
            IPermission[] allPermissions = permissionStore.select(null, null, null, null, null);
            for (IPermission p : allPermissions) {
                set.add(p.getOwner());
            }
            
            // Switch to SortedMap for matching...
            Map<String,String> m = new TreeMap<String,String>();
            for (String s : set) {
                m.put(s.toUpperCase(), s);
            }
            owners = Collections.unmodifiableMap(m);
        }
        
        return owners;

    }

}
