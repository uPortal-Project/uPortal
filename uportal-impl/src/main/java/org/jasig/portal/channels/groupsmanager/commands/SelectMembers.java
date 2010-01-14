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

import java.util.Iterator;

import org.jasig.portal.ChannelRuntimeData;
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
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public class SelectMembers extends GroupsManagerCommand {

   public SelectMembers () {
   }

   /**
    * This is the public method
    * @param sessionData
    * @throws Exception
    */
   public void execute (CGroupsManagerSessionData sessionData) throws Exception{
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
