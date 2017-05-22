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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Version;
import org.apache.commons.lang.Validate;
import org.apereo.portal.dao.usertype.FunctionalNameType;
import org.apereo.portal.layout.om.ILayoutAttributeDescriptor;
import org.apereo.portal.layout.om.IOutputPropertyDescriptor;
import org.apereo.portal.layout.om.IStylesheetData;
import org.apereo.portal.layout.om.IStylesheetDescriptor;
import org.apereo.portal.layout.om.IStylesheetParameterDescriptor;
import org.apereo.portal.layout.om.IStylesheetUserPreferences;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;
import org.hibernate.annotations.Type;

/**
 * JPA implementation of stylesheet descriptor data
 *
 */
@Entity
@Table(name = "UP_SS_DESC")
@SequenceGenerator(name = "UP_SS_DESC_GEN", sequenceName = "UP_SS_DESC_SEQ", allocationSize = 5)
@TableGenerator(name = "UP_SS_DESC_GEN", pkColumnValue = "UP_SS_DESC", allocationSize = 5)
@NaturalIdCache(region = "org.apereo.portal.layout.dao.jpa.StylesheetDescriptorImpl-NaturalId")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class StylesheetDescriptorImpl implements IStylesheetDescriptor {
    @Id
    @GeneratedValue(generator = "UP_SS_DESC_GEN")
    @Column(name = "SS_DESC_ID")
    private final long id;

    @Version
    @Column(name = "ENTITY_VERSION")
    private final long entityVersion;

    //Hidden reference to the child stylesheet user preferences, used to allow cascading deletes where when a stylesheet descriptor is deleted all associated preferences are also deleted
    //MUST BE LAZY FETCH, this set should never actually be populated at runtime or performance will be TERRIBLE
    @SuppressWarnings("unused")
    @OneToMany(
        mappedBy = "stylesheetDescriptor",
        targetEntity = StylesheetUserPreferencesImpl.class,
        cascade = {CascadeType.ALL},
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    private Set<IStylesheetUserPreferences> stylesheetUserPreferences = null;

    @NaturalId
    @Column(name = "SS_NAME", length = 100, nullable = false)
    @Type(type = "fname")
    private final String name;

    @Column(name = "URL_SYNTAX_HELPER_NAME", length = 100)
    private String urlNodeSyntaxHelperName;

    @Column(name = "DESCRIPTION", length = 2000)
    private String description;

    @Column(name = "STYLESHEET", length = 2000, nullable = false)
    private String stylesheetResource;

    @OneToMany(
        targetEntity = OutputPropertyDescriptorImpl.class,
        cascade = CascadeType.ALL,
        fetch = FetchType.EAGER,
        orphanRemoval = true
    )
    @JoinColumn(name = "SS_DESC_ID", nullable = false)
    @MapKey(name = "name")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Fetch(FetchMode.JOIN)
    private Map<String, IOutputPropertyDescriptor> outputProperties =
            new LinkedHashMap<String, IOutputPropertyDescriptor>(0);

    @OneToMany(
        targetEntity = StylesheetParameterDescriptorImpl.class,
        cascade = CascadeType.ALL,
        fetch = FetchType.EAGER,
        orphanRemoval = true
    )
    @MapKey(name = "name")
    @JoinColumn(name = "SS_DESC_ID", nullable = false)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Fetch(FetchMode.JOIN)
    private Map<String, IStylesheetParameterDescriptor> stylesheetParameters =
            new LinkedHashMap<String, IStylesheetParameterDescriptor>(0);

    @OneToMany(
        targetEntity = LayoutAttributeDescriptorImpl.class,
        cascade = CascadeType.ALL,
        fetch = FetchType.EAGER,
        orphanRemoval = true
    )
    @MapKey(name = "name")
    @JoinColumn(name = "SS_DESC_ID", nullable = false)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Fetch(FetchMode.JOIN)
    private Map<String, ILayoutAttributeDescriptor> layoutAttributes =
            new LinkedHashMap<String, ILayoutAttributeDescriptor>(0);

    //Required for Hibernate reflection
    @SuppressWarnings("unused")
    private StylesheetDescriptorImpl() {
        this.id = -1;
        this.entityVersion = -1;
        this.name = null;
    }

    StylesheetDescriptorImpl(String name, String stylesheetResource) {
        FunctionalNameType.validate(name);

        this.id = -1;
        this.entityVersion = -1;
        this.name = name;
        this.setStylesheetResource(stylesheetResource);
    }

    @Override
    public String getDataId() {
        return this.getName();
    }

    @Override
    public String getDataTitle() {
        return this.getName();
    }

    @Override
    public String getDataDescription() {
        return this.getDescription();
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.layout.om.IStylesheetDescriptor#getId()
     */
    @Override
    public long getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.layout.om.IStylesheetDescriptor#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.layout.om.IStylesheetDescriptor#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.layout.om.IStylesheetDescriptor#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.layout.om.IStylesheetDescriptor#setStylesheetResource(java.lang.String)
     */
    @Override
    public void setStylesheetResource(String stylesheetResource) {
        Validate.notEmpty(stylesheetResource);
        this.stylesheetResource = stylesheetResource;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.layout.om.IStylesheetDescriptor#getStylesheetResource()
     */
    @Override
    public String getStylesheetResource() {
        return this.stylesheetResource;
    }

    @Override
    public void setUrlNodeSyntaxHelperName(String urlNodeSyntaxHelperName) {
        this.urlNodeSyntaxHelperName = urlNodeSyntaxHelperName;
    }

    @Override
    public String getUrlNodeSyntaxHelperName() {
        return this.urlNodeSyntaxHelperName;
    }

    @Override
    public Collection<IOutputPropertyDescriptor> getOutputPropertyDescriptors() {
        return Collections.unmodifiableCollection(this.outputProperties.values());
    }

    @Override
    public void setOutputPropertyDescriptors(
            Collection<IOutputPropertyDescriptor> outputPropertyDescriptors) {
        setMap(
                this.outputProperties,
                outputPropertyDescriptors,
                StylesheetDataUpdater.<IOutputPropertyDescriptor>getInstance());
    }

    @Override
    public IOutputPropertyDescriptor getOutputPropertyDescriptor(String name) {
        Validate.notEmpty(name, "name cannot be null");
        return this.outputProperties.get(name);
    }

    @Override
    public Collection<IStylesheetParameterDescriptor> getStylesheetParameterDescriptors() {
        return Collections.unmodifiableCollection(this.stylesheetParameters.values());
    }

    @Override
    public void setStylesheetParameterDescriptors(
            Collection<IStylesheetParameterDescriptor> stylesheetParameterDescriptors) {
        setMap(
                this.stylesheetParameters,
                stylesheetParameterDescriptors,
                StylesheetDataUpdater.<IStylesheetParameterDescriptor>getInstance());
    }

    @Override
    public IStylesheetParameterDescriptor getStylesheetParameterDescriptor(String name) {
        Validate.notEmpty(name, "name cannot be null");
        return this.stylesheetParameters.get(name);
    }

    @Override
    public Collection<ILayoutAttributeDescriptor> getLayoutAttributeDescriptors() {
        return Collections.unmodifiableCollection(this.layoutAttributes.values());
    }

    @Override
    public void setLayoutAttributeDescriptors(
            Collection<ILayoutAttributeDescriptor> layoutAttributeDescriptors) {
        setMap(
                this.layoutAttributes,
                layoutAttributeDescriptors,
                LAYOUT_ATTRIBUTE_DESCRIPTOR_UPDATER);
    }

    @Override
    public ILayoutAttributeDescriptor getLayoutAttributeDescriptor(String name) {
        Validate.notEmpty(name, "name cannot be null");
        return this.layoutAttributes.get(name);
    }

    protected <T extends IStylesheetData> void setMap(
            Map<String, T> dataMap, Collection<T> dataCollection, Updater<T> updater) {
        final Set<String> oldDataKeys = new HashSet<String>(dataMap.keySet());

        for (T newData : dataCollection) {
            final String name = newData.getName();
            oldDataKeys.remove(name);

            final T oldData = dataMap.get(name);
            if (oldData != null) {
                newData = updater.update(oldData, newData);
            }

            dataMap.put(name, newData);
        }

        //Remove all old data entries that were not updated
        dataMap.keySet().remove(oldDataKeys);
    }

    private interface Updater<V> {
        public V update(V existingObject, V newObject);
    }

    private static class StylesheetDataUpdater<V extends IStylesheetData> implements Updater<V> {
        private static final StylesheetDataUpdater<IStylesheetData> INSTANCE =
                new StylesheetDataUpdater<IStylesheetData>();

        @SuppressWarnings("unchecked")
        public static <V extends IStylesheetData> StylesheetDataUpdater<V> getInstance() {
            return (StylesheetDataUpdater<V>) INSTANCE;
        }

        @Override
        public V update(V existingObject, V newObject) {
            existingObject.setDefaultValue(newObject.getDefaultValue());
            existingObject.setDescription(newObject.getDefaultValue());
            existingObject.setScope(newObject.getScope());

            return existingObject;
        }
    }

    private static final StylesheetDataUpdater<ILayoutAttributeDescriptor>
            LAYOUT_ATTRIBUTE_DESCRIPTOR_UPDATER =
                    new StylesheetDataUpdater<ILayoutAttributeDescriptor>() {
                        @Override
                        public ILayoutAttributeDescriptor update(
                                ILayoutAttributeDescriptor existingObject,
                                ILayoutAttributeDescriptor newObject) {
                            existingObject.setTargetElementNames(newObject.getTargetElementNames());

                            return super.update(existingObject, newObject);
                        }
                    };

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
        StylesheetDescriptorImpl other = (StylesheetDescriptorImpl) obj;
        if (this.name == null) {
            if (other.name != null) return false;
        } else if (!this.name.equals(other.name)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "StylesheetDescriptorImpl [id="
                + this.id
                + ", entityVersion="
                + this.entityVersion
                + ", name="
                + this.name
                + ", urlNodeSyntaxHelperName="
                + this.urlNodeSyntaxHelperName
                + ", description="
                + this.description
                + ", stylesheetResource="
                + this.stylesheetResource
                + "]";
    }
}
