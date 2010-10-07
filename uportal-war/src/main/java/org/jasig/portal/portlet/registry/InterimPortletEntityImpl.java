/**
 * Copyright (c) 2000-2010, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.portlet.registry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jasig.portal.portlet.dao.jpa.PortletPreferencesImpl;
import org.jasig.portal.portlet.om.AbstractObjectId;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletPreferences;

/**
 * The purpose of this implementation is two fold. First and foremost it is
 * intended to improve performance by providing an temporary non persistent
 * object for use in rendering the users layout. Secondly this entity exists to
 * avoid further complicating the existing IPortletEntity and IPortletEntityDao
 * implementations.
 * 
 * @author Lennard Fuller
 * @version
 * 
 */
public class InterimPortletEntityImpl implements IPortletEntity {
    private String channelSubscribeId;
    private int userId;
    private IPortletDefinitionId portletDefinitionId;
    private IPortletPreferences portletPreferences = null;
    private IPortletEntityId portletEntityId = null;
    // package access intended <'i'nterim><'p'ortlet><'e'ntity>
    static final String INTERIM_PREFIX = "ipe";
    private static final String INTERIM_ID_PATTERN = "%s_%s_%s_%s";
    private static final char INTERIM_SEPARATOR = '_';

    static class InterimPortletEntityIdImpl extends AbstractObjectId implements
            IPortletEntityId {
        private static final long serialVersionUID = 1L;

        public InterimPortletEntityIdImpl(String portletEntityId) {
            super(portletEntityId);
        }
    }

    static class PortletDefinitionIdImpl extends AbstractObjectId implements
            IPortletDefinitionId {
        private static final long serialVersionUID = 1L;

        /**
         * @param objectId
         */
        public PortletDefinitionIdImpl(long portletDefinitionId) {
            super(Long.toString(portletDefinitionId));
        }
    }

    // package access intended
    static boolean isInterimPortletEntityId(IPortletEntityId portletEntityId) {
        return isInterimPortletEntityId(portletEntityId.getStringId());
    }
    static boolean isInterimPortletEntityId(String portletEntityIdStr) {
        return portletEntityIdStr.startsWith(INTERIM_PREFIX);
    }

    private void initFromIdString(String portletEntityIdStr) {
        String[] vars = StringUtils
                .split(portletEntityIdStr, INTERIM_SEPARATOR);
        this.portletDefinitionId = new PortletDefinitionIdImpl(Long
                .parseLong(vars[1]));
        this.channelSubscribeId = vars[2];
        this.userId = Integer.parseInt(vars[3]);
    }

    public InterimPortletEntityImpl(IPortletEntityId portletEntityId) {
        super();
        initFromIdString(portletEntityId.getStringId());
        this.portletEntityId = portletEntityId;
        this.portletPreferences = new PortletPreferencesImpl();
    }

    public InterimPortletEntityImpl(String portletEntityIdStr) {
        super();
        initFromIdString(portletEntityIdStr);
        this.portletEntityId = new InterimPortletEntityIdImpl(
                portletEntityIdStr);
        this.portletPreferences = new PortletPreferencesImpl();
    }

    public InterimPortletEntityImpl(IPortletDefinitionId portletDefinitionId,
            String channelSubscribeId, int userId) {
        super();
        this.portletDefinitionId = portletDefinitionId;
        this.channelSubscribeId = channelSubscribeId;
        this.userId = userId;
        this.portletEntityId = new InterimPortletEntityIdImpl(String.format(
                INTERIM_ID_PATTERN, INTERIM_PREFIX, portletDefinitionId
                        .getStringId(), channelSubscribeId, userId));
        this.portletPreferences = new PortletPreferencesImpl();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jasig.portal.portlet.om.IPortletEntity#getChannelSubscribeId()
     */
    public String getChannelSubscribeId() {
        return channelSubscribeId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jasig.portal.portlet.om.IPortletEntity#getPortletDefinitionId()
     */
    public IPortletDefinitionId getPortletDefinitionId() {
        return portletDefinitionId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jasig.portal.portlet.om.IPortletEntity#getPortletEntityId()
     */
    public IPortletEntityId getPortletEntityId() {
        return portletEntityId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jasig.portal.portlet.om.IPortletEntity#getPortletPreferences()
     */
    public IPortletPreferences getPortletPreferences() {
        return portletPreferences;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jasig.portal.portlet.om.IPortletEntity#getUserId()
     */
    public int getUserId() {
        return userId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.jasig.portal.portlet.om.IPortletEntity#setPortletPreferences(org.
     * jasig.portal.portlet.om.IPortletPreferences)
     */
    public void setPortletPreferences(IPortletPreferences portletPreferences) {
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
        return new EqualsBuilder().append(this.channelSubscribeId,
                rhs.getChannelSubscribeId()).append(this.userId,
                rhs.getUserId()).append(this.getPortletDefinitionId(),
                rhs.getPortletDefinitionId()).isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(464270933, -1074792143).append(
                this.channelSubscribeId).append(this.userId).append(
                this.getPortletDefinitionId()).toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("portletEntityId", this.portletEntityId).append(
                        "channelSubscribeId", this.channelSubscribeId).append(
                        "userId", this.userId).append("portletDefinitionId",
                        this.getPortletDefinitionId()).toString();
    }
}
