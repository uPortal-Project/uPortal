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
/**
 * CachedEntityInvalidation constructor.
 */
public CachedEntityInvalidation(Class eType, String eKey, Date time)
{
    super();
    entityIdentifier = new EntityIdentifier(eKey, eType);
    invalidationTime = time;
}
/**
 * CachedEntityInvalidation constructor.
 */
public CachedEntityInvalidation(EntityIdentifier newEntityIdentifier, Date time)
{
    super();
    entityIdentifier = newEntityIdentifier;
    invalidationTime = time;
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
      "(" + getKey() + ") " + new java.sql.Timestamp(getInvalidationTime().getTime());
}
}
