/**
 * Copyright © 2004 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */




package org.jasig.portal.layout;


import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import org.jasig.portal.IUserLayoutStore;
import org.jasig.portal.UserLayoutStoreFactory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.PortalException;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.GroupService;


/**
 * The user priority manager implementation, manages user priority ranges for aggregated layouts
 * <p>
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
