/* Copyright 2001, 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/
 
package org.jasig.portal.groups.local;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntitySearcher;

/**
 * An IEntitySearcher implementation for the local portal group service. 
 * Uses implementations of ITypedEntitySearcher to do the dirty work.
 *
 * @author Alex Vigdor
 * @version $Revision$
 */


public class EntitySearcherImpl implements IEntitySearcher {
  protected ITypedEntitySearcher[] searchers;
  public EntitySearcherImpl(ITypedEntitySearcher[] searchers) {
    this.searchers = searchers;
  }
  public EntityIdentifier[] searchForEntities(String query, int method, Class type) throws GroupsException {
    EntityIdentifier[] r = new EntityIdentifier[0];
    for (int i = 0; i < searchers.length; i++){
       if (searchers[i].getType().equals(type)){
          r=searchers[i].searchForEntities(query,method);
          break;
       }
    }
    return r;
  }
  
}