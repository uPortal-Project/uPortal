/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

/**
  * Minimal interface describes an entity that has only a key and a type.  It
  * can be cached, locked and grouped.
  * @author Dan Ellentuck
  * @version $Revision$
  */
public interface IBasicEntity {
/**
 * @return EntityIdentifier
 */
public EntityIdentifier getEntityIdentifier();
}

