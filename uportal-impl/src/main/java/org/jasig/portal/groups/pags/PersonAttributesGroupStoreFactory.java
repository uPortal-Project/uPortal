/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.groups.pags;

import org.jasig.portal.groups.ComponentGroupServiceDescriptor;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntityGroupStore;
import org.jasig.portal.groups.IEntityGroupStoreFactory;

/**
 * Factory class for Person Attributes Group Store
 * 
 * @author Al Wold
 */
public class PersonAttributesGroupStoreFactory implements IEntityGroupStoreFactory {
   private static IEntityGroupStore groupStore;
   
   public static synchronized IEntityGroupStore getGroupStore() {
      if (groupStore == null) {
         groupStore = new PersonAttributesGroupStore();
      }
      return groupStore;
   }
   
   public IEntityGroupStore newGroupStore() throws GroupsException {
      return getGroupStore();
   }

   public IEntityGroupStore newGroupStore(ComponentGroupServiceDescriptor svcDescriptor) throws GroupsException {
      return getGroupStore();   
   }

}
