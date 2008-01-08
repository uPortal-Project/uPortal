/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.dao.jpa;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

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
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletPreferences;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Entity
@Table(
        name = "UP_PORTLET_ENT", 
        uniqueConstraints = @UniqueConstraint(columnNames = { "CHANNEL_SUB_ID", "USER_ID" })
    )
@org.hibernate.annotations.Table(
        appliesTo = "UP_PORTLET_ENT", 
        indexes = @Index(name = "IDX_PORT_END__USR_CHAN_SUB", columnNames = { "CHANNEL_SUB_ID", "USER_ID" })
    )
class PortletEntityImpl implements IPortletEntity {
    //Properties are final to stop changes in code, hibernate overrides the final via reflection to set their values
    @Id
    @GeneratedValue
    @Column(name = "PORTLET_ENT_ID")
    private final long internalPortletEntityId;

    @Column(name = "CHANNEL_SUB_ID", nullable = false, updatable = false)
    private final String channelSubscribeId;

    @Column(name = "USER_ID", nullable = false, updatable = false)
    private final int userId;

    //Hidden reference to the parent portlet definition, used by hibernate for referential integrety
    @ManyToOne(targetEntity = PortletDefinitionImpl.class, cascade = { CascadeType.ALL })
    @JoinColumn(name = "PORTLET_DEF_ID", nullable = false)
    private final IPortletDefinition portletDefinition;

    @OneToOne(targetEntity = PortletPreferencesImpl.class, cascade = { CascadeType.ALL })
    @JoinColumn(name = "PORTLET_PREFS_ID", nullable = false)
    @Cascade( { org.hibernate.annotations.CascadeType.DELETE_ORPHAN, org.hibernate.annotations.CascadeType.ALL })
    private IPortletPreferences portletPreferences = null;
    

    @Transient
    private IPortletEntityId portletEntityId = null;
    
    
    /**
     * Used to initialize fields after persistence actions.
     */
    @SuppressWarnings("unused")
    @PostLoad
    @PostPersist
    @PostUpdate
    @PostRemove
    private void init() {
        this.portletEntityId = new PortletEntityIdImpl(this.internalPortletEntityId);
    }
    
    /**
     * Used by the ORM layer to create instances of the object.
     */
    @SuppressWarnings("unused")
    private PortletEntityImpl() {
        this.internalPortletEntityId = -1;
        this.portletDefinition = null;
        this.channelSubscribeId = null;
        this.userId = -1;
        this.portletPreferences = null;
    }
    
    public PortletEntityImpl(IPortletDefinition portletDefinition, String channelSubscribeId, int userId) {
        this.internalPortletEntityId = -1;
        this.portletDefinition = portletDefinition;
        this.channelSubscribeId = channelSubscribeId;
        this.userId = userId;
        this.portletPreferences = new PortletPreferencesImpl();
    }


    /* (non-Javadoc)
     * @see org.jasig.portal.om.portlet.IPortletEntity#getPortletEntityId()
     */
    public IPortletEntityId getPortletEntityId() {
        return this.portletEntityId;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.om.portlet.IPortletEntity#getPortletDefinitionId()
     */
    public IPortletDefinitionId getPortletDefinitionId() {
        return this.portletDefinition.getPortletDefinitionId();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.om.portlet.IPortletEntity#getChannelSubscribeId()
     */
    public String getChannelSubscribeId() {
        return this.channelSubscribeId;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.om.portlet.IPortletEntity#getUserId()
     */
    public int getUserId() {
        return this.userId;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.om.portlet.IPortletEntity#getPortletPreferences()
     */
    public IPortletPreferences getPortletPreferences() {
        return this.portletPreferences;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.om.portlet.IPortletEntity#setPortletPreferences(org.jasig.portal.om.portlet.prefs.IPortletPreferences)
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
        if (!(object instanceof IPortletEntity)) {
            return false;
        }
        IPortletEntity rhs = (IPortletEntity) object;
        return new EqualsBuilder()
            .append(this.channelSubscribeId, rhs.getChannelSubscribeId())
            .append(this.userId, rhs.getUserId())
            .append(this.getPortletDefinitionId(), rhs.getPortletDefinitionId())
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(464270933, -1074792143)
            .append(this.channelSubscribeId)
            .append(this.userId)
            .append(this.getPortletDefinitionId())
            .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("portletEntityId", this.portletEntityId)
            .append("channelSubscribeId", this.channelSubscribeId)
            .append("userId", this.userId)
            .append("portletDefinitionId", this.getPortletDefinitionId())
            .toString();
    }
}
