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

import java.util.*;

/**
 * An <code>IGroupMember</code> defines the common behavior for both its leaf
 * <code>IEntity</code> and composite <code>IEntityGroup</code> sub-types
 * that together make up a Groups structure.
 * <p>
 * An <code>IGroupMember</code> can answer both its parents and its children but
 * has no api for adding or removing them.  These methods are defined on
 * the composite type, <code>IEntityGroup</code>, because you add a member
 * to a group, not vice versa.
 * <p>
 * Note that an <code>IGroupMember</code> must implement <code>equals()</code>
 * and <code>hashCode()</code> so that duplicates returned from "deep"
 * methods can be recognized.
 *
 * @author Dan Ellentuck
 * @version $Revision$
 */
public interface IGroupMember {
/**
 * Answers if <code>IGroupMember</code> gm is a member of <code>this</code>.
 * @return boolean
 * @param gm org.jasig.portal.groups.IGroupMember
 */
public boolean contains(IGroupMember gm) throws GroupsException;
/**
 * Answers if <code>IGroupMember</code> gm is a recursive member of <code>this</code>.
 * @return boolean
 * @param gm org.jasig.portal.groups.IGroupMember
 */
public boolean deepContains(IGroupMember gm) throws GroupsException;
/**
 * Answers if Object o is an <code>IGroupMember</code> that refers to the same underlying
 * entity(ies) as <code>this</code>.
 * @return boolean
 * @param Object o
 */
public boolean equals(Object o);
/**
 * Returns an <code>Iterator</code> over the <code>Set</code> of this
 * <code>IGroupMember's</code> recursively-retrieved parent groups.
 *
 * @return java.util.Iterator
 */
public Iterator getAllContainingGroups() throws GroupsException;
/**
 * Returns an <code>Iterator</code> over the <code>Set</code> of this
 * <code>IGroupMember's</code> recursively-retrieved members that are
 * <code>IEntities</code>.
 * @return java.util.Iterator
 */
public Iterator getAllEntities() throws GroupsException;
/**
 * Returns an <code>Iterator</code> over the <code>Set</code> of recursively-retrieved
 * <code>IGroupMembers</code> that are members of <code>this</code>.
 * @return java.util.Iterator
 */
public Iterator getAllMembers() throws GroupsException;
/**
 * Returns an <code>Iterator</code> over this <code>IGroupMember's</code> parent groups.
 * @return java.util.Iterator
 */
public Iterator getContainingGroups() throws GroupsException;
/**
 * Returns an <code>Iterator</code> over this <code>IGroupMember's</code>
 * members that are <code>IEntities</code>.
 * @return java.util.Iterator
 */
public Iterator getEntities() throws GroupsException;
/**
 * Returns the <code>entityType</code> of the underlying entities.  This is analagous to
 * <code>Class</code>, as applied to <code>Arrays</code> and their elements.
 *
 * @return java.lang.Class
 */
public Class getEntityType();
/**
 * @return String
 */
String getKey();
/**
 * Returns the named <code>IEntityGroup</code> from our members <code>Collection</code>.
 * @return org.jasig.portal.groups.IEntityGroup
 * @param name java.lang.String
 */
public IEntityGroup getMemberGroupNamed(String name) throws GroupsException;
/**
 * Returns an <code>Iterator</code> over the <code>IGroupMembers</code> in our
 * member <code>Collection</code>.
 * @return java.util.Iterator
 */
public Iterator getMembers() throws GroupsException;
/**
 * @return int
 */
public int hashCode();
/**
 * Answers if this <code>IGroupMember</code> has any members.
 * @return boolean
 */
public boolean hasMembers() throws GroupsException;
/**
 * Answers if <code>this</code> is a recursive member of <code>IGroupMember</code> gm.
 * @return boolean
 * @param gm org.jasig.portal.groups.IGroupMember
 */
public boolean isDeepMemberOf(IGroupMember gm) throws GroupsException;
/**
 * @return boolean
 */
public boolean isEntity();
/**
 * @return boolean
 */
public boolean isGroup();
/**
 * Answers if <code>this</code> is a member of <code>IGroupMember</code> gm.
 * @return boolean
 * @param gm org.jasig.portal.groups.IGroupMember
 */
public boolean isMemberOf(IGroupMember gm) throws GroupsException;
}
