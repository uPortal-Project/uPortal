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

import java.util.LinkedHashSet;
import java.util.Set;

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

import org.apache.commons.lang.Validate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;
import org.jasig.portal.dao.usertype.FunctionalNameType;
import org.jasig.portal.layout.om.ILayoutAttributeDescriptor;
import org.jasig.portal.layout.om.IOutputPropertyDescriptor;
import org.jasig.portal.layout.om.IStylesheetDescriptor;
import org.jasig.portal.layout.om.IStylesheetParameterDescriptor;

/**
 * JPA implementation of stylesheet descriptor data
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Entity
@Table(
        name = "UP_SS_DESCRIPTOR"
    )
@SequenceGenerator(
        name="UP_SS_DESCRIPTOR_GEN",
        sequenceName="UP_SS_DESCRIPTOR_SEQ",
        allocationSize=5
    )
@TableGenerator(
        name="UP_SS_DESCRIPTOR_GEN",
        pkColumnValue="UP_SS_DESCRIPTOR",
        allocationSize=5
    )
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
class StylesheetDescriptorImpl implements IStylesheetDescriptor {
    @Id
    @GeneratedValue(generator = "UP_SS_DESCRIPTOR_GEN")
    @Column(name = "ID")
    private final long id;
    
    @Column(name = "NAME", length=100, nullable = false, unique = true)
    @Type(type = "fname")
    private String name;
    
    @Column(name = "DESCRIPTION", length=2000)
    private String description;
    
    @Column(name = "STYLESHEET", length=2000, nullable = false)
    private String stylesheetResource;
    
    @OneToMany(targetEntity = OutputPropertyDescriptorImpl.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "STYLESHEET_ID")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Fetch(FetchMode.JOIN)
    private Set<IOutputPropertyDescriptor> outputProperties = new LinkedHashSet<IOutputPropertyDescriptor>(0);
    
    @OneToMany(targetEntity = StylesheetParameterDescriptorImpl.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "STYLESHEET_ID")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Fetch(FetchMode.JOIN)
    private Set<IStylesheetParameterDescriptor> stylesheetParameters = new LinkedHashSet<IStylesheetParameterDescriptor>(0);
    
    @OneToMany(targetEntity = LayoutAttributeDescriptorImpl.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "STYLESHEET_ID")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Fetch(FetchMode.JOIN)
    private Set<ILayoutAttributeDescriptor> layoutAttributes = new LinkedHashSet<ILayoutAttributeDescriptor>(0);
    
    
    //Required for Hibernate reflection
    @SuppressWarnings("unused")
    private StylesheetDescriptorImpl() {
        this.id = -1;
    }
    
    public StylesheetDescriptorImpl(String name, String stylesheetResource) {
        this.id = -1;
        this.setName(name);
        this.setStylesheetResource(stylesheetResource);
    }


    /* (non-Javadoc)
     * @see org.jasig.portal.layout.om.IStylesheetDescriptor#getId()
     */
    @Override
    public long getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.om.IStylesheetDescriptor#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        FunctionalNameType.validate(name);
        this.name = name;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.om.IStylesheetDescriptor#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.om.IStylesheetDescriptor#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.om.IStylesheetDescriptor#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.om.IStylesheetDescriptor#setStylesheetResource(java.lang.String)
     */
    @Override
    public void setStylesheetResource(String stylesheetResource) {
        Validate.notEmpty(stylesheetResource);
        this.stylesheetResource = stylesheetResource;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.om.IStylesheetDescriptor#getStylesheetResource()
     */
    @Override
    public String getStylesheetResource() {
        return this.stylesheetResource;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.om.IStylesheetDescriptor#getOutputProperties()
     */
    @Override
    public Set<IOutputPropertyDescriptor> getOutputProperties() {
        return this.outputProperties;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.om.IStylesheetDescriptor#setOutputProperties(java.util.Set)
     */
    @Override
    public void setOutputProperties(Set<IOutputPropertyDescriptor> outputProperties) {
        Validate.notNull(outputProperties);
        this.outputProperties = outputProperties;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.om.IStylesheetDescriptor#getStylesheetParameters()
     */
    @Override
    public Set<IStylesheetParameterDescriptor> getStylesheetParameters() {
        return this.stylesheetParameters;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.om.IStylesheetDescriptor#setStylesheetParameters(java.util.Set)
     */
    @Override
    public void setStylesheetParameters(Set<IStylesheetParameterDescriptor> stylesheetParameters) {
        Validate.notNull(stylesheetParameters);
        this.stylesheetParameters = stylesheetParameters;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.om.IStylesheetDescriptor#getLayoutAttributes()
     */
    @Override
    public Set<ILayoutAttributeDescriptor> getLayoutAttributes() {
        return this.layoutAttributes;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.om.IStylesheetDescriptor#setLayoutAttributes(java.util.Set)
     */
    @Override
    public void setLayoutAttributes(Set<ILayoutAttributeDescriptor> layoutAttributes) {
        Validate.notNull(layoutAttributes);
        this.layoutAttributes = layoutAttributes;
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
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StylesheetDescriptorImpl other = (StylesheetDescriptorImpl) obj;
        if (this.name == null) {
            if (other.name != null)
                return false;
        }
        else if (!this.name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "StylesheetDescriptorImpl [id=" + this.id + ", name=" + this.name + ", description=" + this.description
                + ", stylesheetResource=" + this.stylesheetResource + "]";
    }
}
