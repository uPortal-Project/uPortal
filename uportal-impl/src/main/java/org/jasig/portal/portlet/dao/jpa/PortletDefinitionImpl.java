/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.channel.dao.jpa.ChannelDefinitionImpl;
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
@GenericGenerator(
        name = "UP_PORTLET_DEF_GEN", 
        strategy = "native", 
        parameters = {
            @Parameter(name = "sequence", value = "UP_PORTLET_DEF_SEQ"),
            @Parameter(name = "table", value = "UP_JPA_UNIQUE_KEY"),
            @Parameter(name = "column", value = "NEXT_UP_PORTLET_DEF_HI")
        }
    )
public class PortletDefinitionImpl implements IPortletDefinition {
    //Properties are final to stop changes in code, hibernate overrides the final via reflection to set their values
    @Id
    @GeneratedValue(generator = "UP_PORTLET_DEF_GEN")
    @Column(name = "PORTLET_DEF_ID")
    private final long internalPortletDefinitionId;

    
    @OneToOne(targetEntity = ChannelDefinitionImpl.class)
    @JoinColumn(name = "CHANNEL_DEF_ID", nullable = false, updatable = false, unique = true)
    private final IChannelDefinition channelDefinition;
    

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
        this.channelDefinition = null;
        this.portletPreferences = null;
    }
    
    public PortletDefinitionImpl(ChannelDefinitionImpl channelDefinition) {
        this.internalPortletDefinitionId = -1;
        this.channelDefinition = channelDefinition;
        this.portletPreferences = new PortletPreferencesImpl();
    }
    

    /* (non-Javadoc)
     * @see org.jasig.portal.om.portlet.IPortletDefinition#getPortletDefinitionId()
     */
    public IPortletDefinitionId getPortletDefinitionId() {
        return this.portletDefinitionId;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletDefinition#getChannelDefinition()
     */
    public IChannelDefinition getChannelDefinition() {
        return this.channelDefinition;
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
            .append(this.channelDefinition.getId(), rhs.getChannelDefinition().getId())
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(464270933, -1074792143)
            .append(this.channelDefinition.getId())
            .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("portletDefinitionId", this.portletDefinitionId)
            .append("channelDefinitionId", this.channelDefinition.getId())
            .toString();
    }
}
