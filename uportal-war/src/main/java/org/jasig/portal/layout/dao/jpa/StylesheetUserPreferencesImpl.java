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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Version;

import org.apache.commons.lang.Validate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.jasig.portal.layout.om.IStylesheetDescriptor;
import org.jasig.portal.layout.om.IStylesheetUserPreferences;
import org.jasig.portal.utils.Populator;

/**
 * JPA implementation of stylesheet user preferences data
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Entity
@Table(
        name = "UP_SS_USER_PREF"
    )
@SequenceGenerator(
        name="UP_SS_USER_PREF_GEN",
        sequenceName="UP_SS_USER_PREF_SEQ",
        allocationSize=5
    )
@TableGenerator(
        name="UP_SS_USER_PREF_GEN",
        pkColumnValue="UP_SS_USER_PREF",
        allocationSize=5
    )
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
class StylesheetUserPreferencesImpl implements IStylesheetUserPreferences {
    @Id
    @GeneratedValue(generator = "UP_SS_USER_PREF_GEN")
    @Column(name = "SS_USER_PREF_ID")
    private final long id;
    
    @Version
    @Column(name = "ENTITY_VERSION")
    private final long entityVersion;
    
    @NaturalId
    @ManyToOne(targetEntity = StylesheetDescriptorImpl.class, optional = false)
    @JoinColumn(name = "UP_SS_DESCRIPTOR_ID", nullable = false)
    private final IStylesheetDescriptor stylesheetDescriptor;
    
    //TODO eventually turn into object reference to IPerson
    @NaturalId
    @Column(name = "USER_ID", nullable = false, updatable = false)
    private final int userId;
    
    //TODO eventually turn into object reference to UserProfile
    @NaturalId
    @Column(name = "PROFILE_ID", nullable = false, updatable = false)
    private final int profileId;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name="PROP_NAME", nullable=false, length = 500)
    @Column(name="PROP_VALUE", nullable=false, length = 2000)
    @Type(type="nullSafeString") //only applies to map values
    @CollectionTable(
            name="UP_SS_USER_PREF_OUTPUT_PROP", 
            joinColumns = @JoinColumn(name = "SS_USER_PREF_ID", nullable = false))
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Fetch(FetchMode.JOIN)
    private final Map<String, String> outputProperties = new LinkedHashMap<String, String>(0);
    
    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name="PARAM_NAME", nullable=false, length = 500)
    @Column(name="PARAM_VALUE", nullable=false, length = 2000)
    @Type(type="nullSafeString") //only applies to map values
    @CollectionTable(
            name="UP_SS_USER_PREF_PARAM", 
            joinColumns = @JoinColumn(name = "SS_USER_PREF_ID", nullable = false))
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Fetch(FetchMode.JOIN)
    private final Map<String, String> parameters = new LinkedHashMap<String, String>(0);

    @OneToMany(targetEntity = LayoutNodeAttributesImpl.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @MapKey(name="nodeId")
    @JoinColumn(name = "SS_USER_PREF_ID", nullable=false)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Fetch(FetchMode.JOIN)
    private final Map<String, LayoutNodeAttributesImpl> layoutAttributes = new LinkedHashMap<String, LayoutNodeAttributesImpl>(0);
    
    
    @SuppressWarnings("unused")
    private StylesheetUserPreferencesImpl() {
        this.id = -1;
        this.entityVersion = -1;
        this.stylesheetDescriptor = null;
        this.userId = -1;
        this.profileId = -1;
    }
    
    StylesheetUserPreferencesImpl(IStylesheetDescriptor stylesheetDescriptor, int userId, int profileId) {
        this.id = -1;
        this.entityVersion = -1;
        this.stylesheetDescriptor = stylesheetDescriptor;
        this.userId = userId;
        this.profileId = profileId;
    }
    
    @PrePersist
    @SuppressWarnings("unused") //Called by jpa/hibernate via reflection
    private void purgeEmptyLayoutNodes() {
        //Remove layout attribute objects when they have no values left.
        for (final Iterator<LayoutNodeAttributesImpl> layoutNodeAttrsItr = this.layoutAttributes.values().iterator();
            layoutNodeAttrsItr.hasNext(); ) {
            
            final LayoutNodeAttributesImpl layoutNodeAttrs = layoutNodeAttrsItr.next();
            if (layoutNodeAttrs.getAttributes().isEmpty()) {
                layoutNodeAttrsItr.remove();
            }
        }
    }
    
    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public long getStylesheetDescriptorId() {
        return this.stylesheetDescriptor.getId();
    }

    @Override
    public int getUserId() {
        return this.userId;
    }

    @Override
    public int getProfileId() {
        return this.profileId;
    }
    
    @Override
    public String getOutputProperty(String name) {
        Validate.notEmpty(name, "name cannot be null");
        
        return this.outputProperties.get(name);
    }

    @Override
    public String setOutputProperty(String name, String value) {
        Validate.notEmpty(name, "name cannot be null");
        Validate.notEmpty(value, "value cannot be null");
        
        return this.outputProperties.put(name, value);
    }

    @Override
    public String removeOutputProperty(String name) {
        Validate.notEmpty(name, "name cannot be null");
        
        return this.outputProperties.remove(name);
    }

    
    @Override
    public <P extends Populator<String, String>> P populateOutputProperties(P properties) {
        properties.putAll(this.outputProperties);
        return properties;
    }
    
    @Override
    public void clearOutputProperties() {
        this.outputProperties.clear();
    }

    @Override
    public String getStylesheetParameter(String name) {
        Validate.notEmpty(name, "name cannot be null");
        
        return this.parameters.get(name);
    }

    @Override
    public String setStylesheetParameter(String name, String value) {
        Validate.notEmpty(name, "name cannot be null");
        Validate.notEmpty(value, "value cannot be null");
        
        return this.parameters.put(name, value);
    }

    @Override
    public String removeStylesheetParameter(String name) {
        Validate.notEmpty(name, "name cannot be null");
        
        return this.parameters.remove(name);
    }

    @Override
    public <P extends Populator<String, String>> P populateStylesheetParameters(P stylesheetParameters) {
        stylesheetParameters.putAll(this.parameters);
        return stylesheetParameters;
    }
    
    @Override
    public void clearStylesheetParameters() {
        this.parameters.clear();
    }

    protected LayoutNodeAttributesImpl getLayoutNodeAttributes(String nodeId, boolean create) {
        LayoutNodeAttributesImpl layoutAttribute = this.layoutAttributes.get(nodeId);
        if (layoutAttribute != null) {
            return layoutAttribute;
        }
        
        if (!create) {
            return null;
        }

        layoutAttribute = new LayoutNodeAttributesImpl(nodeId);
        this.layoutAttributes.put(nodeId, layoutAttribute);
        return layoutAttribute;
    }
    

    @Override
    public String getLayoutAttribute(String nodeId, String name) {
        Validate.notEmpty(nodeId, "nodeId cannot be null");
        Validate.notEmpty(name, "name cannot be null");
        
        final LayoutNodeAttributesImpl layoutAttribute = getLayoutNodeAttributes(nodeId, true);
        final Map<String, String> attributes = layoutAttribute.getAttributes();
        return attributes.get(name);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.om.IStylesheetUserPreferences#setLayoutAttribute(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public String setLayoutAttribute(String nodeId, String name, String value) {
        Validate.notEmpty(nodeId, "nodeId cannot be null");
        Validate.notEmpty(name, "name cannot be null");
        Validate.notEmpty(value, "value cannot be null");
        
        final LayoutNodeAttributesImpl layoutAttribute = getLayoutNodeAttributes(nodeId, true);
        final Map<String, String> attributes = layoutAttribute.getAttributes();
        return attributes.put(name, value);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.om.IStylesheetUserPreferences#removeLayoutAttribute(java.lang.String, java.lang.String)
     */
    @Override
    public String removeLayoutAttribute(String nodeId, String name) {
        Validate.notEmpty(nodeId, "nodeId cannot be null");
        Validate.notEmpty(name, "name cannot be null");
        
        final LayoutNodeAttributesImpl layoutAttribute = getLayoutNodeAttributes(nodeId, false);
        if (layoutAttribute == null) {
            return null;
        }
        final Map<String, String> attributes = layoutAttribute.getAttributes();
        return attributes.remove(name);
    }
    
    @Override
    public <P extends Populator<String, String>> P populateLayoutAttributes(String nodeId, P layoutAttributes) {
        Validate.notEmpty(nodeId, "nodeId cannot be null");
        
        final LayoutNodeAttributesImpl nodeAttributes = this.layoutAttributes.get(nodeId);
        if (nodeAttributes != null) {
            layoutAttributes.putAll(nodeAttributes.getAttributes());
        }
        
        return layoutAttributes;
    }
    
    @Override
    public Collection<String> getAllLayoutAttributeNodeIds() {
        return Collections.unmodifiableSet(this.layoutAttributes.keySet());
    }

    @Override
    public void clearLayoutAttributes(String nodeId) {
        Validate.notEmpty(nodeId, "nodeId cannot be null");
        
        this.layoutAttributes.remove(nodeId);
    }

    @Override
    public void clearAllLayoutAttributes() {
        this.layoutAttributes.clear(); 
    }

    @Override
    public String toString() {
        return "StylesheetUserPreferencesImpl [id=" + this.id + ", entityVersion=" + this.entityVersion
                + ", stylesheetDescriptor=" + this.stylesheetDescriptor + ", userId=" + this.userId + ", profileId="
                + this.profileId + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.profileId;
        result = prime * result + ((this.stylesheetDescriptor == null) ? 0 : this.stylesheetDescriptor.hashCode());
        result = prime * result + this.userId;
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
        StylesheetUserPreferencesImpl other = (StylesheetUserPreferencesImpl) obj;
        if (this.profileId != other.profileId)
            return false;
        if (this.stylesheetDescriptor == null) {
            if (other.stylesheetDescriptor != null)
                return false;
        }
        else if (!this.stylesheetDescriptor.equals(other.stylesheetDescriptor))
            return false;
        if (this.userId != other.userId)
            return false;
        return true;
    }
}
