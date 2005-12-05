/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
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