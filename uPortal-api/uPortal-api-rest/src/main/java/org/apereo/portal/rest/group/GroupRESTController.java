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
package org.apereo.portal.rest.group;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.apereo.portal.groups.IEntityGroup;
import org.apereo.portal.groups.IGroupMember;
import org.apereo.portal.portlets.groupselector.EntityEnum;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.services.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class GroupRESTController {

    private IPersonManager personManager;

    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    @RequestMapping(value = "/groups", method = RequestMethod.GET)
    public ModelAndView getUsersGroup(HttpServletRequest request) {
        IPerson person = personManager.getPerson(request);
        Set<Entity> groups = Collections.emptySet();
        if (person != null) {
            String username = person.getUserName();
            if (username != null) {
                final IGroupMember member = GroupService.getGroupMember(username, IPerson.class);
                final Set<IEntityGroup> parents = member.getParentGroups();
                groups =
                        parents.stream()
                                .map(group -> EntityFactory.createEntity(group, EntityEnum.GROUP))
                                .collect(Collectors.toSet());
            }
        }

        return new ModelAndView("json", "groups", groups);
    }
}
