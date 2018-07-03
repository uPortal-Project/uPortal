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
package org.apereo.portal.tenants;

import java.util.Collections;
import java.util.HashMap;
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
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;

/** JPA-backed implementation of {@link ITenant}. */
@Entity
@Table(name = "UP_TENANT")
@SequenceGenerator(name = "UP_TENANT_GEN", sequenceName = "UP_TENANT_SEQ", allocationSize = 1)
@TableGenerator(name = "UP_TENANT_GEN", pkColumnValue = "UP_TENANT", allocationSize = 1)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
/* package-private */ class JpaTenant implements ITenant {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "UP_TENANT_GEN")
    @Column(name = "TENANT_ID")
    private long id;

    @Column(name = "TENANT_NAME", unique = true, nullable = false)
    private String name;

    @Column(name = "TENANT_FNAME", unique = true, nullable = false, length = 128)
    @Type(type = "fname")
    private String fname;

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "ATTR_NAME", nullable = false, length = 500)
    @Column(name = "ATTR_VALUE", nullable = false, length = 2000)
    @Type(type = "nullSafeString") // only applies to map values
    @CollectionTable(
            name = "UP_TENANT_ATTRIBUTES",
            joinColumns = @JoinColumn(name = "TENANT_ID", nullable = false))
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Fetch(FetchMode.JOIN)
    private Map<String, String> attributes = new HashMap<String, String>(0);

    public JpaTenant() {
        this.id = -1;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getFname() {
        return fname;
    }

    @Override
    public void setFname(String fname) {
        this.fname = fname;
    }

    @Override
    public String getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public void setAttribute(String name, String value) {
        attributes.put(name, value);
    }

    @Override
    public Map<String, String> getAttributesMap() {
        return Collections.unmodifiableMap(new HashMap<String, String>(attributes));
    }

    @Override
    public String toString() {
        return "JpaTenant [id="
                + id
                + ", name="
                + name
                + ", fname="
                + fname
                + ", attributes="
                + attributes
                + "]";
    }

    /** The default order for tenants is alphabetically by name. */
    @Override
    public int compareTo(ITenant t) {
        return this.name.compareTo(t.getName());
    }
}
