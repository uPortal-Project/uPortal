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

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.channels.groupsmanager.CGroupsManagerSessionData;
import org.jasig.portal.channels.groupsmanager.GroupsManagerXML;
import org.jasig.portal.channels.groupsmanager.Utility;
import org.jasig.portal.groups.ILockableEntityGroup;
import org.jasig.portal.services.GroupService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** This command delegates to the GroupsService to find entities requested
 *  by the user.
 * @author Don Fracapane
 * @version $Revision$
 */
public class EditGroup extends GroupsManagerCommand {

   /**
    * put your documentation comment here
    */
   public EditGroup () {
   }

   /**
    * This is the public method
    * @throws Exception
    * @param sessionData
    */
   public void execute (CGroupsManagerSessionData sessionData) throws Exception{
      ChannelStaticData staticData = sessionData.staticData;
      ChannelRuntimeData runtimeData= sessionData.runtimeData;

      Document model = getXmlDoc(sessionData);
      Utility.logMessage("DEBUG", "EditGroup::execute(): Start");
      String parentElemId = getCommandArg(runtimeData);
      // if not IPerson group, then set view root to root for requested type

      String userID = getUserID(sessionData);
      //String userName = GroupsManagerXML.getEntityName(ENTITY_CLASSNAME, userID);
      String userKey = sessionData.user.getEntityIdentifier().getKey();
      String lockKey = userID + "::" + userKey;
      Utility.logMessage("DEBUG", "EditGroup::execute(): lockKey = " + lockKey);
      Element parentElem = GroupsManagerXML.getElementById(model, parentElemId);
      String parentKey = parentElem.getAttribute("key");
      ILockableEntityGroup lockedGroup = GroupService.findLockableGroup(parentKey, lockKey);
      // if lockedGroup is null, an exception has probably already been thrown
      if (lockedGroup != null){
         GroupsManagerXML.refreshAllNodesRecursivelyIfRequired(model, parentElem);
         // store in sessionData
         sessionData.lockedGroup=lockedGroup;
         sessionData.mode = EDIT_MODE;
         staticData.setParameter("groupParentId", parentElemId);
      }

      Utility.logMessage("DEBUG", "EditGroup::execute(): Uid of parent element = " +
            parentElemId);
   }
}
