/**
 * Copyright © 2002 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jasig.portal;

import org.jasig.portal.security.IPerson;
import java.util.Date;

/**
 * Interface defining how the portal reads and writes its channel types,
 * definitions, and categories.
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public interface IChannelRegistryStore {

  /**
   * Get the channel type associated with a particular identifier.
   * @param channelTypeId, the channel type identifier
   * @return channelType, the channel type
   * @throws java.sql.SQLException
   */
  public ChannelType getChannelType(int channelTypeId) throws Exception;

  /**
   * Returns an array of ChannelTypes.
   * @return channelTypes, the list of publishable channel types
   * @throws java.lang.Exception
   */
  public ChannelType[] getChannelTypes() throws Exception;

  /**
   * Registers a channel type.
   * @param chanType a channel type
   * @throws java.lang.Exception
   */
  public void addChannelType(ChannelType chanType) throws Exception;

  /**
   * Deletes a channel type.  The deletion will only succeed if no existing
   * channels reference the channel type.
   * @param chanType a channel type
   * @throws java.sql.SQLException
   */
  public void deleteChannelType(ChannelType chanType) throws Exception;

  /**
   * Get a channel definition.
   * @param channelPublishId a channel publish ID
   * @return channelDefinition, a definition of the channel
   * @throws java.lang.Exception
   */
  public ChannelDefinition getChannelDefinition(int channelPublishId) throws Exception;

  /**
   * Publishes a channel.
   * @param channelDef the channel definition
   * @param categories the categories of which this channel should be a member
    * @throws java.lang.Exception
   */
  public void addChannelDefinition(ChannelDefinition channelDef, ChannelCategory[] categories) throws Exception;

  /**
   * Permanently deletes a channel definition from the store.
   * @param channelDef the channel definition
   * @throws java.lang.Exception
   */
  public void deleteChannelDefinition(ChannelDefinition channelDef) throws Exception;

  /**
   * Sets a channel definition as "approved".  This effectively makes a
   * channel definition available in the channel registry, making the channel
   * available for subscription.
   * @param channelDef the channel definition
   * @param approver the user that approves this channel definition
   * @param approveDate the date when the channel definition should be approved (can be future dated)
   * @throws java.lang.Exception
   */
  public void approveChannelDefinition(ChannelDefinition channelDef, IPerson approver, Date approveDate) throws Exception;


  /**
   * Sets a channel definition as "unapproved".  This effectively removes a
   * channel definition from the channel registry, making the channel
   * unavailable for subscription.
   * @param channelDef the channel definition
   * @throws java.lang.Exception
   */
  public void disapproveChannelDefinition(ChannelDefinition channelDef) throws Exception;

  /**
   * Associates a channel definition with a category.
   * @param channelDef, the channel definition
   * @param category, the channel category to which to associate the channel definition
   * @throws org.jasig.portal.PortalException
   */
  public void addChannelToCategory(ChannelDefinition channelDef, ChannelCategory category) throws Exception;

  /**
   * Disassociates a channel definition from a category.
   * @param channelDef, the channel definition
   * @param category, the channel category from which to disassociate the channel definition
   * @throws org.jasig.portal.PortalException
   */
  public void removeChannelFromCategory(ChannelDefinition channelDef, ChannelCategory category) throws Exception;

}







