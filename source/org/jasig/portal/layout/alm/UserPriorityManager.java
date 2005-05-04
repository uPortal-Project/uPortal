/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.alm;


import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.PortalException;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.layout.IUserLayoutStore;
import org.jasig.portal.layout.UserLayoutStoreFactory;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.GroupService;


/**
 * The user priority manager implementation, manages user priority ranges for aggregated layouts.
 * <p>
 * 
 * Prior to uPortal 2.5, this class existed in the package org.jasig.portal.layout.
 * It was moved to its present package to reflect that it is part of Aggregated
 * Layouts.
 *
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version $Revision$
 */
public class UserPriorityManager {
	
 private static IUserLayoutStore layoutStore = UserLayoutStoreFactory.getUserLayoutStoreImpl();
 private static Map ranges = new HashMap();
 private static final int MAX_SIZE = 5000;
 
 public static final int DEFAULT_MAX_PRIORITY = 1000;
 public static final int DEFAULT_MIN_PRIORITY = 0;
 
 
	
 public static int[] getPriorityRange ( IPerson person ) throws PortalException {
 	if ( ranges.size() >= MAX_SIZE ) ranges.clear();
 	Object storedRange = ranges.get(new Integer(person.getID()));
 	if ( storedRange != null )
 	  return (int[]) storedRange;
 	
 	if ( !(layoutStore instanceof IAggregatedUserLayoutStore) )
 	  throw new PortalException ( "The layout store must have type IAggregatedUserLayoutStore!");
	IAggregatedUserLayoutStore store = (IAggregatedUserLayoutStore) layoutStore;  
	
	EntityIdentifier personIdentifier = person.getEntityIdentifier();
    IGroupMember groupPerson = GroupService.getGroupMember(personIdentifier);
    
    int[] result = new int[] { DEFAULT_MIN_PRIORITY, DEFAULT_MAX_PRIORITY };
    
    for ( Iterator groups = groupPerson.getAllContainingGroups(); groups.hasNext(); ) {
      IEntityGroup group = (IEntityGroup) groups.next();
      int[] range = store.getPriorityRange(group.getKey());
      if ( range.length == 2 ) {
          if ( result[0] > range[0] )
            result[0] = range[0];
		  if ( result[1] < range[1] )
			result[1] = range[1];           	
      }
    }
 	
 	ranges.put(new Integer(person.getID()),result);
 	return result;	
 }

}
