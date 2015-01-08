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
package org.jasig.portal.api.groups;

import java.util.Set;

public interface GroupsService {
    public Entity getRootGroup();
    public Entity getGroup(String groupId,boolean populateChildren);
    public Entity findGroup(String groupName,boolean populateChildren);
    public Set<Entity> findGroups(String searchTerm);
    public Set<Entity> getGroupsForMember(String memberId);
    public Entity findMember(String memberName,boolean populateChildren);
    public Set<Entity> findMembers(String searchTerm);
    public Set<Entity> getMembersForGroup(String groupName);
}
