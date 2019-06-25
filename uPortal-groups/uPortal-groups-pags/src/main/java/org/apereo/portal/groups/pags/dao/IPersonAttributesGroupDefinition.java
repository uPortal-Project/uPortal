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
package org.apereo.portal.groups.pags.dao;

import java.util.Set;
import org.apereo.portal.EntityIdentifier;
import org.dom4j.Element;

/**
 * Describes the definition of a group. PAGS group definitions are used (at runtime) to create
 * groups in the GaP (Groups & Permissions) subsystem. An object of this type, therefore, is not
 * itself a group; it's a blueprint for an {@link IEntityGroup}.
 */
public interface IPersonAttributesGroupDefinition {

    long getId();

    /**
     * Provides the {@link EntityIdentifier} that represents the {@link IEntityGroup} this
     * definition produces in GaP. This identifier will be available -- and identical -- whether the
     * group has been created already or not.
     */
    EntityIdentifier getCompositeEntityIdentifierForGroup();

    String getName();

    void setName(String groupName);

    String getDescription();

    void setDescription(String groupDescription);

    Set<IPersonAttributesGroupDefinition> getMembers();

    Set<IPersonAttributesGroupDefinition> getDeepMembers();

    Set<IPersonAttributesGroupDefinition> getDeepMembers(
            Set<IPersonAttributesGroupDefinition> processing,
            Set<IPersonAttributesGroupDefinition> seen);

    void setMembers(Set<IPersonAttributesGroupDefinition> members);

    Set<IPersonAttributesGroupTestGroupDefinition> getTestGroups();

    void setTestGroups(Set<IPersonAttributesGroupTestGroupDefinition> testGroups);

    /** Supports exporting. */
    void toElement(Element parent);
}
