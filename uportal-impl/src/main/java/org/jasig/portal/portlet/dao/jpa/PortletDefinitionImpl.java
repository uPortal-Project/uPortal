/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.dao.jpa;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Index;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletPreferences;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Entity
@Table(name = "UP_PORTLET_DEF")
@org.hibernate.annotations.Table(
        appliesTo = "UP_PORTLET_DEF", 
        indexes = {
            @Index(name = "IDX_PORT_DEF__PORT_APP_DD", columnNames = { "PORTLET_APP_ID", "PORTLET_NAME" }),
            @Index(name = "IDX_PORT_DEF__CHAN_DEF", columnNames = "CHANNEL_DEF_ID")
        }
    )
class PortletDefinitionImpl implements IPortletDefinition {
    //Properties are final to stop changes in code, hibernate overrides the final via reflection to set their values
    @Id
    @GeneratedValue
    @Column(name = "PORTLET_DEF_ID")
    private final long internalPortletDefinitionId;

    @Column(name = "CHANNEL_DEF_ID", nullable = false, updatable = false, unique = true)
    private final int channelDefinitionId;

    @Column(name = "PORTLET_APP_ID", length = 512, nullable = false, updatable = false)
    private final String portletApplicaitonId;

    @Column(name = "PORTLET_NAME", length = 512, nullable = false, updatable = false)
    private final String portletName;

    //Hidden reference to the parent portlet definition, used by hibernate for referential integrety
    @SuppressWarnings("unused")
    @OneToMany(mappedBy = "portletDefinition", targetEntity = PortletEntityImpl.class, cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
    private Set<IPortletEntity> portletEntities = null;

    @OneToOne(targetEntity = PortletPreferencesImpl.class, cascade = { CascadeType.ALL })
    @JoinColumn(name = "PORTLET_PREFS_ID", nullable = false)
    @Cascade( { org.hibernate.annotations.CascadeType.DELETE_ORPHAN, org.hibernate.annotations.CascadeType.ALL })
    private IPortletPreferences portletPreferences = null;
    

    @Transient
    private IPortletDefinitionId portletDefinitionId = null;

    
    /**
     * Used to initialize fields after persistence actions.
     */
    @SuppressWarnings("unused")
    @PostLoad
    @PostPersist
    @PostUpdate
    @PostRemove
    private void init() {
        this.portletDefinitionId = new PortletDefinitionIdImpl(this.internalPortletDefinitionId);
    }
    
    
    /**
     * Used by the ORM layer to create instances of the object.
     */
    @SuppressWarnings("unused")
    private PortletDefinitionImpl() {
        this.internalPortletDefinitionId = -1;
        this.channelDefinitionId = -1;
        this.portletApplicaitonId = null;
        this.portletName = null;
        this.portletPreferences = null;
    }
    
    public PortletDefinitionImpl(int channelDefinitionId, String portletApplicaitonId, String portletName) {
        this.internalPortletDefinitionId = -1;
        this.channelDefinitionId = channelDefinitionId;
        this.portletApplicaitonId = portletApplicaitonId;
        this.portletName = portletName;
        this.portletPreferences = new PortletPreferencesImpl();
    }
    

    /* (non-Javadoc)
     * @see org.jasig.portal.om.portlet.IPortletDefinition#getPortletDefinitionId()
     */
    public IPortletDefinitionId getPortletDefinitionId() {
        return this.portletDefinitionId;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.om.portlet.IPortletDefinition#getChannelDefinitionId()
     */
    public int getChannelDefinitionId() {
        return this.channelDefinitionId;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.om.portlet.IPortletDefinition#getPortletApplicationId()
     */
    public String getPortletApplicationId() {
        return this.portletApplicaitonId;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.om.portlet.IPortletDefinition#getPortletName()
     */
    public String getPortletName() {
        return this.portletName;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.om.portlet.IPortletDefinition#getPortletPreferences()
     */
    public IPortletPreferences getPortletPreferences() {
        return this.portletPreferences;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.om.portlet.IPortletDefinition#setPortletPreferences(org.jasig.portal.om.portlet.prefs.IPortletPreferences)
     */
    public void setPortletPreferences(IPortletPreferences portletPreferences) {
        Validate.notNull(portletPreferences, "portletPreferences can not be null");
        this.portletPreferences = portletPreferences;
    }
    
    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof IPortletDefinition)) {
            return false;
        }
        IPortletDefinition rhs = (IPortletDefinition) object;
        return new EqualsBuilder()
            .append(this.channelDefinitionId, rhs.getChannelDefinitionId())
            .append(this.portletApplicaitonId, rhs.getPortletApplicationId())
            .append(this.portletName, rhs.getPortletName())
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(464270933, -1074792143)
            .append(this.channelDefinitionId)
            .append(this.portletApplicaitonId)
            .append(this.portletName)
            .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("portletDefinitionId", this.portletDefinitionId)
            .append("channelDefinitionId", this.channelDefinitionId)
            .append("portletApplicaitonId", this.portletApplicaitonId)
            .append("portletName", this.portletName)
            .toString();
    }
}
