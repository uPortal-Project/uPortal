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
package org.apereo.portal.persondir.dao.jpa;

import java.io.Serializable;
import java.util.ArrayList;
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
import javax.persistence.Version;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "UP_PERSON_ATTR")
@SequenceGenerator(
    name = "UP_PERSON_ATTR_GEN",
    sequenceName = "UP_PERSON_ATTR_SEQ",
    allocationSize = 10
)
@TableGenerator(name = "UP_PERSON_ATTR_GEN", pkColumnValue = "UP_PERSON_ATTR", allocationSize = 10)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
class LocalAccountPersonAttributeImpl implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "UP_PERSON_ATTR_GEN")
    private final int id;

    @Version
    @Column(name = "ENTITY_VERSION")
    private final long entityVersion;

    @Column(name = "ATTR_NAME", nullable = false)
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(name = "UP_PERSON_ATTR_VALUES", joinColumns = @JoinColumn(name = "ATTR_ID"))
    @IndexColumn(name = "VALUE_ORDER")
    @Type(type = "nullSafeString")
    @Column(name = "ATTR_VALUE")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Fetch(FetchMode.JOIN)
    private final List<String> values = new ArrayList<String>(0);

    @SuppressWarnings("unused")
    private LocalAccountPersonAttributeImpl() {
        this.id = -1;
        this.entityVersion = -1;
    }

    public LocalAccountPersonAttributeImpl(String name, List<String> values) {
        this.id = -1;
        this.entityVersion = -1;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((values == null) ? 0 : values.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof LocalAccountPersonAttributeImpl)) {
            return false;
        }
        LocalAccountPersonAttributeImpl other = (LocalAccountPersonAttributeImpl) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (values == null) {
            if (other.values != null) {
                return false;
            }
        } else if (!values.equals(other.values)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "LocalAccountPersonAttributeImpl [id="
                + this.id
                + ", entityVersion="
                + this.entityVersion
                + ", name="
                + this.name
                + "]";
    }
}
