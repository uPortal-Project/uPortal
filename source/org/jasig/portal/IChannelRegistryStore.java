/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
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
 *
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
 */


package  org.jasig.portal;

import org.jasig.portal.security.IPerson;
import  org.w3c.dom.*;


/**
 * Interface defining how the portal retrieves it's channels and categories.
 * Methods are also provided to allow for publishing and unpublishing content.
 * The intent is that this task can be performed based on channel, category, and role.
 * @author John Laker
 * @version $Revision$
 */
public interface IChannelRegistryStore {

  /**
   * put your documentation comment here
   * @return
   * @exception PortalException
   */
  public int getNextId () throws PortalException;



  /**
   * Gets the channel registry as an XML document
   * @return the channel registry XML
   * @throws java.lang.Exception
   */
  public Document getChannelRegistryXML (IPerson person) throws Exception;



  /**
   * put your documentation comment here
   * @param registryXML
   */
  public void setRegistryXML (String registryXML);



  /**
   * Returns a string of XML which describes the channel types.
   * @return channelTypes, the list of publishable channel types
   * @throws java.lang.Exception
   */
  public Document getChannelTypesXML () throws Exception;



  /**
   * put your documentation comment here
   * @param role
   * @return
   */
  public Document getCategoryXML (String role);



  /**
   * Removes a channel from the channel registry.
   * @param chanID, the ID of the channel to remove.
   * @exception Exception
   */
  public void removeChannel (String chanID) throws Exception;



  /**
   * Publishes a channel.
   * @param id the identifier for the channel
   * @param publisherId the identifier for the user who is publishing this channel
   * @param chanXML XML that describes the channel
   * @param catID an array of category IDs
   * @exception Exception
   */
  public void addChannel (int id, int publisherId, Document chanXML, String catID[]) throws Exception;

  /**
   * Approves a channel.
   * @param chanId
   * @param approverId
   * @param approveDate
   * @exception Exception
   */
  public void approveChannel(int chanId, int approverId, java.sql.Timestamp approveDate) throws Exception;
}



