/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.groups;

import org.jasig.portal.EntityIdentifier;

/**
 * Defines an api for searching for entities
 *
 * @author Alex Vigdor
 * @version $Revision$
 */

public interface IEntitySearcher {
  /**
   * Find EntityIdentifiers for entities whose name matches the query string 
   * according to the specified method and is of the specified type 
   */
  public EntityIdentifier[] searchForEntities(String query, int method, Class type) throws GroupsException;
}