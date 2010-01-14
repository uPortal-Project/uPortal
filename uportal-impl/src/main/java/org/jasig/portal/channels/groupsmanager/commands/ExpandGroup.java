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
 * If the children xml elements have not already been created, this command will
 * retrieve the group members and created the elements. This command then sets
 * the expanded attribute for an element to "true" and lets the transformation
 * handle the tree expansion display.
 * @author Don Fracapane
 * @version $Revision$
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
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
      ChannelRuntimeData runtimeData= sessionData.runtimeData;

      Utility.logMessage("DEBUG", "ExpandGroup::execute(): Start");
      Document model = getXmlDoc(sessionData);
      // Due to the networked relationship of groups, the next method has to return a list of elements.
      String elemUid = getCommandArg(runtimeData);
      Utility.logMessage("DEBUG", "ExpandGroup::execute(): Uid of expanded element = "
            + elemUid);
      Element expandedElem = GroupsManagerXML.getElementByTagNameAndId(model, GROUP_TAGNAME, elemUid);
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
