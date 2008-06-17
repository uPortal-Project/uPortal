/* Copyright 2001, 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.groups;

import org.jasig.portal.EntityIdentifier;

/**
 * Reference implementation for <code>IEntity</code>.
 * @author Dan Ellentuck
 * @version $Revision$
 */
public class EntityImpl extends GroupMemberImpl implements IEntity {
    protected EntityIdentifier entityIdentifier;
/**
 * EntityImpl constructor
 */
public EntityImpl(String newEntityKey, Class newEntityType) throws GroupsException
{
    this(new EntityIdentifier(newEntityKey, newEntityType));
}
/**
 * EntityImpl constructor
 */
public EntityImpl(EntityIdentifier ei) throws GroupsException
{
    super(ei);
    Integer id = org.jasig.portal.EntityTypes.getEntityTypeID(ei.getType());
    String key = id + "." + ei.getKey();
    entityIdentifier = new EntityIdentifier(key, org.jasig.portal.EntityTypes.LEAF_ENTITY_TYPE);
}
/**
 * @param obj the Object to compare with
 * @return true if these Objects are equal; false otherwise.
 * @see java.util.Hashtable
 */
public boolean equals(Object obj)
{
    if ( obj == null )
        return false;
    if ( obj == this )
        return true;
    if ( ! ( obj instanceof EntityImpl))
        return false;

    return this.getEntityIdentifier().equals( ((IEntity) obj).getEntityIdentifier() );
}
/**
 * @return org.jasig.portal.EntityIdentifier
 */
public EntityIdentifier getEntityIdentifier() {
    return entityIdentifier;
}
/**
 * Returns the type of the underyling entity.
 * @return java.lang.Class
 */
public Class getEntityType()
{
    return getUnderlyingEntityIdentifier().getType();
}
/**
 * Returns the key of the underlying entity.
 * @return java.lang.String
 */
public java.lang.String getKey() {
    return getUnderlyingEntityIdentifier().getKey();
}
/**
 * Returns the type of the underyling entity.
 * @return java.lang.Class
 */
public Class getLeafType()
{
    return getEntityType();
}
/**
 * Returns this object's type, as opposed to the type of its
 * underlying entity.
 *
 * @return java.lang.Class
 */
public Class getType()
{
    return getEntityType();
}
/**
 * @return boolean
 */
public boolean isEntity()
{
    return true;
}
/**
 * Returns a String that represents the value of this object.
 * @return a string representation of the receiver
 */
public String toString()
{
    String clsName = getEntityType().getName();
    return "EntityImpl (" + clsName + ") "  + getKey();
}
}
