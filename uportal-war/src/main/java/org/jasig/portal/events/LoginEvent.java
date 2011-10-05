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

package org.jasig.portal.events;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.MapKeyColumn;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Immutable;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 2.6
 */
@Entity
@Table(name = "UPE_LOGIN_EVENT")
@Inheritance(strategy=InheritanceType.JOINED)
@PrimaryKeyJoinColumn(name="EVENT_ID")
@Immutable
public final class LoginEvent extends PortalEvent {
    private static final long serialVersionUID = 1L;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(
        name = "UPE_LOGIN_GROUPS",
        joinColumns = @JoinColumn(name = "ATTR_ID")
    )
    @Column(name = "GROUP_KEY")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Fetch(FetchMode.JOIN)
    private final Set<String> groups;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name="ATTR_NAME", nullable=false, length = 500)
    @CollectionTable(
            name="UPE_LOGIN_USER_ATTR_MAP", 
            joinColumns = @JoinColumn(name = "EVENT_ID", nullable = false))
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Fetch(FetchMode.JOIN)
    private final Map<String, UserAttributeList> attributes;
    
    @SuppressWarnings("unused")
    private LoginEvent() {
        super();
        this.groups = null;
        this.attributes = null;
    }

    LoginEvent(PortalEventBuilder eventBuilder, 
            Set<String> groups, Map<String, List<String>> attributes) {
        super(eventBuilder);
        this.groups = new LinkedHashSet<String>(groups);
        this.attributes = new LinkedHashMap<String, UserAttributeList>();
        for (final Map.Entry<String, List<String>> attributeEntry : attributes.entrySet()) {
            this.attributes.put(attributeEntry.getKey(), new UserAttributeList(attributeEntry.getValue()));
        }
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
        return Collections.unmodifiableMap(
                Maps.transformValues(this.attributes, new Function<UserAttributeList, List<String>>() {
                    /* (non-Javadoc)
                     * @see com.google.common.base.Function#apply(java.lang.Object)
                     */
                    @Override
                    public List<String> apply(UserAttributeList input) {
                        return input.getValues();
                    }
                })
            );
    }
}
