/**
 * Copyright (c) 2001 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.groups;

import java.util.Iterator;

/**
 * An <code>IEntityGroup</code> is a composite <code>IGroupMember</code>.
 * It contains <code>IEntities</code> and other <code>IEntityGroups</code>.
 * <p>
 * A member is always added to or removed from its group, not vice versa.
 * The following methods are used to maintain the group structure in memory:
 * <p>
 *   <code>addMember(IGroupMember gm)</code><br>
 *   <code>removeMember(IGroupMember gm)</code><br>
 * <p>
 * The following methods commit changes in the group structure to the
 * persistent store:
 * <p>
 *   <code>delete()</code> - delete the group and its memberships<br>
 *   <code>update()</code>  - insert or update the group, as appropriate<br>
 *   <code>updateMembers()</code> - insert/update/delete group memberships as appropriate<br>
 *
 * @author Dan Ellentuck
 * @version $Revision$
 */
public interface IEntityGroup extends IGroupMember
{
/**
 * Adds <code>IGroupMember</code> gm to this group, but does not commit it to the
 * data store.  Use <code>updateMembers()</code> to commit memberships to the data store.
 * @param gm org.jasig.portal.groups.IGroupMember
 * @exception GroupsException is thrown if the member is a group and
 * this group already has a group with the same name or if the addition
 * of the group creates a circular reference.
 */
  public void addMember(IGroupMember gm) throws GroupsException;
/**
 * Deletes the <code>IEntityGroup</code> from the data store.
 */
  public void delete() throws GroupsException;
  public String getCreatorID();
  public String getDescription();
  public String getName();
/**
 * Removes the <code>IGroupMember</code> from this group, but does not remove the
 * membership from the data store.
 * @param gm org.jasig.portal.groups.IGroupMember
 */
  public void removeMember(IGroupMember gm);
  public void setCreatorID(String userID);
  public void setDescription(String name);
/**
 * A group name must be unique within any of its containing groups
 * otherwise throw a GroupsException.
 */
  public void setName(String name) throws GroupsException;
/**
 * Commit the <code>IEntityGroup</code> AND ITS MEMBERSHIPS to the data store.
 */
  public void update() throws GroupsException;
/**
 * Commit this <code>IEntityGroup's</code> memberships to the data store.
 */
  public void updateMembers() throws GroupsException;
}
