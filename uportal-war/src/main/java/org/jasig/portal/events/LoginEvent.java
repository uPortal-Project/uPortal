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
package org.jasig.portal.events;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import org.jasig.portal.security.IPerson;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
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

    LoginEvent(PortalEventBuilder eventBuilder, 
            Set<String> groups, Map<String, List<String>> attributes) {
        super(eventBuilder);
        Validate.notNull(groups, "groups");
        Validate.notNull(attributes, "attributes");
        
        this.groups = ImmutableSet.copyOf(groups);
        
        final Builder<String, List<String>> attributesBuilder = ImmutableMap.builder();
        for (final Map.Entry<String, List<String>> attributeEntry : attributes.entrySet()) {
            attributesBuilder.put(attributeEntry.getKey(), ImmutableList.copyOf(attributeEntry.getValue()));
        }
        this.attributes = attributesBuilder.build();
    }

    /**
     * Instantiate a new LoginEvent from its raw elements.
     *
     * @param groups non-null Set of String group names of which the user who logged in is member
     * @param userAttributes non-null user attributes of the user who logged in
     * @param source non-null source of the LoginEvent
     * @param serverName non-null name of the server logged in to
     * @param eventSessionId on-null id of the session the user logged in to
     * @param person non-null person who has logged in
     * @param portalRequest non-null request in which this login has happened
     *
     * @since uPortal 4.2
     */
    public LoginEvent (

        // state specific to LoginEvent
        final Set<String> groups, final Map<String, List<String>> userAttributes,

        // state general to PortalEvent
        final String serverName, final String eventSessionId, final IPerson person,
        final HttpServletRequest portalRequest,

        // state general to ApplicationEvent
        final Object source
        ) {

        // packaging into PortalEventBuilder required by the superclass API
        super(new PortalEventBuilder(source, serverName, eventSessionId, person, portalRequest));

        this.groups = groups;
        this.attributes = userAttributes;

    }
    
    /**
     * @return The groups the user was in at login
     */
    public Set<String> getGroups() {
        return this.groups;
    }

    /**
     * @return The attributes the user had at login
     */
    public Map<String, List<String>> getAttributes() {
        return this.attributes;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return super.toString() + 
                ", groups=" + this.groups.size() + 
                ", attributes=" + this.attributes.size() + "]";
    }
}
