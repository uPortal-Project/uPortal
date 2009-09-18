/**
 * 
 */
package org.jasig.portal.url;


/**
 * Subclass of {@link IBasePortalUrl} that adds functions specific to channels.
 * 
 * @author Nicholas Blair, nblair@doit.wisc.edu
 */
public interface IPortalChannelUrl extends IBasePortalUrl {
	
	/**
	 * Set the functional name for the channel this object represents.
	 * 
	 * @param fName
	 */
	public void setFName(String fName);
	
	/**
	 * Get the functional name for the channel this object represents.
	 * 
	 * Cannot return null.
	 * 
	 * @return
	 */
	public String getFName();
	
	/**
	 * Set the channel subscription id for the channel this object represents.
	 * 
	 * @param channelSubscribeId
	 */
	public void setChannelSubscribeId(String channelSubscribeId);
	
	/**
	 * Get the channel subscription id for the channel this object represents.
	 * 
	 * Cannot return null.
	 * 
	 * @return
	 */
	public String getChannelSubscribeId();
}
