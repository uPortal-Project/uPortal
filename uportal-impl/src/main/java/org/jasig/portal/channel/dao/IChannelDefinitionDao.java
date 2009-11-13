/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.channel.dao;

import java.util.List;

import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.channel.IChannelType;

/**
 * Provides APIs for creating, storing and retrieving {@link IChannelDefinition} objects.
 * 
 * @version $Revision$
 */
public interface IChannelDefinitionDao {
    /**
     * Creates, initializes and persists a new {@link IChannelDefinition} based on the specified parameters
     * 
     * @param channelType The type the channel is based on
     * @param fname A unique, human-readable key for the channel. Must match the regular expression {@link org.jasig.portal.dao.usertype.FunctionalNameType#VALID_FNAME_PATTERN}
     * @param clazz A fully qualified Java class name that implements the {@link org.jasig.portal.IChannel} interface. This is the class the portal will execute when rendering the channel.
     * @param name The display name for the channel, this is shown in administrative UIs
     * @param title The title used on portal generated chrome
     * 
     * @return A newly created, initialized and persisted {@link IChannelDefinition}
     * @throws org.springframework.dao.DataIntegrityViolationException If a IChannelDefinition already exists for the provide arguments
     * @throws IllegalArgumentException If any of the parameters are null
     */
    public IChannelDefinition createChannelDefinition(IChannelType channelType, String fname, String clazz, String name, String title);

    /**
     * Persists changes to a {@link IChannelDefinition}.
     * 
     * @param definition The channel definition to store the changes for
     * @throws IllegalArgumentException if definition is null.
     */
    public IChannelDefinition updateChannelDefinition(IChannelDefinition definition);
	
    /**
     * Removes the specified {@link IChannelDefinition} from the persistent store.
     * 
     * @param definition The definition to remove.
     * @throws IllegalArgumentException if definition is null.
     */
    public void deleteChannelDefinition(IChannelDefinition definition);
	
    /**
     * Get a {@link IChannelDefinition} for the specified id.
     * 
     * @param id The id to get the definition for.
     * @return The channel definition for the id, null if no definition exists for the id.
     */
    public IChannelDefinition getChannelDefinition(int id);
	
    /**
     * Get a {@link IChannelDefinition} for the specified functional name
     * 
     * @param fname The fname to get the definition for.
     * @return The channel definition for the fname, null if no definition exists for the fname.
     * @throws IllegalArgumentException if fname is null.
     */
    public IChannelDefinition getChannelDefinition(String fname);
    
    /**
     * Get a {@link IChannelDefinition} for the specified channel name.
     * 
     * @param name The name to get the definition for.
     * @return THe channel definition for the name, null if no definition exists for the name.
     * @throws IllegalArgumentException if the name is null.
     */
    public IChannelDefinition getChannelDefinitionByName(String name);
	
	/**
	 * @return A {@link List} of all persisted {@link IChannelDefinition}s
	 */
	public List<IChannelDefinition> getChannelDefinitions();
}
