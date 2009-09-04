/**
 * 
 */
package org.jasig.portal.url;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * Basic implementation of {@link IPortalRequestInfo} - a straightforward java bean.
 * Package private by design - see {@link IPortalUrlProvider} for a means to retrieve
 * an instance.
 * 
 * @author Nicholas Blair, nblair@doit.wisc.edu
 *
 */
class PortalRequestInfoImpl implements IPortalRequestInfo {

    private String targetedChannelSubscribeId;
    private String targetedLayoutNodeId;
    private IPortletWindowId targetedPortletWindowId;
    private UrlState urlState;
    private boolean action;
    
    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalRequestInfo#getTargetedChannelSubscribeId()
     */
    public String getTargetedChannelSubscribeId() {
        return targetedChannelSubscribeId;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalRequestInfo#getTargetedLayoutNodeId()
     */
    public String getTargetedLayoutNodeId() {
        return targetedLayoutNodeId;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalRequestInfo#getTargetedPortletWindowId()
     */
    public IPortletWindowId getTargetedPortletWindowId() {
        return targetedPortletWindowId;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalRequestInfo#getUrlState()
     */
    public UrlState getUrlState() {
        return urlState;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalRequestInfo#isAction()
     */
    public boolean isAction() {
        return action;
    }

    /**
     * @param targetedChannelSubscribeId the targetedChannelSubscribeId to set
     */
    public void setTargetedChannelSubscribeId(String targetedChannelSubscribeId) {
        this.targetedChannelSubscribeId = targetedChannelSubscribeId;
    }

    /**
     * @param targetedLayoutNodeId the targetedLayoutNodeId to set
     */
    public void setTargetedLayoutNodeId(String targetedLayoutNodeId) {
        this.targetedLayoutNodeId = targetedLayoutNodeId;
    }

    /**
     * @param targetedPortletWindowId the targetedPortletWindowId to set
     */
    public void setTargetedPortletWindowId(IPortletWindowId targetedPortletWindowId) {
        this.targetedPortletWindowId = targetedPortletWindowId;
    }

    /**
     * @param urlState the urlState to set
     */
    public void setUrlState(UrlState urlState) {
        this.urlState = urlState;
    }

    /**
     * @param action the action to set
     */
    public void setAction(boolean action) {
        this.action = action;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof PortalRequestInfoImpl)) {
            return false;
        }
        PortalRequestInfoImpl rhs = (PortalRequestInfoImpl) obj;
        return new EqualsBuilder()
            .append(this.targetedChannelSubscribeId, rhs.getTargetedChannelSubscribeId())
            .append(this.targetedLayoutNodeId, rhs.getTargetedLayoutNodeId())
            .append(this.targetedPortletWindowId, rhs.getTargetedPortletWindowId())
            .append(this.urlState, rhs.getUrlState())
            .append(this.action, rhs.isAction())
            .isEquals();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(-917388297, 674832469)
        .append(this.targetedChannelSubscribeId)
        .append(this.targetedLayoutNodeId)
        .append(this.targetedPortletWindowId)
        .append(this.urlState)
        .append(this.action)
        .toHashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(ToStringStyle.SHORT_PREFIX_STYLE)
        .append(this.targetedChannelSubscribeId)
        .append(this.targetedLayoutNodeId)
        .append(this.targetedPortletWindowId)
        .append(this.urlState)
        .append(this.action)
        .toString();
    }

    
}
