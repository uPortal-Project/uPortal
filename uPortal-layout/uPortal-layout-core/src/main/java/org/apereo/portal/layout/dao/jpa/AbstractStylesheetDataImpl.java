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

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import org.apache.commons.lang.Validate;
import org.apereo.portal.layout.om.IStylesheetData;
import org.hibernate.annotations.Type;

/**
 */
@MappedSuperclass
abstract class AbstractStylesheetDataImpl implements IStylesheetData {
    @Column(name = "DATA_NAME", length = 100, nullable = false, updatable = false)
    private final String name;

    @Column(name = "DEFAULT_VALUE", length = 500)
    @Type(type = "nullSafeString")
    private String defaultValue;

    @Column(name = "PERSISTENCE_SCOPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private Scope scope;

    @Column(name = "DESCRIPTION", length = 2000)
    private String description;

    AbstractStylesheetDataImpl() {
        this.name = null;
    }

    public AbstractStylesheetDataImpl(String name, Scope scope) {
        Validate.notEmpty(name, "name cannot be null");
        Validate.notNull(scope, "scope cannot be null");

        this.name = name;
        this.scope = scope;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.layout.om.IStylesheetData#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.layout.om.IStylesheetData#setDefaultValue(java.lang.String)
     */
    @Override
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.layout.om.IStylesheetData#getDefaultValue()
     */
    @Override
    public String getDefaultValue() {
        return this.defaultValue;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.layout.om.IStylesheetData#setScope(org.apereo.portal.layout.om.IStylesheetData.Scope)
     */
    @Override
    public void setScope(Scope scope) {
        Validate.notNull(scope);
        this.scope = scope;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.layout.om.IStylesheetData#getScope()
     */
    @Override
    public Scope getScope() {
        return this.scope;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.layout.om.IStylesheetData#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.layout.om.IStylesheetData#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        AbstractStylesheetDataImpl other = (AbstractStylesheetDataImpl) obj;
        if (this.name == null) {
            if (other.name != null) return false;
        } else if (!this.name.equals(other.name)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "AbstractStylesheetDataImpl [name="
                + this.name
                + ", defaultValue="
                + this.defaultValue
                + ", scope="
                + this.scope
                + ", description="
                + this.description
                + "]";
    }
}
