/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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

