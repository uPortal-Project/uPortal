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

package org.jasig.portal.layout.dao.jpa;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.SequenceGenerator;
import javax.persistence.TableGenerator;

import org.apache.commons.lang.Validate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.jasig.portal.layout.om.IStylesheetData;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Entity
@SequenceGenerator(
        name="UP_STYLESHEET_DATA_GEN",
        sequenceName="UP_STYLESHEET_DATA_SEQ",
        allocationSize=5
    )
@TableGenerator(
        name="UP_STYLESHEET_DATA_GEN",
        pkColumnValue="UP_STYLESHEET_DATA",
        allocationSize=5
    )
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
abstract class AbstractStylesheetDataImpl implements IStylesheetData {
    @Id
    @GeneratedValue(generator = "UP_STYLESHEET_DATA_GEN")
    @Column(name = "ID")
    private final long id;
    
    @Column(name = "NAME", length=100, nullable = false)
    private String name;
    
    @Column(name = "DEFAULT_VALUE", length=500)
    private String defaultValue;
    
    @Column(name = "SCOPE", nullable = false)
    private Scope scope;
    
    @Column(name = "DESCRIPTION", length=2000)
    private String description;
    
    //Required by hibernate for reflective creation
    AbstractStylesheetDataImpl() {
        this.id = -1;
    }
    
    public AbstractStylesheetDataImpl(String name, Scope scope) {
        this.id = -1;
        this.name = name;
        this.scope = scope;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.om.IStylesheetData#getId()
     */
    @Override
    public long getId() {
        return this.id;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.om.IStylesheetData#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        Validate.notNull(name);
        this.name = name;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.om.IStylesheetData#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.om.IStylesheetData#setDefaultValue(java.lang.String)
     */
    @Override
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.om.IStylesheetData#getDefaultValue()
     */
    @Override
    public String getDefaultValue() {
        return this.defaultValue;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.om.IStylesheetData#setScope(org.jasig.portal.layout.om.IStylesheetData.Scope)
     */
    @Override
    public void setScope(Scope scope) {
        Validate.notNull(scope);
        this.scope = scope;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.om.IStylesheetData#getScope()
     */
    @Override
    public Scope getScope() {
        return this.scope;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.om.IStylesheetData#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.om.IStylesheetData#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.defaultValue == null) ? 0 : this.defaultValue.hashCode());
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        result = prime * result + ((this.scope == null) ? 0 : this.scope.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractStylesheetDataImpl other = (AbstractStylesheetDataImpl) obj;
        if (this.defaultValue == null) {
            if (other.defaultValue != null)
                return false;
        }
        else if (!this.defaultValue.equals(other.defaultValue))
            return false;
        if (this.name == null) {
            if (other.name != null)
                return false;
        }
        else if (!this.name.equals(other.name))
            return false;
        if (this.scope != other.scope)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "AbstractStylesheetDataImpl [id=" + this.id + ", name=" + this.name + ", defaultValue=" + this.defaultValue
                + ", scope=" + this.scope + ", description=" + this.description + "]";
    }
}
