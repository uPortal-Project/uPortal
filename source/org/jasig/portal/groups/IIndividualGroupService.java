/* Copyright © 2002 The JA-SIG Collaborative.  All rights reserved.
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
 * Defines a component group service that finds and maintains
 * <code>IGroupMembers</code> within a composite group service.
 *
 * @author Dan Ellentuck
 * @version $Revision$
 */
public interface IIndividualGroupService extends ICompositeGroupService {
  /**
   * Removes the <code>IEntityGroup</code> from the store.
   */
  public void deleteGroup(IEntityGroup group) throws GroupsException;
  /**
   * Returns a preexisting <code>IEntityGroup</code> from the store.
   * @param ent CompositeEntityIdentifier
   */
  public IEntityGroup findGroup(CompositeEntityIdentifier ent)
  throws GroupsException;
  /**
   * Returns an <code>Iterator</code> over the members of <code>group</code>.
   * @param group IEntityGroup
   */
  public Iterator findMembers(IEntityGroup group) throws GroupsException;
  /**
   * Answers if the group can be updated or deleted in the store.
   * @param group IEntityGroup
   */
  public boolean isEditable(IEntityGroup group) throws GroupsException;
  /**
   * Answers if the service can be updated by the portal.
   */
  public boolean isEditable();
  /**
   * Returns a new <code>IEntityGroup</code> for the given Class with an unused
   * key.
   */
  public IEntityGroup newGroup(Class type) throws GroupsException;
  /**
   * Commits the updated <code>IEntityGroup</code> and its memberships to the
   * store.
   * @param group IEntityGroup
   */
  public void updateGroup(IEntityGroup group) throws GroupsException;
  /**
   * Commits the updated group memberships to the store.
   * @param group IEntityGroup
   */
  public void updateGroupMembers(IEntityGroup group) throws GroupsException;
}
