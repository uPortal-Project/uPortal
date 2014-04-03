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

package org.jasig.portal.groups.pags.dao;

import java.util.Set;

import org.dom4j.Element;
import org.jasig.portal.IBasicEntity;

/**
 * @author Shawn Connolly, sconnolly@unicon.net
 */
public interface IPersonAttributesGroupDefinition extends IBasicEntity {

    long getId();
    String getName();
    void setName(String groupName);
    String getDescription();
    void setDescription(String groupDescription);

    Set<IPersonAttributesGroupDefinition> getMembers();
    void setMembers(Set<IPersonAttributesGroupDefinition> members);
    Set<IPersonAttributesGroupDefinition> getParents();
    public void setParents(Set<IPersonAttributesGroupDefinition> parents);
    Set<IPersonAttributesGroupTestGroupDefinition> getTestGroups();
    void setTestGroups(Set<IPersonAttributesGroupTestGroupDefinition> testGroups);

    // Used for Exporting
    void toElement(Element parent);

}
