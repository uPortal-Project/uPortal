/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.rest;

import java.util.Collections;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.apereo.portal.api.groups.ApiGroupsService;
import org.apereo.portal.api.groups.Entity;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class GroupRESTController {

    private ApiGroupsService groupService;

    private IPersonManager personManager;

    @Autowired(required = true)
    public void setGroupService(ApiGroupsService groupService) {
        this.groupService = groupService;
    }

    @Autowired(required = true)
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    @RequestMapping(value = "/groups", method = RequestMethod.GET)
    public ModelAndView getUsersGroup(HttpServletRequest request) {
        IPerson person = personManager.getPerson(request);
        Set<Entity> groups = Collections.EMPTY_SET;
        if (person != null) {
            String username = person.getUserName();
            if (username != null) {
                groups = groupService.getGroupsForMember(username);
            }
        }

        return new ModelAndView("json", "groups", groups);
    }
}
