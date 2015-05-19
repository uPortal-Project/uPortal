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
package org.jasig.portal.portlets.groupadmin;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * Represents a IPersonAttributesGroupTestGroupDefinition ad hoc group instance for UI editing.
 * 
 * @author Benito J. Gonzalez <bgonzalez@unicon.net>
 * @see     org.jasig.portal.groups.pags.dao.EntityPersonAttributesGroupStore
 * @see     org.jasig.portal.groups.pags.dao.IPersonAttributesGroupTestGroupDefinition
 * @since   4.3
 */
public final class PagsAdHocGroupForm implements Serializable {

    private static final long serialVersionUID = 1L;
    private String name;
    private String description;
    private Set<String> includes = new TreeSet<>();
    private Set<String> excludes = new TreeSet<>();

    public PagsAdHocGroupForm() {
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> getIncludes() {
        return Collections.unmodifiableSet(includes);
    }

    public void setIncludes(Set<String> includes) {
        this.includes.clear();
        this.includes.addAll(includes);
    }

    public void addIncludes(String include) {
        this.includes.add(include);
    }

    public Set<String> getExcludes() {
        return Collections.unmodifiableSet(excludes);
    }

    public void setExcludes(Set<String> excludes) {
        this.excludes.clear();
        this.excludes.addAll(excludes);
    }

    public void addExcludes(String exclude) {
        this.excludes.add(exclude);
    }
}
