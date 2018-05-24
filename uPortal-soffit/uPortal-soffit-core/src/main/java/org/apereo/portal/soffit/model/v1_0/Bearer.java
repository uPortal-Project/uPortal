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
package org.apereo.portal.soffit.model.v1_0;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Encapsulates username, user attributes, and group affiliations.
 *
 * @since 5.0
 */
public class Bearer extends AbstractTokenizable {

    private final String username;
    private final Map<String, List<String>> attributes;
    private final List<String> groups;

    public Bearer(
            String encryptedToken,
            String username,
            Map<String, List<String>> attributes,
            List<String> groups) {
        super(encryptedToken);
        this.username = username;
        this.attributes = Collections.unmodifiableMap(attributes);
        this.groups = Collections.unmodifiableList(groups);
    }

    /**
     * Supports proxying a missing data model element.
     *
     * @since 5.1
     */
    protected Bearer() {
        super(null);
        this.username = null;
        this.attributes = null;
        this.groups = null;
    }

    public String getUsername() {
        return username;
    }

    public Map<String, List<String>> getAttributes() {
        return attributes;
    }

    public List<String> getGroups() {
        return groups;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
        result = prime * result + ((groups == null) ? 0 : groups.hashCode());
        result = prime * result + ((username == null) ? 0 : username.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        Bearer other = (Bearer) obj;
        if (attributes == null) {
            if (other.attributes != null) return false;
        } else if (!attributes.equals(other.attributes)) return false;
        if (groups == null) {
            if (other.groups != null) return false;
        } else if (!groups.equals(other.groups)) return false;
        if (username == null) {
            if (other.username != null) return false;
        } else if (!username.equals(other.username)) return false;
        return true;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("username", username)
                .append("attributes", attributes)
                .append("groups", groups)
                .append("getEncryptedToken()", this.getEncryptedToken())
                .toString();
    }
}
