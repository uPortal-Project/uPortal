/**
 * 
 */
package org.jasig.portal.url;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Implementation of {@link IPortalChannelUrl}.
 * Setters will throw IllegalArgumentException for null inputs.
 * 
 * @author Nicholas Blair, nblair@doit.wisc.edu
 *
 */
class PortalChannelUrlImpl extends AbstractPortalUrl implements IPortalChannelUrl {

	/**
	 * 
	 * @param request
	 * @param urlGenerator
	 */
	protected PortalChannelUrlImpl(HttpServletRequest request,
			IUrlGenerator urlGenerator) {
		super(request, urlGenerator);
	}

	private String channelSubscribeId;
	private String fName;
	
	/* (non-Javadoc)
	 * @see org.jasig.portal.url.IPortalChannelUrl#getChannelSubscribeId()
	 */
	public String getChannelSubscribeId() {
		return this.channelSubscribeId;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.url.IPortalChannelUrl#getFName()
	 */
	public String getFName() {
		return fName;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.url.IPortalChannelUrl#setFName(java.lang.String)
	 */
	public void setFName(String fName) {
		Validate.notNull(fName, "fName was null");
		this.fName = fName;
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.url.IPortalChannelUrl#setChannelSubscribeId(java.lang.String)
	 */
	public void setChannelSubscribeId(String channelSubscribeId) {
		Validate.notNull(channelSubscribeId, "channelSubscribeId was null");
		this.channelSubscribeId = channelSubscribeId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object object) {
		if (object == this) {
            return true;
        }
        if (!(object instanceof PortalChannelUrlImpl)) {
            return false;
        }
        PortalChannelUrlImpl rhs = (PortalChannelUrlImpl) object;
        return new EqualsBuilder()
            .appendSuper(super.equals(object))
            .append(this.channelSubscribeId, rhs.channelSubscribeId)
            .append(this.fName, rhs.fName)
            .isEquals();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder(-942791321, 1709261357)
        .appendSuper(super.hashCode())
        .append(this.channelSubscribeId)
        .append(this.fName)
        .toHashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.url.IBasePortalUrl#getUrlString()
	 */
	public String getUrlString() {
		return this.urlGenerator.generateChannelUrl(request, this);
	}

}
