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
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public class EditGroup extends GroupsManagerCommand {

   /**
    * put your documentation comment here
    */
   public EditGroup () {
   }

   /**
    * This is the public method
    * @param sessionData
    * @throws Exception
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
         GroupsManagerXML.refreshAllNodesRecursivelyIfRequired(sessionData.getUnrestrictedData(), parentElem);
         // store in sessionData
         sessionData.lockedGroup=lockedGroup;
         sessionData.mode = EDIT_MODE;
         staticData.setParameter("groupParentId", parentElemId);
      }

      Utility.logMessage("DEBUG", "EditGroup::execute(): Uid of parent element = " +
            parentElemId);
   }
}
