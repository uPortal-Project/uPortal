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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IPermissionStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/permissionsList")
public class PermissionsListController extends AbstractPermissionsController {

    private IPermissionStore permissionStore;
    
    /*
     * Public API.
     */

    public static final String OWNER_PARAMETER = "owner";
    public static final String PRINCIPAL_PARAMETER = "principal";
    public static final String ACTIVITY_PARAMETER = "activity";
    public static final String TARGET_PARAMETER = "target";

    @Autowired(required=true)
    public void setPermissionStore(IPermissionStore permissionStore) {
        this.permissionStore = permissionStore;
    }

    /*
     * Protected API.
     */

    @Override
    protected ModelAndView invokeSensative(HttpServletRequest req, HttpServletResponse res) throws Exception {
        
        // The user is authorized to see this data;  now gather parameters
        String ownerParam = req.getParameter(OWNER_PARAMETER);
        String principalParam = req.getParameter(PRINCIPAL_PARAMETER);
        String activityParam = req.getParameter(ACTIVITY_PARAMETER);
        String targetParam = req.getParameter(TARGET_PARAMETER);

        IPermission[] rslt = permissionStore.select(ownerParam, principalParam, 
                            activityParam, targetParam, null);

        return new ModelAndView("jsonView", "permissionsList", marshall(rslt));

    }

    /*
     * Private Stuff.
     */
    
    private List<Map<String,String>> marshall(IPermission[] data) {
        
        // Assertions.
        if (data == null) {
            String msg = "Argument 'data' cannot be null";
            throw new IllegalArgumentException(msg);
        }
        
        List<Map<String,String>> rslt = new ArrayList<Map<String,String>>(data.length);
        for (IPermission p : data) {
            Map<String,String> entry = new HashMap<String,String>();
            entry.put("owner", p.getOwner());
            entry.put("principalType", "foo");
            entry.put("principalName", p.getPrincipal());
            entry.put("activity", p.getActivity());
            entry.put("target", p.getTarget());
            entry.put("permissionType", p.getType());
            rslt.add(entry);
        }
        
        return rslt;
        
    }

}
