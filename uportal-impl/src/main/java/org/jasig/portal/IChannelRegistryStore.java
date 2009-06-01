/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal;

import java.util.Date;
import java.util.List;

import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.channel.IChannelType;
import org.jasig.portal.security.IPerson;

/**
 * Interface defining how the portal reads and writes its channel types,
 * definitions, and categories.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public interface IChannelRegistryStore {

  /**
   * Creates a new channel type.
   * @return the new channel type
   * @throws java.lang.Exception
   */
  public IChannelType newChannelType();

  /**
   * Get the channel type associated with a particular identifier.
   * @param channelTypeId the channel type identifier
   * @return channelType the channel type
   * @throws java.lang.Exception
   */
  public IChannelType getChannelType(int channelTypeId);

  /**
   * Returns an array of ChannelTypes.
   * @return the list of publishable channel types
   * @throws java.lang.Exception
   */
  public List<IChannelType> getChannelTypes();

  /**
   * Persists a channel type.
   * @param chanType a channel type
   * @throws java.lang.Exception
   */
  public IChannelType saveChannelType(IChannelType chanType);

  /**
   * Deletes a channel type.  The deletion will only succeed if no existing
   * channels reference the channel type.
   * @param chanType a channel type
   * @throws java.lang.Exception
   */
  public void deleteChannelType(IChannelType chanType);

  /**
   * Create a new ChannelDefinition object.
   * @return the new channel definition
   * @throws java.lang.Exception
   */
  public IChannelDefinition newChannelDefinition();

  /**
   * Get a channel definition.
   * @param channelPublishId a channel publish ID
   * @return a definition of the channel or <code>null</code> if no matching channel definition can be found
   * @throws java.lang.Exception
   */
  public IChannelDefinition getChannelDefinition(int channelPublishId);

  /**
   * Get a channel definition.  If there is more than one channel definition
   * with the given functional name, then the first one will be returned.
   * @param channelFunctionalName a channel functional name
   * @return a definition of the channel or <code>null</code> if no matching channel definition can be found
   * @throws java.lang.Exception
   */
  public IChannelDefinition getChannelDefinition(String channelFunctionalName);

  /**
   * Get all channel definitions including ones that haven't been approved.
   * @return channelDefs, the channel definitions
   * @throws java.lang.Exception
   */
  public List<IChannelDefinition> getChannelDefinitions();

  /**
   * Persists a channel definition.
   * @param channelDef the channel definition
   * @throws java.lang.Exception
   */
  public void saveChannelDefinition(IChannelDefinition channelDef);

  /**
   * Permanently deletes a channel definition from the store.
   * @param channelDef the channel definition
   * @throws java.lang.Exception
   */
  public void deleteChannelDefinition(IChannelDefinition channelDef);

  /**
   * Sets a channel definition as "approved".  This effectively makes a
   * channel definition available in the channel registry, making the channel
   * available for subscription.
   * @param channelDef the channel definition
   * @param approver the user that approves this channel definition
   * @param approveDate the date when the channel definition should be approved (can be future dated)
   * @throws java.lang.Exception
   */
  public void approveChannelDefinition(IChannelDefinition channelDef, IPerson approver, Date approveDate);


  /**
   * Sets a channel definition as "unapproved".  This effectively removes a
   * channel definition from the channel registry, making the channel
   * unavailable for subscription.
   * @param channelDef the channel definition
   * @throws java.lang.Exception
   */
  public void disapproveChannelDefinition(IChannelDefinition channelDef);

  /**
   * Creates a new channel category.
   * @return the new channel category
   * @throws java.lang.Exception
   */
  public ChannelCategory newChannelCategory();

  /**
   * Creates a new channel category with the specified values.
   * @param name the name of the category
   * @param description the name of the description
   * @param creatorId the id of the creator or system
   * @return channelCategory the new channel category
   * @throws java.lang.Exception
   */
  public ChannelCategory newChannelCategory( String name,
                                             String description,
                                             String creatorId )
     ;

  /**
   * Gets an existing channel category.
   * @param channelCategoryId the id of the category to get
   * @return the channel category
   * @throws java.lang.Exception
   */
  public ChannelCategory getChannelCategory(String channelCategoryId);

  /**
   * Gets top level channel category
   * @return the new channel category
   * @throws java.lang.Exception
   */
  public ChannelCategory getTopLevelChannelCategory();

  /**
   * Recursively gets all child channel categories for a parent category.
   * @return channelCategories the children categories
   * @throws java.lang.Exception
   */
  public ChannelCategory[] getAllChildCategories(ChannelCategory parent);

  /**
   * Recursively gets all child channel definitions for a parent category.
   * @return channelDefinitions the children channel definitions
   * @throws java.lang.Exception
   */
  public IChannelDefinition[] getAllChildChannels(ChannelCategory parent);

  /**
   * Gets all child channel categories for a parent category.
   * @return channelCategories the children categories
   * @throws java.lang.Exception
   */
  public ChannelCategory[] getChildCategories(ChannelCategory parent);

  /**
   * Gets all child channel definitions for a parent category.
   * @return channelDefinitions the children channel definitions
   * @throws java.lang.Exception
   */
  public IChannelDefinition[] getChildChannels(ChannelCategory parent);

  /**
   * Gets the immediate parent categories of this category.
   * @return parents, the parent categories.
   * @throws java.lang.Exception
   */
  public ChannelCategory[] getParentCategories(ChannelCategory child);

  /**
   * Gets the immediate parent categories of this channel definition.
   * @return the parent categories.
   * @throws java.lang.Exception
   */
  public ChannelCategory[] getParentCategories(IChannelDefinition child);

  /**
   * Persists a channel category.
   * @param category the channel category to persist
   * @throws java.lang.Exception
   */
  public void saveChannelCategory(ChannelCategory category);

  /**
   * Deletes a channel category.
   * @param category the channel category to delete
   * @throws java.lang.Exception
   */
  public void deleteChannelCategory(ChannelCategory category);

  /**
   * Makes one category a child of another.
   * @param source the source category
   * @param destination the destination category
   * @throws java.lang.Exception
   */
  public void addCategoryToCategory(ChannelCategory source, ChannelCategory destination);

  /**
   * Makes one category a child of another.
   * @param child the category to remove
   * @param parent the category to remove from
   * @throws java.lang.Exception
   */
  public void removeCategoryFromCategory(ChannelCategory child, ChannelCategory parent);

  /**
   * Associates a channel definition with a category.
   * @param channelDef the channel definition
   * @param category the channel category to which to associate the channel definition
   * @throws java.lang.Exception
   */
  public void addChannelToCategory(IChannelDefinition channelDef, ChannelCategory category);

  /**
   * Disassociates a channel definition from a category.
   * @param channelDef the channel definition
   * @param category the channel category from which to disassociate the channel definition
   * @throws java.lang.Exception
   */
  public void removeChannelFromCategory(IChannelDefinition channelDef, ChannelCategory category);

}







