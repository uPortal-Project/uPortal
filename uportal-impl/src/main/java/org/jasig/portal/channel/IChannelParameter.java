/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.channel;

/**
 * IChannelParameter represents an interface for ChannelDefinition parameters.
 * These parameters function as defaults for the channel and may optionally
 * be overridden by end users.
 * 
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @author Jen Bourey, jbourey@unicon.net
 * @revision $Revision$
 */
public interface IChannelParameter {
    
    // Getter methods
    
    /**
     * Get the name of the channel parameter.
     * @return the name of the channel parameter.
     */
    public String getName();
    
    /**
     * Get the default value of the channel parameter.
     * @return the default value for this channel parameter.
     */
    public String getValue();
    
    /**
     * Get whether the value of this channel parameter may be overridden.
     * @return true if value may be overridden, false otherwise.
     */
    public boolean getOverride();
    
    /**
     * Get a description of this channel parameter.
     * @return a description of this channel parameter.
     */
    public String getDescription();

    
    // Setter methods
    
    /**
     * Set the name of the channel parameter.
     * @param name the name of the channel parameter
     */
    public void setName(String name);
    
    /**
     * Set the default value for this channel parameter.
     * @param value the default value for this channel parameter.
     */
    public void setValue(String value);
    
    /**
     * Set whether this channel parameter may be overridden.
     * @param override true if the channel parameter may be overridden.
     */
    public void setOverride(boolean override);
    
    /**
     * Set the description of this channel parameter.
     * @param descr description of this channel parameter.
     */
    public void setDescription(String descr);

}
