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
package org.apereo.portal.events.aggr.tabs;

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

@Entity
@Table(name = "UP_AGGR_TAB_MAPPING")
@SequenceGenerator(
    name = "UP_AGGR_TAB_MAPPING_GEN",
    sequenceName = "UP_AGGR_TAB_MAPPING_SEQ",
    allocationSize = 10
)
@TableGenerator(
    name = "UP_AGGR_TAB_MAPPING_GEN",
    pkColumnValue = "UP_AGGR_TAB_MAPPING_PROP",
    allocationSize = 10
)
@Immutable
@NaturalIdCache(region = "org.apereo.portal.events.aggr.tabs.AggregatedTabMappingImpl-NaturalId")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public final class AggregatedTabMappingImpl implements AggregatedTabMapping, Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "UP_AGGR_TAB_MAPPING_GEN")
    @Column(name = "ID")
    private final long id;

    @NaturalId
    @Column(name = "FRAGMENT_NAME", length = 200, nullable = false)
    private final String fragmentName;

    @NaturalId
    @Column(name = "TAB_NAME", length = 200, nullable = false)
    private final String tabName;

    @Transient private int hashCode = 0;

    @SuppressWarnings("unused")
    private AggregatedTabMappingImpl() {
        this.id = -1;
        this.fragmentName = null;
        this.tabName = null;
    }

    AggregatedTabMappingImpl(String fragmentName, String tabName) {
        this.id = -1;
        this.fragmentName = fragmentName;
        this.tabName = tabName;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getFragmentName() {
        return this.fragmentName;
    }

    @Override
    public String getTabName() {
        return this.tabName;
    }

    @Override
    public int hashCode() {
        int h = this.hashCode;
        if (h == 0) {
            final int prime = 31;
            h = 1;
            h = prime * h + ((fragmentName == null) ? 0 : fragmentName.hashCode());
            h = prime * h + ((tabName == null) ? 0 : tabName.hashCode());
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
        AggregatedTabMappingImpl other = (AggregatedTabMappingImpl) obj;
        if (fragmentName == null) {
            if (other.fragmentName != null) return false;
        } else if (!fragmentName.equals(other.fragmentName)) return false;
        if (tabName == null) {
            if (other.tabName != null) return false;
        } else if (!tabName.equals(other.tabName)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "AggregatedTabMappingImpl [id="
                + id
                + ", fragmentName="
                + fragmentName
                + ", tabName="
                + tabName
                + "]";
    }

    @Override
    public String getDisplayString() {
        return getTabName() + " (" + getFragmentName() + ")";
    }
}
