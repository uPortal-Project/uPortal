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
package org.apereo.portal.portlet.dao.jpa;

import java.util.ArrayList;
import java.util.Arrays;
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
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import javax.persistence.Version;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.pluto.container.PortletPreference;
import org.apache.pluto.container.om.portlet.Preference;
import org.apereo.portal.dao.usertype.NullSafeStringColumnMapper;
import org.apereo.portal.portlet.om.IPortletPreference;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.Type;

/**
 */
@Entity
@Table(name = "UP_PORTLET_PREF")
@org.hibernate.annotations.Table(
    appliesTo = "UP_PORTLET_PREF",
    indexes = @Index(name = "IDX_UPP_PREFS_ID", columnNames = "PORTLET_PREFS_ID")
)
@SequenceGenerator(
    name = "UP_PORTLET_PREF_GEN",
    sequenceName = "UP_PORTLET_PREF_SEQ",
    allocationSize = 10
)
@TableGenerator(
    name = "UP_PORTLET_PREF_GEN",
    pkColumnValue = "UP_PORTLET_PREF",
    allocationSize = 10
)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class PortletPreferenceImpl implements IPortletPreference, Cloneable {
    private static final String NULL_MARKER = "NULL";

    @Id
    @GeneratedValue(generator = "UP_PORTLET_PREF_GEN")
    @Column(name = "PORTLET_PREF_ID")
    private final long portletPreferenceId;

    @Version
    @Column(name = "ENTITY_VERSION")
    private final long entityVersion;

    @Column(name = "PREF_NAME", length = 100000)
    @Type(type = "org.hibernate.type.TextType")
    @Lob
    private String name = null;

    @Column(name = "READ_ONLY", nullable = false)
    private boolean readOnly = false;

    @ElementCollection(fetch = FetchType.EAGER, targetClass = String.class)
    @JoinTable(name = "UP_PORTLET_PREF_VALUES", joinColumns = @JoinColumn(name = "PORTLET_PREF_ID"))
    @IndexColumn(name = "VALUE_ORDER")
    @Lob
    @Column(name = "PREF_VALUE", length = 100000)
    @Type(type = "org.hibernate.type.TextType")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Fetch(FetchMode.JOIN)
    private List<String> values = new ArrayList<String>(0);

    @Column(name = "NULL_VALUES", nullable = false)
    private boolean nullValues = true;

    @Transient private transient String[] valuesArray;

    public PortletPreferenceImpl() {
        this.portletPreferenceId = -1;
        this.entityVersion = -1;
    }

    public PortletPreferenceImpl(PortletPreferenceImpl portletPreference) {
        if (portletPreference.getName() == null) {
            throw new IllegalArgumentException("name cannot be null");
        }

        this.portletPreferenceId = -1;
        this.entityVersion = -1;
        this.name = portletPreference.getName();
        this.readOnly = portletPreference.isReadOnly();
        this.setValues(portletPreference.getValues());
    }

    public PortletPreferenceImpl(PortletPreference portletPreference) {
        if (portletPreference.getName() == null) {
            throw new IllegalArgumentException("name cannot be null");
        }

        this.portletPreferenceId = -1;
        this.entityVersion = -1;
        this.name = portletPreference.getName();
        this.readOnly = portletPreference.isReadOnly();

        final String[] values = portletPreference.getValues();
        this.setValues(values);
    }

    public PortletPreferenceImpl(Preference preference) {
        if (preference.getName() == null) {
            throw new IllegalArgumentException("name cannot be null");
        }

        this.portletPreferenceId = -1;
        this.entityVersion = -1;
        this.name = preference.getName();
        this.readOnly = preference.isReadOnly();

        this.setValues(preference.getValues().toArray(new String[] {}));
    }

    public PortletPreferenceImpl(String name, boolean readOnly, String... values) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }

        this.portletPreferenceId = -1;
        this.entityVersion = -1;
        this.name = name;
        this.readOnly = readOnly;
        this.setValues(values);
    }

    /*
     * (non-Javadoc)
     * @see org.apache.pluto.container.PortletPreference#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.pluto.container.PortletPreference#getValues()
     */
    @Override
    public String[] getValues() {
        if (this.nullValues || this.values == null) {
            return null;
        }

        if (this.valuesArray != null) {
            return this.valuesArray.clone();
        }

        final String[] valuesArray = new String[this.values.size()];
        int index = 0;
        for (final String value : this.values) {
            if (NULL_MARKER.equals(value)) {
                valuesArray[index++] = null;
            } else {
                valuesArray[index++] = value.substring(1);
            }
        }
        this.valuesArray = valuesArray.clone();
        return valuesArray;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.pluto.container.PortletPreference#isReadOnly()
     */
    @Override
    public boolean isReadOnly() {
        return this.readOnly;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.pluto.container.PortletPreference#setValues(java.lang.String[])
     */
    @Override
    public void setValues(String[] values) {
        if (values == null) {
            this.valuesArray = null;
            this.values = null;
            this.nullValues = true;
        } else if (this.values == null) {
            this.values = new ArrayList<String>(Arrays.asList(values));
            this.nullValues = false;
            this.valuesArray = values.clone();
        } else {
            this.nullValues = false;
            this.values.clear();
            for (final String value : values) {
                if (value == null) {
                    this.values.add(NULL_MARKER);
                } else {
                    this.values.add(NullSafeStringColumnMapper.NOT_NULL_PREFIX + value);
                }
            }
            this.valuesArray = values.clone();
        }
    }

    /*
     * (non-Javadoc)
     * @see org.apache.pluto.container.PortletPreference#clone()
     */
    @Override
    public PortletPreference clone() {
        return new PortletPreferenceImpl(this);
    }

    /** @see java.lang.Object#equals(Object) */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof IPortletPreference)) {
            return false;
        }
        PortletPreferenceImpl rhs = (PortletPreferenceImpl) object;
        return new EqualsBuilder()
                .append(this.name, rhs.getName())
                .append(this.readOnly, rhs.isReadOnly())
                .append(this.getValues(), rhs.getValues())
                .isEquals();
    }

    /** @see java.lang.Object#hashCode() */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(-1904185833, -1222355625)
                .append(this.name)
                .append(this.readOnly)
                .append(this.values)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "PortletPreferenceImpl [portletPreferenceId="
                + this.portletPreferenceId
                + ", entityVersion="
                + this.entityVersion
                + ", name="
                + this.name
                + ", readOnly="
                + this.readOnly
                + "]";
    }
}
