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
 * Encapsulates user input for creating or updating an "Ad Hoc" group.  These
 * are PAGS groups that leverage the {@link AdHocGroupTester}.  There is a
 * specialized UI for managing them.
 * 
 * @author Benito J. Gonzalez <bgonzalez@unicon.net>
 * @see     org.jasig.portal.groups.pags.dao.EntityPersonAttributesGroupStore
 * @see     org.jasig.portal.groups.pags.dao.IPersonAttributesGroupTestGroupDefinition
 * @since   4.3
 */
public final class AdHocPagsForm implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Object DESCRIPTION_PREAMBLE = "Users who";
    private static final Object DESCRIPTION_ARE = "are members of";
    private static final Object DESCRIPTION_BUT = "but";
    private static final Object DESCRIPTION_ARE_NOT = "are not members of";

    private String name = null;  // May only be set once, for a new group
    private Set<String> includes = Collections.emptySet();
    private Set<String> excludes = Collections.emptySet();

    public String getName() {
        return this.name;
    }

    /**
     * Setting the name is only allowed if the current name is null, which
     * indicates the form is for a new group. 
     * 
     * @param name
     */
    public void setName(String name) {
        if (name != null) {
            String msg = "Setting the group name only allowed for new Ad Hoc "
                                        + "groups;  current name=" + name;
            throw new IllegalStateException(msg);
        }
        this.name = name;
    }

    public String getDescription() {
        /*
         * For groups created with this UI (Ad Hoc groups), the description
         * field is generated.  It will be updated each time the group is
         * edited.
         */
        StringBuilder rslt = new StringBuilder();
        rslt.append(DESCRIPTION_PREAMBLE);

        // Includes
        if (includes.size() != 0) {
            rslt.append(" ").append(DESCRIPTION_ARE).append(" (");
            for (String incl : includes) {
                rslt.append(incl).append(", ");
            }
            rslt.setLength(rslt.length() - 2);  // Trim the last ", " from looping
            rslt.append(")");
        }

        // Where we have both Includes & Excludes
        if (includes.size() != 0 && excludes.size() != 0) {
            rslt.append(" ").append(DESCRIPTION_BUT);
        }

        // Excludes
        if (excludes.size() != 0) {
            rslt.append(" ").append(DESCRIPTION_ARE_NOT).append(" (");
            for (String excl : excludes) {
                rslt.append(excl).append(", ");
            }
            rslt.setLength(rslt.length() - 2);  // Trim the last ", " from looping
            rslt.append(")");
        }

        return rslt.toString();

    }

    public Set<String> getIncludes() {
        return Collections.unmodifiableSet(includes);
    }

    public void setIncludes(Set<String> includes) {
        this.includes  = new TreeSet<>(includes);
    }

    public void addIncludes(String include) {
        this.includes.add(include);
    }

    public Set<String> getExcludes() {
        return Collections.unmodifiableSet(excludes);
    }

    public void setExcludes(Set<String> excludes) {
        this.excludes  = new TreeSet<>(excludes);
    }

    public void addExcludes(String exclude) {
        this.excludes.add(exclude);
    }

}
