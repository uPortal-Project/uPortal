package org.jasig.portal.channel;

/**
 * A channel type references a particular java class that implements the
 * IChannel interface. It also references a channel publishing document that
 * describes the parameters that must be fed to the channel.
 * 
 * @author Ken Weiner, kweiner@unicon.net
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public interface IChannelType {

	// Getter methods
	
	/**
	 * Get the unique ID of this channel type.
	 * 
	 * @return unique id
	 */
	public int getId();

	/**
	 * Get the canonical name of the Java class associated with this channel type.
	 * 
	 * @return class name
	 */
	public String getJavaClass();

	/**
	 * Get the name of this channel type
	 * 
	 * @return channel type name
	 */
	public String getName();

	/**
	 * Get a description of this channel type
	 * 
	 * @return channel type description
	 */
	public String getDescription();

	/**
	 * Get the URI of the ChannelPublishingDocument associated with this
	 * channel type.  This CPD will be used to determine configuration options 
	 * for channels of this type.
	 * 
	 * @return ChannelPublishingDocument URI
	 */
	public String getCpdUri();

	
	// Setter methods
	
	/**
	 * Set the associated Java class's canonical name
	 * 
	 * @return Java classname
	 */
	public void setJavaClass(String javaClass);

	/**
	 * Set the description for this channel type
	 * 
	 * @param description
	 */
	public void setDescription(String descr);

	/**
	 * Set the URI of the ChannelPublishingDocument associated with this channel
	 * type.  This CPD will be used to determine configuration options for 
	 * channels of this type.
	 * 
	 * @param ChannelPublishingDocument URI
	 */
	public void setCpdUri(String cpdUri);

}