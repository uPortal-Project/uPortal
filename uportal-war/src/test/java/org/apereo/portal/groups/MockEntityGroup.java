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
package org.apereo.portal.groups;

import java.util.Set;
import javax.naming.Name;
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.IBasicEntity;

public class MockEntityGroup implements IEntityGroup {

    private final EntityIdentifier underlyingEntityIdentifier;

    public MockEntityGroup(String groupKey, Class<? extends IBasicEntity> entityType) {
        this.underlyingEntityIdentifier = new EntityIdentifier(groupKey, entityType);
    }

    @Override
    public Set<IEntityGroup> getAncestorGroups() throws GroupsException {
        return null;
    }

    @Override
    public Set<IEntityGroup> getParentGroups() throws GroupsException {
        return null;
    }

    @Override
    public String getKey() {
        return null;
    }

    @Override
    public Class<? extends IBasicEntity> getLeafType() {
        return null;
    }

    @Override
    public Class getType() {
        return null;
    }

    @Override
    public EntityIdentifier getUnderlyingEntityIdentifier() {
        return underlyingEntityIdentifier;
    }

    @Override
    public boolean isDeepMemberOf(IEntityGroup group) throws GroupsException {
        return false;
    }

    @Override
    public boolean isGroup() {
        return false;
    }

    @Override
    public boolean isMemberOf(IEntityGroup group) throws GroupsException {
        return false;
    }

    @Override
    public IEntityGroup asGroup() {
        return null;
    }

    @Override
    public boolean hasMembers() throws GroupsException {
        return false;
    }

    @Override
    public boolean contains(IGroupMember gm) throws GroupsException {
        return false;
    }

    @Override
    public boolean deepContains(IGroupMember gm) throws GroupsException {
        return false;
    }

    @Override
    public Set<IGroupMember> getChildren() throws GroupsException {
        return null;
    }

    @Override
    public Set<IGroupMember> getDescendants() throws GroupsException {
        return null;
    }

    @Override
    public void addChild(IGroupMember gm) throws GroupsException {}

    @Override
    public void delete() throws GroupsException {}

    @Override
    public String getCreatorID() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getLocalKey() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Name getServiceName() {
        return null;
    }

    @Override
    public boolean isEditable() throws GroupsException {
        return false;
    }

    @Override
    public void removeChild(IGroupMember gm) throws GroupsException {}

    @Override
    public void setCreatorID(String userID) {}

    @Override
    public void setDescription(String name) {}

    @Override
    public void setName(String name) throws GroupsException {}

    @Override
    public void update() throws GroupsException {}

    @Override
    public void updateMembers() throws GroupsException {}

    @Override
    public void setLocalGroupService(IIndividualGroupService groupService) throws GroupsException {}

    @Override
    public EntityIdentifier getEntityIdentifier() {
        return null;
    }
}
