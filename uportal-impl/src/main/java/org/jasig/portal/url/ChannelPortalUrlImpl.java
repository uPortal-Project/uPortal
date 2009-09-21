/**
 * 
 */
package org.jasig.portal.url;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Implementation of {@link IChannelPortalUrl}.
 * Setters will throw IllegalArgumentException for null inputs.
 * 
 * @author Nicholas Blair, nblair@doit.wisc.edu
 *
 */
class ChannelPortalUrlImpl extends PortletPortalUrlImpl implements IChannelPortalUrl {

	private final String channelSubscribeId;
	private final String fName;
	private final IUrlGenerator urlGenerator;
	private final HttpServletRequest request;
	private boolean worker = false;
	
	/**
	 * 
	 * @param request
	 * @param urlGenerator
	 */
	protected ChannelPortalUrlImpl(HttpServletRequest request,
			IUrlGenerator urlGenerator, String channelSubscribeId, String fName) {
		super(request, urlGenerator, channelSubscribeId);
		this.channelSubscribeId = channelSubscribeId;
		this.fName = fName;
		this.urlGenerator = urlGenerator;
		this.request = request;
	}

	
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

	/* (non-Javadoc)
	 * @see org.jasig.portal.url.IPortalChannelUrl#isWorker()
	 */
	public boolean isWorker() {
		return this.worker;
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.url.IPortalChannelUrl#setWorker(boolean)
	 */
	public void setWorker(boolean worker) {
		this.worker = worker;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object object) {
		if (object == this) {
            return true;
        }
        if (!(object instanceof ChannelPortalUrlImpl)) {
            return false;
        }
        ChannelPortalUrlImpl rhs = (ChannelPortalUrlImpl) object;
        return new EqualsBuilder()
            .appendSuper(super.equals(object))
            .append(this.channelSubscribeId, rhs.channelSubscribeId)
            .append(this.fName, rhs.fName)
            .append(this.worker, rhs.worker)
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
        .append(this.worker)
        .toHashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.url.IBasePortalUrl#getUrlString()
	 */
	public String getUrlString() {
		return this.urlGenerator.generateChannelUrl(request, this);
	}
	
	/* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.getUrlString();
    }


}
