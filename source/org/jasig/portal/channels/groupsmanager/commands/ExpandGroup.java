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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * If the children xml elements have not already been created, this command will
 * retrieve the group members and created the elements. This command then sets
 * the expanded attribute for an element to "true" and lets the transformation
 * handle the tree expansion display.
 * @author Don Fracapane
 * @version $Revision$
 */

public class ExpandGroup extends org.jasig.portal.channels.groupsmanager.commands.GroupsManagerCommand {

   /** Creates new ExpandGroup */
   public ExpandGroup () {
   }

   /**
    * This is the public method
    * @param sessionData
    * @throws Exception
    */
   public void execute (CGroupsManagerSessionData sessionData) throws Exception{
      ChannelStaticData staticData = sessionData.staticData;
      ChannelRuntimeData runtimeData= sessionData.runtimeData;

      Utility.logMessage("DEBUG", "ExpandGroup::execute(): Start");
      Document model = getXmlDoc(sessionData);
      // Due to the networked relationship of groups, the next method has to return a list of elements.
      String elemUid = getCommandArg(runtimeData);
      Utility.logMessage("DEBUG", "ExpandGroup::execute(): Uid of expanded element = "
            + elemUid);
      Element expandedElem = GroupsManagerXML.getElementByTagNameAndId(model, GROUP_TAGNAME, elemUid);
      String rootOwner;
      if (expandedElem != null) {
        GroupsManagerXML.refreshAllNodesRecursivelyIfRequired(sessionData.getUnrestrictedData(), expandedElem);

        if (expandedElem.getAttribute("searchResults").equals("true")){
          expandedElem.setAttribute("expanded","true");
        }
        else{
          GroupsManagerXML.expandGroupElementXML(expandedElem,sessionData.getUnrestrictedData());
        }
      }
   }
}
