/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.channels.groupsmanager.commands;

import java.util.Iterator;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.channels.groupsmanager.CGroupsManagerSessionData;
import org.jasig.portal.channels.groupsmanager.GroupsManagerXML;
import org.jasig.portal.channels.groupsmanager.Utility;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * SelectMembers sets the "selected" attribute for each elements that was
 * selected by the user.
 * @author Don Fracapane
 * @version $Revision$
 */
public class SelectMembers extends GroupsManagerCommand {

   public SelectMembers () {
   }

   /**
    * This is the public method
    * @param sessionData
    * @throws Exception
    */
   public void execute (CGroupsManagerSessionData sessionData) throws Exception{
      ChannelStaticData staticData = sessionData.staticData;
      ChannelRuntimeData runtimeData= sessionData.runtimeData;

      Utility.logMessage("DEBUG", "SelectMembers::execute(): Start");
      Document model = getXmlDoc(sessionData);
      String theCommand = getCommand(runtimeData);
      Utility.logMessage("DEBUG", "SelectMembers::execute(): action = " + theCommand);
      Iterator itr = runtimeData.keySet().iterator();
      Element theElement;
      while (itr.hasNext()) {
         String key = (String)itr.next();
         String thisPerm = null;
         String tagname = theCommand + "//";
         if (key.indexOf(tagname) > -1) {
            thisPerm = key.substring(key.lastIndexOf("/") + 1);
            if (Utility.notEmpty(thisPerm)) {
               //Utility.logMessage("DEBUG","SelectMembers::renderXML(): Iterating over input");
               String princeKey = thisPerm.substring(0, thisPerm.lastIndexOf("|"));
               String princeType = thisPerm.substring(thisPerm.lastIndexOf("|") + 1);
               //String principal = princeType + "." + princeKey;
               theElement = GroupsManagerXML.getElementByTagNameAndId(model, princeType, princeKey);
               // test first
               if (theElement != null) {
                  GroupsManagerXML.refreshAllNodesIfRequired(sessionData.getUnrestrictedData(), theElement);
                  theElement.setAttribute("selected", String.valueOf(theCommand.equals("Select")));
                  Utility.logMessage("DEBUG", "SelectMembers::execute(): " + theCommand
                        + "ed element " + princeType + " " + princeKey);
               }
            }
         }
      }
   }
}
