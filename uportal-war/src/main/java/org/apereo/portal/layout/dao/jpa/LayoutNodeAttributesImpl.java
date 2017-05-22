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
package org.apereo.portal.layout.dao.jpa;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.persistence.Cacheable;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Version;
import org.apache.commons.lang.Validate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "UP_SS_USER_PREF_LAY_ATTR")
@SequenceGenerator(
    name = "UP_SS_USER_PREF_LAY_ATTR_GEN",
    sequenceName = "UP_SS_USER_PREF_LAY_ATTR_SEQ",
    allocationSize = 5
)
@TableGenerator(
    name = "UP_SS_USER_PREF_LAY_ATTR_GEN",
    pkColumnValue = "UP_SS_USER_PREF_LAY_ATTR",
    allocationSize = 5
)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
class LayoutNodeAttributesImpl {
    @Id
    @GeneratedValue(generator = "UP_SS_USER_PREF_LAY_ATTR_GEN")
    @Column(name = "UP_SS_USER_PREF_LAY_ATTR_ID")
    private final long id;

    @Version
    @Column(name = "ENTITY_VERSION")
    private final long entityVersion;

    @Column(name = "NODE_ID", nullable = false, length = 200)
    private final String nodeId;

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "ATTR_NAME", nullable = false, length = 500)
    @Column(name = "ATTR_VALUE", nullable = false, length = 2000)
    @Type(type = "nullSafeString") //only applies to map values
    @CollectionTable(
        name = "UP_SS_USER_PREF_LAY_ATTR_VAL",
        joinColumns = @JoinColumn(name = "UP_SS_USER_PREF_LAY_ATTR_ID", nullable = false)
    )
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Fetch(FetchMode.JOIN)
    private Map<String, String> attributes = new LinkedHashMap<String, String>(0);

    @SuppressWarnings("unused")
    private LayoutNodeAttributesImpl() {
        this.id = -1;
        this.entityVersion = -1;
        this.nodeId = null;
    }

    public LayoutNodeAttributesImpl(String nodeId) {
        Validate.notEmpty(nodeId, "nodeId cannot be null");
        this.id = -1;
        this.entityVersion = -1;
        this.nodeId = nodeId;
    }

    public long getId() {
        return this.id;
    }

    public String getNodeId() {
        return this.nodeId;
    }

    public Map<String, String> getAttributes() {
        return this.attributes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.nodeId == null) ? 0 : this.nodeId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        LayoutNodeAttributesImpl other = (LayoutNodeAttributesImpl) obj;
        if (this.nodeId == null) {
            if (other.nodeId != null) return false;
        } else if (!this.nodeId.equals(other.nodeId)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "LayoutNodeAttributesImpl [id="
                + this.id
                + ", entityVersion="
                + this.entityVersion
                + ", nodeId="
                + this.nodeId
                + "]";
    }
}
