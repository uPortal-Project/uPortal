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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
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
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.portlet.WindowState;
import org.apache.commons.lang.Validate;
import org.apereo.portal.layout.dao.jpa.StylesheetDescriptorImpl;
import org.apereo.portal.layout.om.IStylesheetDescriptor;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletDefinitionId;
import org.apereo.portal.portlet.om.IPortletEntity;
import org.apereo.portal.portlet.om.IPortletEntityId;
import org.apereo.portal.portlet.om.IPortletPreference;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;

/**
 */
@Entity
@Table(name = "UP_PORTLET_ENT")
@SequenceGenerator(
    name = "UP_PORTLET_ENT_GEN",
    sequenceName = "UP_PORTLET_ENT_SEQ",
    allocationSize = 10
)
@TableGenerator(name = "UP_PORTLET_ENT_GEN", pkColumnValue = "UP_PORTLET_ENT", allocationSize = 10)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
class PortletEntityImpl implements IPortletEntity {
    //Properties are final to stop changes in code, hibernate overrides the final via reflection to set their values
    @Id
    @GeneratedValue(generator = "UP_PORTLET_ENT_GEN")
    @Column(name = "PORTLET_ENT_ID")
    private final long internalPortletEntityId;

    @SuppressWarnings("unused")
    @Version
    @Column(name = "ENTITY_VERSION")
    private final long entityVersion;

    @NaturalId
    @Column(name = "LAYOUT_NODE_ID", nullable = false)
    private final String layoutNodeId;

    @NaturalId
    @Index(name = "IDX_UP_PORTLET_ENT__UP_USER")
    @Column(name = "USER_ID", nullable = false)
    private final int userId;

    //Hidden reference to the parent portlet definition, used by hibernate for referential integrety
    @ManyToOne(
        targetEntity = PortletDefinitionImpl.class,
        cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}
    )
    @JoinColumn(name = "PORTLET_DEF_ID", nullable = false)
    private final IPortletDefinition portletDefinition;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "UP_PORTLET_ENT__STATES",
        joinColumns = @JoinColumn(name = "PORTLET_ENT_ID")
    )
    @MapKeyJoinColumn(name = "STYLESHEET_DESCRIPTOR_ID")
    @Column(name = "WINDOW_STATE")
    @Type(type = "windowState")
    private final Map<StylesheetDescriptorImpl, WindowState> windowStates =
            new HashMap<StylesheetDescriptorImpl, WindowState>(0);

    @OneToOne(
        cascade = {CascadeType.ALL},
        orphanRemoval = true
    )
    @JoinColumn(name = "PORTLET_PREFS_ID", nullable = false)
    @Fetch(FetchMode.JOIN)
    private final PortletPreferencesImpl portletPreferences;

    @Transient private IPortletEntityId portletEntityId = null;

    /** Used to initialize fields after persistence actions. */
    @SuppressWarnings("unused")
    @PostLoad
    @PostPersist
    @PostUpdate
    @PostRemove
    private void init() {
        this.portletEntityId = new PortletEntityIdImpl(this.internalPortletEntityId);
    }

    /** Used by the ORM layer to create instances of the object. */
    @SuppressWarnings("unused")
    private PortletEntityImpl() {
        this.internalPortletEntityId = -1;
        this.entityVersion = -1;
        this.portletDefinition = null;
        this.layoutNodeId = null;
        this.userId = -1;
        this.portletPreferences = null;
    }

    public PortletEntityImpl(
            IPortletDefinition portletDefinition, String channelSubscribeId, int userId) {
        Validate.notNull(portletDefinition, "portletDefinition can not be null");
        Validate.notNull(channelSubscribeId, "layoutNodeId can not be null");

        this.internalPortletEntityId = -1;
        this.entityVersion = -1;
        this.portletDefinition = portletDefinition;
        this.layoutNodeId = channelSubscribeId;
        this.userId = userId;
        this.portletPreferences = new PortletPreferencesImpl();
    }

    @Override
    public IPortletDefinitionId getPortletDefinitionId() {
        return this.portletDefinition.getPortletDefinitionId();
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.om.portlet.IPortletEntity#getPortletEntityId()
     */
    @Override
    public IPortletEntityId getPortletEntityId() {
        return this.portletEntityId;
    }

    @Override
    public IPortletDefinition getPortletDefinition() {
        return this.portletDefinition;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.om.portlet.IPortletEntity#getChannelSubscribeId()
     */
    @Override
    public String getLayoutNodeId() {
        return this.layoutNodeId;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.om.portlet.IPortletEntity#getUserId()
     */
    @Override
    public int getUserId() {
        return this.userId;
    }

    @Override
    public Map<Long, WindowState> getWindowStates() {
        final Map<Long, WindowState> simpleWindowStates = new LinkedHashMap<Long, WindowState>();
        synchronized (this.windowStates) {
            for (Map.Entry<StylesheetDescriptorImpl, WindowState> windowStateEntry :
                    windowStates.entrySet()) {
                final StylesheetDescriptorImpl stylesheetDescriptor = windowStateEntry.getKey();
                final long stylesheetDescriptorId = stylesheetDescriptor.getId();
                final WindowState windowState = windowStateEntry.getValue();
                simpleWindowStates.put(stylesheetDescriptorId, windowState);
            }
        }
        return Collections.unmodifiableMap(simpleWindowStates);
    }

    @Override
    public WindowState getWindowState(IStylesheetDescriptor stylesheetDescriptor) {
        synchronized (this.windowStates) {
            return this.windowStates.get(stylesheetDescriptor);
        }
    }

    @Override
    public void setWindowState(IStylesheetDescriptor stylesheetDescriptor, WindowState state) {
        synchronized (this.windowStates) {
            if (state == null) {
                this.windowStates.remove(stylesheetDescriptor);
            } else {
                this.windowStates.put((StylesheetDescriptorImpl) stylesheetDescriptor, state);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.portlet.om.IPortletEntity#getPortletPreferences()
     */
    @Override
    public List<IPortletPreference> getPortletPreferences() {
        return portletPreferences.getPortletPreferences();
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.portlet.om.IPortletEntity#setPortletPreferences(java.util.List)
     */
    @Override
    public boolean setPortletPreferences(List<IPortletPreference> portletPreferences) {
        return this.portletPreferences.setPortletPreferences(portletPreferences);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.layoutNodeId == null) ? 0 : this.layoutNodeId.hashCode());
        result =
                prime * result
                        + ((this.portletDefinition == null)
                                ? 0
                                : this.portletDefinition.hashCode());
        result = prime * result + this.userId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!IPortletEntity.class.isAssignableFrom(obj.getClass())) return false;
        IPortletEntity other = (IPortletEntity) obj;
        if (this.layoutNodeId == null) {
            if (other.getLayoutNodeId() != null) return false;
        } else if (!this.layoutNodeId.equals(other.getLayoutNodeId())) return false;
        if (this.portletDefinition == null) {
            if (other.getPortletDefinition() != null) return false;
        } else if (!this.portletDefinition.equals(other.getPortletDefinition())) return false;
        if (this.userId != other.getUserId()) return false;
        return true;
    }

    @Override
    public String toString() {
        return "PortletEntity ["
                + "portletEntityId="
                + this.portletEntityId
                + ", "
                + "layoutNodeId="
                + this.layoutNodeId
                + ", "
                + "userId="
                + this.userId
                + ", "
                + "portletDefinition="
                + this.portletDefinition
                + "]";
    }
}
