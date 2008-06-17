/* Copyright 2001, 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.groups;

import java.util.Iterator;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.IBasicEntity;

/**
 * An <code>IGroupMember</code> defines common behavior for both the leaf
 * <code>IEntity</code> and composite <code>IEntityGroup</code> sub-types
 * that together make up a Groups structure.
 * <p>
 * An <code>IGroupMember</code> can answer both its parents and its children but
 * has no api for adding or removing them.  These methods are defined on
 * the composite type, <code>IEntityGroup</code>, since you add a member to a
 * group, and not vice versa.
 * <p>
 * Because it extends <code>IBasicEntity</code>, an <code>IGroupMember</code> has
 * an <code>EntityIdentifier</code> that can be used to cache and lock it.  A leaf
 * <code>IGroupMember</code> also has a separate <code>EntityIdentifier</code> for
 * its underlying entity.  This second <code>EntityIdentifier</code> is used to
 * create and record group memberships.  In the case of a composite (non-leaf)
 * <code>IGroupMember</code>, both <code>EntityIdentifiers</code> are the same.
 * <p>
 * Take care to implement <code>equals()</code> and <code>hashCode()</code> so
 * that duplicates returned from "deep" methods can  be recognized.
 *
 * @author Dan Ellentuck
 * @version $Revision$
 */
public interface IGroupMember extends IBasicEntity {
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
 * @param o
 * @return boolean
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
 * Returns the underlying entity type.  For an <code>IEntityGroup</code>, this is
 * analagous to <code>Class</code> as applied to an <code>Array</code>; it is an
 * attribute of the group object.  For an <code>IEntity</code>, it is the entity
 * type of the group the entity belongs to, which may be any <code>Class</code>
 * the underlying entity can be legally cast to.  Thus, an <code>IEntity</code>
 * with an underlying entity of type <code>Manager</code> could have an entity
 * type of <code>Employee</code> as long as <code>Employee</code> was a
 * superclass of <code>Manager</code>.
 *
 * @return java.lang.Class
 */
public Class getEntityType();
/**
 * Returns the key of the underlying entity.
 * @return String
 */
public String getKey();
/**
 * @see #getEntityType()
 */
  public Class getLeafType();
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
 * Returns the type of the underlying entity.  For a group this will be
 * <code>IEntityGroup</code>.  For an entity, it will be the type of the
 * underlying <code>EntityIdentifier</code>.
 *
 * @return java.lang.Class
 */
public Class getType();
/**
 * Returns <code>EntityIdentifier</code> for this <code>IGroupMember's</code>
 * underlying entity.  In the case of an <code>IEntityGroup</code>, it will
 * be the <code>EntityIdentifier</code> for <code>this</code>.  In the case
 * of an  <code>IEntity</code>, it will be the <code>EntityIdentifier</code>
 * that identifies the underlying IPerson, ChannelDefinition, etc.
 *
 * @return org.jasig.portal.EntityIdentifier
 */
public EntityIdentifier getUnderlyingEntityIdentifier();
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
