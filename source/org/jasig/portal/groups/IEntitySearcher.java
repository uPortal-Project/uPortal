/* Copyright 2001, 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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