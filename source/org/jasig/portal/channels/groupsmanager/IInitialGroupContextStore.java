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
 */

package  org.jasig.portal.channels.groupsmanager;

import  java.sql.Timestamp;
import org.jasig.portal.groups.IGroupMember;
/**
 * Defines the interface for an Initial Groups Context Store object to be used
 * by CGroupssManager. This will be used to make the stores swappable through an
 * entry in portal.properties. The reference implementation provided by
 * Columbia is for an RDBM Store. The RDBMInitialGroupContextStore is hardcoded
 * in InitialGroupsContextImpl.getFactory() at this point.
 */

public interface IInitialGroupContextStore {

   /**
    * Persistently delete the Initial Groups Context
    * @param igc
    * @exception ChainedException
    */
   void delete (IInitialGroupContext igc) throws ChainedException;

   /**
    * Find the inital contexts  with this ownerID.
    * @return java.util.Iterator
    * @param String creatorID
    */
   java.util.Iterator findInitialGroupContextsForOwner (IGroupMember owner) throws ChainedException;

   /**
    * Find and return an instance of the inital group context.
    * @return org.jasig.portal.channels.groupsmanager.IInitialGroupContext
    * @param String ownerID
    * @param String groupID
    */
   IInitialGroupContext find (String ownerID, String groupID) throws ChainedException;

   /**
    * Returns a new instance of IInitialGroupsContext
    * @param ownerID
    * @param ownerType g=group, p=person
    * @param groupID The id of the associated group.
    * @param ordinal Used to display the initial group contexts in a specified order.
    * @param expanded Indicates whether or not the inital group context will be expanded when the gui is first displayed.
    *  *
    * @return org.jasig.portal.groups.IEntityGroup
    *  *
    * @throws GroupsManagerException
    *  *
    */
   IInitialGroupContext newInstance (String ownerID, String ownerType, String groupID,
         int ordinal, boolean expanded, Timestamp dateCreated) throws ChainedException;

   /**
    * Commits changes made to an Initial Group Context to the database.
    * @param igc The Initial Group Context to be committed.
    *  *
    * @throws GroupsManagerException
    */
   void update (IInitialGroupContext igc) throws ChainedException;
}
