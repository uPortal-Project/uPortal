/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.groups.pags;

import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntityStore;
import org.jasig.portal.groups.IEntityStoreFactory;

/**
 * @author Al Wold
 */
public class PersonAttributesEntityStoreFactory implements IEntityStoreFactory {

   public IEntityStore newEntityStore() throws GroupsException {
      return (IEntityStore)new PersonAttributesGroupStoreFactory().newGroupStore();
   }

}
