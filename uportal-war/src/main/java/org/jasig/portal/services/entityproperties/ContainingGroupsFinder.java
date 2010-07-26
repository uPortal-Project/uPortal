/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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