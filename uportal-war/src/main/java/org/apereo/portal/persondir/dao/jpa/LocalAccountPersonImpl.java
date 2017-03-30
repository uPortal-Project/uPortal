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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Version;
import org.apereo.portal.persondir.ILocalAccountPerson;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.NaturalId;
import org.springframework.util.Assert;

@Entity
@Table(name = "UP_PERSON_DIR")
@SequenceGenerator(
    name = "UP_PERSON_DIR_GEN",
    sequenceName = "UP_PERSON_DIR_SEQ",
    allocationSize = 5
)
@TableGenerator(name = "UP_PERSON_DIR_GEN", pkColumnValue = "UP_PERSON_DIR", allocationSize = 5)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
class LocalAccountPersonImpl implements Serializable, ILocalAccountPerson {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "UP_PERSON_DIR_GEN")
    @Column(name = "USER_DIR_ID")
    private final long id;

    @Version
    @Column(name = "ENTITY_VERSION")
    private final long entityVersion;

    @NaturalId
    @Column(name = "USER_NAME", length = 100, nullable = false)
    private final String name;

    @Column(name = "ENCRPTD_PSWD", length = 256)
    private String password;

    @Column(name = "LST_PSWD_CGH_DT")
    private Date lastPasswordChange;

    @OneToMany(
        targetEntity = LocalAccountPersonAttributeImpl.class,
        fetch = FetchType.EAGER,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @JoinColumn(name = "USER_DIR_ID", nullable = false)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Fetch(FetchMode.JOIN)
    private final Collection<LocalAccountPersonAttributeImpl> attributes =
            new ArrayList<LocalAccountPersonAttributeImpl>(0);

    @SuppressWarnings("unused")
    private LocalAccountPersonImpl() {
        this.id = -1;
        this.entityVersion = -1;
        this.name = null;
    }

    public LocalAccountPersonImpl(String name) {
        Assert.notNull(name);

        this.id = -1;
        this.entityVersion = -1;
        this.name = name;
    }

    public LocalAccountPersonImpl(String name, Long Id) {
        Assert.notNull(name);
        Assert.notNull(Id);
        this.id = Id;
        this.entityVersion = -1;
        this.name = name;
    }

    @Override
    public long getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.persondir.jpa.ILocalAccountPersonAttribute#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.persondir.jpa.ILocalAccountPersonAttribute#getPassword()
     */
    @Override
    public String getPassword() {
        return password;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.persondir.jpa.ILocalAccountPersonAttribute#setPassword(java.lang.String)
     */
    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.persondir.jpa.ILocalAccountPersonAttribute#getLastPasswordChange()
     */
    @Override
    public Date getLastPasswordChange() {
        return lastPasswordChange;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.persondir.jpa.ILocalAccountPersonAttribute#setLastPasswordChange(java.util.Date)
     */
    @Override
    public void setLastPasswordChange(Date lastPasswordChange) {
        this.lastPasswordChange = lastPasswordChange;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.persondir.jpa.ILocalAccountPersonAttribute#getAttributeValue(java.lang.String)
     */
    @Override
    public Object getAttributeValue(String name) {
        final List<Object> values = this.getAttributeValues(name);
        if (values != null && values.size() > 0) {
            return values.get(0);
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.persondir.jpa.ILocalAccountPersonAttribute#getAttributeValues(java.lang.String)
     */
    @Override
    public List<Object> getAttributeValues(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }

        for (LocalAccountPersonAttributeImpl attribute : attributes) {
            if (name.equals(attribute.getName())) {
                return this.getObjectValues(attribute);
            }
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.persondir.jpa.ILocalAccountPersonAttribute#getAttributes()
     */
    @Override
    public Map<String, List<Object>> getAttributes() {
        final Map<String, List<Object>> attributeMap = new LinkedHashMap<String, List<Object>>();

        for (final LocalAccountPersonAttributeImpl attribute : attributes) {
            final List<Object> objValues = this.getObjectValues(attribute);
            attributeMap.put(attribute.getName(), objValues);
        }

        return Collections.unmodifiableMap(attributeMap);
    }

    @Override
    public void setAttribute(String name, String... values) {
        this.setAttribute(name, Arrays.asList(values));
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.persondir.jpa.ILocalAccountPersonAttribute#setAttribute(java.lang.String, java.util.List)
     */
    @Override
    public void setAttribute(String name, List<String> values) {
        for (LocalAccountPersonAttributeImpl attribute : attributes) {
            if (name.equals(attribute.getName())) {
                attribute.setValues(values);
                return;
            }
        }

        attributes.add(new LocalAccountPersonAttributeImpl(name, values));
    }

    public void setAttribute(String name, String value) {
        for (LocalAccountPersonAttributeImpl attribute : attributes) {
            if (name.equals(attribute.getName())) {
                attribute.setValues(Collections.singletonList(value));
                return;
            }
        }

        attributes.add(new LocalAccountPersonAttributeImpl(name, Collections.singletonList(value)));
    }

    @Override
    public boolean removeAttribute(String name) {
        for (final Iterator<LocalAccountPersonAttributeImpl> itr = attributes.iterator();
                itr.hasNext();
                ) {
            if (itr.next().getName().equals(name)) {
                itr.remove();
                return true;
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.persondir.jpa.ILocalAccountPersonAttribute#setAttributes(java.util.Map)
     */
    @Override
    public void setAttributes(Map<String, List<String>> attributes) {
        //Tries to modify as many of the existing attributes in place to reduce DB churn in hibernate

        //Make a local copy so we don't edit the original reference
        attributes = new LinkedHashMap<String, List<String>>(attributes);

        for (final Iterator<LocalAccountPersonAttributeImpl> attributesItr =
                        this.attributes.iterator();
                attributesItr.hasNext();
                ) {
            final LocalAccountPersonAttributeImpl attribute = attributesItr.next();

            //Remove the new values for the attribute from the input map
            final String name = attribute.getName();
            final List<String> newValues = attributes.remove(name);

            //If no new values remove the attribute
            if (newValues == null) {
                attributesItr.remove();
            }
            //Otherwise update the existing values
            else {
                attribute.setValues(new ArrayList<String>(newValues));
            }
        }

        //Add any remaining new attributes to the list
        for (final Map.Entry<String, List<String>> attribute : attributes.entrySet()) {
            final String name = attribute.getKey();
            final List<String> values = attribute.getValue();
            this.attributes.add(new LocalAccountPersonAttributeImpl(name, values));
        }
    }

    protected List<Object> getObjectValues(LocalAccountPersonAttributeImpl attribute) {
        final List<String> values = attribute.getValues();
        final List<Object> objValues = new ArrayList<Object>(values.size());
        objValues.addAll(values);
        return Collections.unmodifiableList(objValues);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
        result =
                prime * result + ((lastPasswordChange == null) ? 0 : lastPasswordChange.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((password == null) ? 0 : password.hashCode());
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
        if (!(obj instanceof LocalAccountPersonImpl)) {
            return false;
        }
        LocalAccountPersonImpl other = (LocalAccountPersonImpl) obj;
        if (attributes == null) {
            if (other.attributes != null) {
                return false;
            }
        } else if (!attributes.equals(other.attributes)) {
            return false;
        }
        if (lastPasswordChange == null) {
            if (other.lastPasswordChange != null) {
                return false;
            }
        } else if (!lastPasswordChange.equals(other.lastPasswordChange)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (password == null) {
            if (other.password != null) {
                return false;
            }
        } else if (!password.equals(other.password)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "LocalAccountPersonImpl [id="
                + this.id
                + ", entityVersion="
                + this.entityVersion
                + ", name="
                + this.name
                + ", lastPasswordChange="
                + this.lastPasswordChange
                + "]";
    }
}
