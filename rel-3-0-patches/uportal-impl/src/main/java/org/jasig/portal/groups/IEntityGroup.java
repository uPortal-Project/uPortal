/* Copyright 2001, 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.groups;

import javax.naming.Name;

/**
 * An <code>IEntityGroup</code> is a composite, or non-leaf <code>IGroupMember</code>.
 * It contains <code>IEntities</code> and other <code>IEntityGroups</code>.
 * <p>
 * The api defines methods for adding a member to, and removing it from, a group, 
 * though not vice versa.  (Although there is nothing to prevent a given <code>IGroupMember</code>
 * implementation from storing references to its containing groups.)  These methods only
 * change the group structure in memory.
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
 * <p>
 * The following methods were added to permit an <code>IEntityGroup</code> to function
 * within a composite group service:
 * <p>
 *   <code>getLocalKey()</code> - returns the key within the service of origin.<br>
 *   <code>getServiceName()</code> - returns the Name of the group service of origin.<br>
 *   <code>setLocalGroupService()</code> - sets the group service of origin.<br>
 * <p>
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
 * @exception GroupsException if the delete cannot be performed. 
 */
  public void delete() throws GroupsException;
/**
 * Returns the name of the group creator.  May be null.
 * @return String
 */
  public String getCreatorID();
/**
 * Returns the group description, which may be null.
 * @return String
 */
  public String getDescription();
/**
 * Returns the key from the group service of origin.
 * @return String
 */
  public String getLocalKey();
/**
 * Returns the group name.
 * @return String
 */
  public String getName();
/**
 * Returns the Name of the group service of origin.
 * @return String
 */
  public Name getServiceName();
/**
 * Answers if this <code>IEntityGroup</code> can be changed or deleted.
 * @return boolean
 * @exception GroupsException
 */
  public boolean isEditable() throws GroupsException;
/**
 * Removes the <code>IGroupMember</code> from this group, but does not remove the
 * membership from the data store.
 * @param gm org.jasig.portal.groups.IGroupMember
 */
  public void removeMember(IGroupMember gm) throws GroupsException;
/**
 * @param userID String (required)
 */
  public void setCreatorID(String userID);
/**
 * @param name String (may be null)
 */
  public void setDescription(String name);
/**
 * Sets the group name which must be unique within any of its containing 
 * groups.  
 * @param name String
 * @exception GroupsException
 */
  public void setName(String name) throws GroupsException;
/**
 * Commit the <code>IEntityGroup</code> AND ITS MEMBERSHIPS to the data store.
 * @exception GroupsException if the update cannot be performed. 
 */
  public void update() throws GroupsException;
/**
 * Commit this <code>IEntityGroup's</code> memberships to the data store.
 * @exception GroupsException if the update cannot be performed. 
 */
  public void updateMembers() throws GroupsException;

/**
 * Sets the group service of origin.
 */
  public void setLocalGroupService(IIndividualGroupService groupService) throws GroupsException;
}
