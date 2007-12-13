/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.mock.portlet.om;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletPreferences;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class MockPortletDefinition implements IPortletDefinition {
    private int channelDefinitionId;
    private String portletApplicaitonId;
    private String portletName;
    private IPortletPreferences portletPreferences = null;
    private IPortletDefinitionId portletDefinitionId = null;

    
    public MockPortletDefinition() {
        this.channelDefinitionId = -1;
        this.portletApplicaitonId = null;
        this.portletName = null;
        this.portletPreferences = null;
    }
    
    public MockPortletDefinition(IPortletDefinitionId portletDefinitionId, int channelDefinitionId, String portletApplicaitonId, String portletName) {
        this.portletDefinitionId = portletDefinitionId;
        this.channelDefinitionId = channelDefinitionId;
        this.portletApplicaitonId = portletApplicaitonId;
        this.portletName = portletName;
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
        this.portletPreferences = portletPreferences;
    }
    /**
     * @return the portletApplicaitonId
     */
    public String getPortletApplicaitonId() {
        return this.portletApplicaitonId;
    }

    /**
     * @param portletApplicaitonId the portletApplicaitonId to set
     */
    public void setPortletApplicaitonId(String portletApplicaitonId) {
        this.portletApplicaitonId = portletApplicaitonId;
    }

    /**
     * @param channelDefinitionId the channelDefinitionId to set
     */
    public void setChannelDefinitionId(int channelDefinitionId) {
        this.channelDefinitionId = channelDefinitionId;
    }

    /**
     * @param portletName the portletName to set
     */
    public void setPortletName(String portletName) {
        this.portletName = portletName;
    }

    /**
     * @param portletDefinitionId the portletDefinitionId to set
     */
    public void setPortletDefinitionId(IPortletDefinitionId portletDefinitionId) {
        this.portletDefinitionId = portletDefinitionId;
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
