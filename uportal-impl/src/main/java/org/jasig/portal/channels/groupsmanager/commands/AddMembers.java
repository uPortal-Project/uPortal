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
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.services.GroupService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** 
 *  AddMembers is a command which sets the id of the parent group.
 *  The parent group is the group to which child members will be added.
 *  Control is then passed to a selection view where the child group members will
 *  be selected for addition. When the selection has been completed by the user,
 *  the DoneWithSelection command will be invoked where the selected children
 *  group members are actually processed.  Alternatively, the CancelSelection
 *  command is invoked to cancel the selection process and reset the mode and
 *  view control parameters.
 * @author Don Fracapane
 * @version $Revision$
 */
public class AddMembers extends GroupsManagerCommand {

   /**
    * put your documentation comment here
    */
   public AddMembers () {
   }

   /**
    * This is the public method
    * @param sessionData
    * @throws Exception
    */
   public void execute (CGroupsManagerSessionData sessionData) throws Exception {
      ChannelStaticData staticData = sessionData.staticData;
      ChannelRuntimeData runtimeData= sessionData.runtimeData;
      Document model = getXmlDoc(sessionData);
      Utility.logMessage("DEBUG", "AddMembers::execute(): Start");
      String parentAddElemId = getCommandArg(runtimeData);

      IGroupMember pg = GroupsManagerXML.retrieveGroupMemberForElementId(model, parentAddElemId);
      sessionData.rootViewGroupID = Utility.translateKeytoID(GroupService.getRootGroup(pg.getLeafType()).getKey(),sessionData.getUnrestrictedData());
      // Parent was locked so no other thread or process could have changed it, but
      // child members could have changed.
      Element parentElem = GroupsManagerXML.getElementById(model, parentAddElemId);
      GroupsManagerXML.refreshAllNodesRecursivelyIfRequired(sessionData.getUnrestrictedData(), parentElem);
      sessionData.returnToMode = sessionData.mode;
      sessionData.mode=SELECT_MODE;
      sessionData.highlightedGroupID = sessionData.rootViewGroupID;
      Utility.logMessage("DEBUG", "AddMembers::execute(): Uid of parent element = " +
            parentAddElemId);
      staticData.setParameter("groupParentId", parentAddElemId);
   }
}



