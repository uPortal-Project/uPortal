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

package  org.jasig.portal.channels.groupsmanager.commands;

import java.util.Enumeration;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.channels.groupsmanager.CGroupsManagerSessionData;
import org.jasig.portal.channels.groupsmanager.GroupsManagerConstants;
import org.jasig.portal.channels.groupsmanager.GroupsManagerXML;
import org.jasig.portal.channels.permissionsmanager.CPermissionsManagerServantFactory;
import org.jasig.portal.groups.IEntityGroup;

/**
 * A Groups Manager command to instantiate a permissions manager servant
 *
 * @author Alex Vigdor
 * @version $Revision$
 */
public class AssignPermissions extends GroupsManagerCommand implements GroupsManagerConstants{

  public AssignPermissions() {
  }

   /**
    * put your documentation comment here
    * @throws Exception
    * @param sessionData
    */
   public void execute(CGroupsManagerSessionData sessionData) throws Exception{
      ChannelRuntimeData slaveRD = sessionData.runtimeData;
      String[] tgts = new String[1];
      String[] acts = null;
      IEntityGroup g = (IEntityGroup) GroupsManagerXML.retrieveGroupMemberForElementId(this.getXmlDoc(sessionData), this.getCommandArg(sessionData.runtimeData));
      tgts[0] = g.getKey();
      if (g.isEditable()){
         acts = sessionData.permissible.getActivityTokens();
      }
      else{
        acts = new String[] {VIEW_PERMISSION, SELECT_PERMISSION, ASSIGN_PERMISSION}; 
      }
      sessionData.servantChannel = CPermissionsManagerServantFactory.getPermissionsServant(sessionData.permissible,
            sessionData.staticData, null, acts, tgts);
      slaveRD = (ChannelRuntimeData)sessionData.runtimeData.clone();
      Enumeration srd = slaveRD.keys();
      while (srd.hasMoreElements()) {
         slaveRD.remove(srd.nextElement());
      }
      sessionData.runtimeData = slaveRD;
   }
}