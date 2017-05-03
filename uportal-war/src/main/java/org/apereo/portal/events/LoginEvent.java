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
package org.apereo.portal.events;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.Validate;

/**
 * @since 2.6
 */
public final class LoginEvent extends PortalEvent {
    private static final long serialVersionUID = 1L;

    private final Set<String> groups;

    private final Map<String, List<String>> attributes;

    @SuppressWarnings("unused")
    private LoginEvent() {
        super();
        this.groups = Collections.emptySet();
        this.attributes = Collections.emptyMap();
    }

    LoginEvent(
            PortalEventBuilder eventBuilder,
            Set<String> groups,
            Map<String, List<String>> attributes) {
        super(eventBuilder);
        Validate.notNull(groups, "groups");
        Validate.notNull(attributes, "attributes");

        this.groups = ImmutableSet.copyOf(groups);

        final Builder<String, List<String>> attributesBuilder = ImmutableMap.builder();
        for (final Map.Entry<String, List<String>> attributeEntry : attributes.entrySet()) {
            attributesBuilder.put(
                    attributeEntry.getKey(), ImmutableList.copyOf(attributeEntry.getValue()));
        }
        this.attributes = attributesBuilder.build();
    }

    /** @return The groups the user was in at login */
    public Set<String> getGroups() {
        return this.groups;
    }

    /** @return The attributes the user had at login */
    public Map<String, List<String>> getAttributes() {
        return this.attributes;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return super.toString()
                + ", groups="
                + this.groups.size()
                + ", attributes="
                + this.attributes.size()
                + "]";
    }
}
