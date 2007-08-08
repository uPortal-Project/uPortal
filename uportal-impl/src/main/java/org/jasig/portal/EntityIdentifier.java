/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import java.io.Serializable;

/**
 * A key and type that uniquely identify a portal entity.
 * @author Dan Ellentuck
 * @version $Revision$
 * @see IBasicEntity
 */
public class EntityIdentifier implements Serializable {
    protected String key;
    protected Class type;
    
/**
 * KeyTypePair constructor.
 */
public EntityIdentifier(String entityKey, Class entityType) {
    super();
    key = entityKey;
    type = entityType;
}

/**
 * @param o the Object to compare with
 * @return true if these Objects are equal; false otherwise.
 */
public boolean equals(Object o) {
    if ( o == null )
        return false;
    if ( ! (o instanceof EntityIdentifier) )
        return false;
    EntityIdentifier ei = (EntityIdentifier) o;
    return ei.getType() == getType() &&
        ei.getKey().equals(key);
}

/**
 * @return java.lang.String
 */
public String getKey() {
    return key;
}

/**
 * @return java.lang.Class
 */
public Class getType() {
    return type;
}

/**
 * @return an integer hash code for the receiver
 */
public int hashCode() {
    return getType().hashCode() + getKey().hashCode();
}

/**
 * Returns a String that represents the value of this object.
 * @return a string representation of the receiver
 */
public String toString() {
    return "EntityIdentifier (" + type + "(" + key + "))";
}

}
