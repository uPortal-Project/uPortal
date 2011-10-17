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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.apache.commons.lang.Validate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.Type;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 2.6
 */
@Entity
@Table(name = "UPE_LOGIN_EVENT")
@Inheritance(strategy=InheritanceType.JOINED)
@PrimaryKeyJoinColumn(name="EVENT_ID")
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
    
    @OneToMany(targetEntity = UserAttributeList.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "LOGIN_EVENT_ID", nullable = false)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Fetch(FetchMode.JOIN)
    private final Collection<UserAttributeList> attributes;

    
    @SuppressWarnings("unused")
    private LoginEvent() {
        super();
        this.groups = null;
        this.attributes = null;
    }

    LoginEvent(PortalEventBuilder eventBuilder, 
            Set<String> groups, Map<String, List<String>> attributes) {
        super(eventBuilder);
        Validate.notNull(groups, "groups");
        Validate.notNull(attributes, "attributes");
        
        this.groups = new LinkedHashSet<String>(groups);
        this.attributes = new ArrayList<UserAttributeList>(attributes.size());
        for (final Map.Entry<String, List<String>> attributeEntry : attributes.entrySet()) {
            this.attributes.add(new UserAttributeList(attributeEntry.getKey(), attributeEntry.getValue()));
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
        final Builder<String, List<String>> attributesBuilder = ImmutableMap.builder();
        for (final UserAttributeList userAttributeList : this.attributes) {
            attributesBuilder.put(userAttributeList.getName(), userAttributeList.getValues());
        }
        return attributesBuilder.build();
    }
    
    @Entity
    @Table(name = "UPE_LOGIN_USER_ATTR")
    @SequenceGenerator(
            name="UPE_LOGIN_USER_ATTR_GEN",
            sequenceName="UPE_LOGIN_USER_ATTR_SEQ",
            allocationSize=500
        )
    @TableGenerator(
            name="UPE_LOGIN_USER_ATTR_GEN",
            pkColumnValue="UPE_LOGIN_USER_ATTR",
            allocationSize=500
        )
    @Cacheable
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Immutable
    static class UserAttributeList {
        @Id
        @GeneratedValue(generator = "UPE_LOGIN_USER_ATTR_GEN")
        @SuppressWarnings("unused")
        private final long id;
        
        @Column(name = "ATTR_NAME", nullable = false)
        private String name;
        
        @ElementCollection(fetch = FetchType.EAGER)
        @JoinTable(
            name = "UPE_LOGIN_USER_ATTR_VALUES",
            joinColumns = @JoinColumn(name = "ATTR_ID")
        )
        @IndexColumn(name = "VALUE_ORDER")
        @Type(type = "nullSafeString")
        @Column(name = "ATTR_VALUE")
        @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
        @Fetch(FetchMode.JOIN)
        private final List<String> values = new ArrayList<String>(0);

        @SuppressWarnings("unused")
        private UserAttributeList() { 
            this.id = -1;
        }
        
        UserAttributeList(String name, List<String> values) {
            this.id = -1;
            this.name = name;
            this.setValues(values);
        }
        
        public List<String> getValues() {
            return values;
        }

        public void setValues(List<String> values) {
            this.values.clear();
            if (values != null) {
                this.values.addAll(values);
            }
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
            result = prime * result + ((this.values == null) ? 0 : this.values.hashCode());
            return result;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            UserAttributeList other = (UserAttributeList) obj;
            if (this.name == null) {
                if (other.name != null)
                    return false;
            }
            else if (!this.name.equals(other.name))
                return false;
            if (this.values == null) {
                if (other.values != null)
                    return false;
            }
            else if (!this.values.equals(other.values))
                return false;
            return true;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return this.name + "=" + this.values;
        }
    }
}
