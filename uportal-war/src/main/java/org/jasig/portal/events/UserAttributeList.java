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
import java.util.Collections;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "UPE_SESSION_USER_ATTR")
@SequenceGenerator(
        name="UPE_SESSION_USER_ATTR_GEN",
        sequenceName="UPE_SESSION_USER_ATTR_SEQ",
        allocationSize=500
    )
@TableGenerator(
        name="UPE_SESSION_USER_ATTR_GEN",
        pkColumnValue="UPE_SESSION_USER_ATTR",
        allocationSize=500
    )
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Immutable
class UserAttributeList {
    @Id
    @GeneratedValue(generator = "UPE_SESSION_USER_ATTR_GEN")
    private final long id;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(
        name = "UPE_SESSION_USER_ATTR_VAL",
        joinColumns = @JoinColumn(name = "ATTR_ID")
    )
    @IndexColumn(name = "VALUE_ORDER")
    @Type(type = "nullSafeString")
    @Column(name = "ATTR_VALUE")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Fetch(FetchMode.JOIN)
    private final List<String> values;

    @SuppressWarnings("unused")
    private UserAttributeList() {
        this.id = -1;
        this.values = null;
    }
    
    UserAttributeList(List<String> values) {
        this.id = -1;
        this.values = new ArrayList<String>(values);
    }

    public long getId() {
        return this.id;
    }

    public List<String> getValues() {
        return Collections.unmodifiableList(this.values);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (this.id ^ (this.id >>> 32));
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
        if (this.id != other.id)
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.values.toString();
    }
}