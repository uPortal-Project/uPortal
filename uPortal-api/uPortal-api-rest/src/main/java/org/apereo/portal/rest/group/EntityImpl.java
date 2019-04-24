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

import java.util.ArrayList;
import java.util.List;

public class EntityImpl implements Entity {
    private String entityType;
    private String id;
    private String name;
    private String creatorId;
    private String description;
    private Principal principal;
    private List<Entity> children = new ArrayList<>();
    private boolean childrenInitialized;

    @Override
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    @Override
    public String getEntityType() {
        return entityType;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    @Override
    public String getCreatorId() {
        return creatorId;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setPrincipal(Principal principal) {
        this.principal = principal;
    }

    @Override
    public Principal getPrincipal() {
        return principal;
    }

    @Override
    public void setChildren(List<Entity> children) {
        this.children = children;
    }

    @Override
    public void addChild(Entity child) {
        children.add(child);
    }

    @Override
    public List<Entity> getChildren() {
        return children;
    }

    @Override
    public void setChildrenInitialized(boolean childrenInitialized) {
        this.childrenInitialized = childrenInitialized;
    }

    @Override
    public boolean isChildrenInitialized() {
        return childrenInitialized;
    }
}
