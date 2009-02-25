/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.services.entityproperties;

import java.util.Iterator;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.services.GroupService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A finder implementation to provide a , delimited list of containing groups
 * for any entity
 *
 * @author Alex Vigdor av317@columbia.edu
 * @version $Revision$
 */

public class ContainingGroupsFinder implements IEntityPropertyFinder {
    
    private static final Log log = LogFactory.getLog(ContainingGroupsFinder.class);
    
  protected final static String[] names = new String[] {"Containing Groups"};
  public ContainingGroupsFinder() {
  }
  public String[] getPropertyNames(EntityIdentifier entityID) {
    return names;
  }
  public String getProperty(EntityIdentifier entityID, String name) {
    String r = null;
    try{
      StringBuffer buf = new StringBuffer();
      if (name.equals(names[0])){
        IGroupMember gm = GroupService.getGroupMember(entityID);
        Iterator i = gm.getContainingGroups();
        int x = 0;
        while (i.hasNext()){
          if (x > 0){
            buf.append(", "); 
          }
           IEntityGroup g = (IEntityGroup) i.next();
           buf.append(g.getName());
           x++;
        }
      }
      r = buf.toString();
    }
    catch(Exception e){
       log.error("Exception getting property " +
            "entityId=" + entityID + " name=" + name, e);
    }
    return r;
  }
}