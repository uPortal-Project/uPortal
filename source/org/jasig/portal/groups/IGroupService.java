/**
 * Copyright (c) 2001, 2002 The JA-SIG Collaborative.  All rights reserved.
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

/**
 * Defines an api for discovering an entry point into the groups system,
 * represented by an <code>IGroupMember</code>.  This is analogous to getting an
 * <code>InitialContext</code> in JNDI.  Subsequent requests for navigating or
 * maintaining groups go thru the <code>IGroupMember</code>.
 *
 * @author Dan Ellentuck
 * @version $Revision$
 */

public interface IGroupService {
  /*
   * Returns a pre-existing <code>IEntityGroup</code> or null if the
   * <code>IGroupMember</code> does not exist.
   */
  public IEntityGroup findGroup(String key) throws GroupsException;

  /*
   * Returns a pre-existing <code>IGroupMember</code> or null if the
   * <code>IGroupMember</code> does not exist.  The <code>IGroupMember</code>
   * can be either an <code>IEntityGroup</code> or an <code>IEntity</code>.
   */
  public IGroupMember findGroupMember(String key, Class type)
    throws GroupsException;

  /*
   * Refers to the security.properties file to get the key for the group
   * Everyone and asks the group store implementation for the corresponding
   * <code>IEntityGroup</code>.
   */
  public IEntityGroup getEveryoneGroup() throws GroupsException;

  /*
   * Returns a new <code>IEntityGroup</code> for the given Class with an unused
   * key.
   */
  public IEntityGroup newGroup(Class type) throws GroupsException;
}