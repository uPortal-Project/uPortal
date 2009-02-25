/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
