/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.groups.pags;

import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntitySearcher;
import org.jasig.portal.groups.IEntitySearcherFactory;

/**
 * @author Al Wold
 */
public class PersonAttributesEntitySearcherFactory implements IEntitySearcherFactory {
   public IEntitySearcher newEntitySearcher() throws GroupsException {
      return (IEntitySearcher)new PersonAttributesGroupStoreFactory().newGroupStore();
   }

}
