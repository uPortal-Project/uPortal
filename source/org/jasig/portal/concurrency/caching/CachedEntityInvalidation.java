/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.concurrency.caching;

import java.util.Date;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.IBasicEntity;
/**
 * An instance of this class represents an event: a change to an
 * IBasicEntity that renders any cached instances of the entity
 * invalid.
 *
 * @author Dan Ellentuck
 * @version $Revision$
 */
public class CachedEntityInvalidation implements IBasicEntity {
    private final EntityIdentifier entityIdentifier;
    private final Date invalidationTime;
    private final int cacheID;
/**
 * CachedEntityInvalidation constructor.
 */
public CachedEntityInvalidation(Class eType, String eKey, Date time, int cache)
{
    this( new EntityIdentifier(eKey, eType), time, cache );
}
/**
 * CachedEntityInvalidation constructor.
 */
public CachedEntityInvalidation(EntityIdentifier newEntityIdentifier, Date time, int cache)
{
    super();
    entityIdentifier = newEntityIdentifier;
    invalidationTime = time;
    cacheID = cache;
}
/**
 * @return EntityIdentifier
 */
public final EntityIdentifier getEntityIdentifier() {
    return entityIdentifier;
}
/**
 * @return Date
 */
public Date getInvalidationTime() {
    return invalidationTime;
}
/**
  * @return java.lang.String
 */
public final String getKey() {
    return getEntityIdentifier().getKey();
}
/**
 * @return java.lang.Class
 */
public final Class getType() {
    return getEntityIdentifier().getType();
}
/**
 * @return String
 */
public String toString() {
    return "CachedEntityInvalidation: " + getType().getName() +
      "(" + getKey() + ") " + new java.sql.Timestamp(getInvalidationTime().getTime()) +
      " from cache " + cacheID;
}
/**
 * @return int
 */
public int getCacheID() {
    return cacheID;
}

}
