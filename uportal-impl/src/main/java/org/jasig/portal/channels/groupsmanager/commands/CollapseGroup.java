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
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
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
