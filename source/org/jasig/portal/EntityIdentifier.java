/**
 * Copyright (c) 2002 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal;

/**
 * A key and type that uniquely identify a portal entity.
 * @author Dan Ellentuck
 * @version $Revision$
 * @see IBasicEntity
 */
public class EntityIdentifier implements java.io.Serializable {
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
 * @param obj the Object to compare with
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
