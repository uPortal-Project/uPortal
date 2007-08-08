/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.channels.groupsmanager.commands;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.channels.groupsmanager.CGroupsManagerSessionData;
import org.jasig.portal.channels.groupsmanager.GroupsManagerXML;
import org.jasig.portal.channels.groupsmanager.Utility;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This command sets the expanded attribute for an element to "false" and lets
 * the transformation handle the tree collapse display.
 * @author Don Fracapane
 * @version $Revision$
 */
public class CollapseGroup extends GroupsManagerCommand {

   /** Creates new CollapseGroup */
   public CollapseGroup () {
   }

   /**
    * This is the public method
    * @param sessionData
    * @throws Exception
    */
   public void execute (CGroupsManagerSessionData sessionData) throws Exception{
      ChannelRuntimeData runtimeData= sessionData.runtimeData;

      Utility.logMessage("DEBUG", "CollapseGroup::execute(): Start");
      Document model = getXmlDoc(sessionData);
      Element collapseElem = GroupsManagerXML.getElementByTagNameAndId(model, GROUP_TAGNAME,
            getCommandArg(runtimeData));
      Utility.logMessage("DEBUG", "CollapseGroup::execute(): collapseElem was found: "
            + collapseElem);
      if (collapseElem != null) {
         Utility.logMessage("DEBUG", "CollapseGroup::execute(): Element to be expanded: \n"
               + collapseElem);
         GroupsManagerXML.refreshAllNodesIfRequired(sessionData.getUnrestrictedData(), collapseElem);
         collapseElem.setAttribute("expanded", "false");
         GroupsManagerXML.refreshAllNodesIfRequired(sessionData.getUnrestrictedData(), collapseElem);
      }
      return;
   }
}
