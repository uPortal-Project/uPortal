/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
