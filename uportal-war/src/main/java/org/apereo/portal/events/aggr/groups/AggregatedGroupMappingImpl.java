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
/**
 * ' * Licensed to Jasig under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Jasig
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.events.aggr.groups;

import java.io.Serializable;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;

/**
 */
@Entity
@Table(name = "UP_AGGR_GROUP_MAPPING")
@SequenceGenerator(
    name = "UP_AGGR_GROUP_MAPPING_GEN",
    sequenceName = "UP_AGGR_GROUP_MAPPING_SEQ",
    allocationSize = 10
)
@TableGenerator(
    name = "UP_AGGR_GROUP_MAPPING_GEN",
    pkColumnValue = "UP_AGGR_GROUP_MAPPING_PROP",
    allocationSize = 10
)
@Immutable
@NaturalIdCache(
    region = "org.apereo.portal.events.aggr.groups.AggregatedGroupMappingImpl-NaturalId"
)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public final class AggregatedGroupMappingImpl implements AggregatedGroupMapping, Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "UP_AGGR_GROUP_MAPPING_GEN")
    @Column(name = "ID")
    private final long id;

    @NaturalId
    @Column(name = "GROUP_SERVICE", length = 200, nullable = false)
    private final String groupService;

    @NaturalId
    @Column(name = "GROUP_NAME", length = 200, nullable = false)
    private final String groupName;

    @Transient private int hashCode = 0;

    @SuppressWarnings("unused")
    private AggregatedGroupMappingImpl() {
        this.id = -1;
        this.groupService = null;
        this.groupName = null;
    }

    AggregatedGroupMappingImpl(String groupService, String groupName) {
        this.id = -1;
        this.groupService = groupService;
        this.groupName = groupName;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public String getGroupName() {
        return this.groupName;
    }

    @Override
    public String getGroupService() {
        return this.groupService;
    }

    @Override
    public int hashCode() {
        int h = this.hashCode;
        if (h == 0) {
            final int prime = 31;
            h = 1;
            h = prime * h + ((groupName == null) ? 0 : groupName.hashCode());
            h = prime * h + ((groupService == null) ? 0 : groupService.hashCode());
            this.hashCode = h;
        }
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        if (hashCode() != obj.hashCode()) return false;
        AggregatedGroupMappingImpl other = (AggregatedGroupMappingImpl) obj;
        if (groupName == null) {
            if (other.groupName != null) return false;
        } else if (!groupName.equals(other.groupName)) return false;
        if (groupService == null) {
            if (other.groupService != null) return false;
        } else if (!groupService.equals(other.groupService)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "AggregatedGroupMapping [id="
                + id
                + ", groupService="
                + groupService
                + ", groupName="
                + groupName
                + "]";
    }
}
