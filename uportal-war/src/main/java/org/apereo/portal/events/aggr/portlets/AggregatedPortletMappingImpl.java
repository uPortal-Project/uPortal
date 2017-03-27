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
package org.apereo.portal.events.aggr.portlets;

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
import org.hibernate.annotations.Type;

@Entity
@Table(name = "UP_AGGR_PORTLET_MAPPING")
@SequenceGenerator(
    name = "UP_AGGR_PORTLET_MAPPING_GEN",
    sequenceName = "UP_AGGR_PORTLET_MAPPING_SEQ",
    allocationSize = 10
)
@TableGenerator(
    name = "UP_AGGR_PORTLET_MAPPING_GEN",
    pkColumnValue = "UP_AGGR_PORTLET_MAPPING_PROP",
    allocationSize = 10
)
@Immutable
@NaturalIdCache(
    region = "org.apereo.portal.events.aggr.portlets.AggregatedPortletMappingImpl-NaturalId"
)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public final class AggregatedPortletMappingImpl implements AggregatedPortletMapping, Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "UP_AGGR_PORTLET_MAPPING_GEN")
    @Column(name = "ID")
    private final long id;

    @Column(name = "PORTLET_NAME", length = 128, nullable = false)
    private final String name;

    @NaturalId
    @Column(name = "PORTLET_FNAME", length = 255, nullable = false)
    @Type(type = "fname")
    private final String fname;

    @Transient private int hashCode = 0;

    @SuppressWarnings("unused")
    private AggregatedPortletMappingImpl() {
        this.id = -1;
        this.name = null;
        this.fname = null;
    }

    AggregatedPortletMappingImpl(String name, String fname) {
        this.id = -1;
        this.name = name;
        this.fname = fname;
    }

    @Override
    public String getFname() {
        return this.fname;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int hashCode() {
        int h = this.hashCode;
        if (h == 0) {
            final int prime = 31;
            h = 1;
            h = prime * h + ((fname == null) ? 0 : fname.hashCode());
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
        AggregatedPortletMappingImpl other = (AggregatedPortletMappingImpl) obj;
        if (fname == null) {
            if (other.fname != null) return false;
        } else if (!fname.equals(other.fname)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "AggregatedPortletMappingImpl [id="
                + id
                + ", name="
                + name
                + ", fname="
                + fname
                + "]";
    }
}
