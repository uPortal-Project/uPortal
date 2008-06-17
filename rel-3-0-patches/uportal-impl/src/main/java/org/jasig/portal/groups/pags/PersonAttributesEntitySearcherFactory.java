/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
