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

import java.util.List;
import org.jasig.portal.api.Principal;

public interface Entity {
    public static final String ENTITY_CATEGORY = "category";
    public static final String ENTITY_CHANNEL = "channel";
    public static final String ENTITY_GROUP = "group";
    public static final String ENTITY_PERSON = "person";

    public void setEntityType(String entityType);
    public String getEntityType();
    public void setId(String id);
    public String getId();
    public void setName(String name);
    public String getName();
    public void setCreatorId(String creatorId);
    public String getCreatorId();
    public void setDescription(String description);
    public String getDescription();
    public void setPrincipal(Principal principal);
    public Principal getPrincipal();
    public void setChildren(List<Entity> children);
    public List<Entity> getChildren();
    public void addChild(Entity entity);
    public void setChildrenInitialized(boolean childrenInitialized);
    public boolean isChildrenInitialized();
}
