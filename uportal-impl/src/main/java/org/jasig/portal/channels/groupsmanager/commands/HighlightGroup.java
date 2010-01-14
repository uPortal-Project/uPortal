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

package org.jasig.portal.channels.groupsmanager.commands;

import org.jasig.portal.channels.groupsmanager.CGroupsManagerSessionData;
import org.jasig.portal.channels.groupsmanager.GroupsManagerCommandFactory;
import org.jasig.portal.channels.groupsmanager.GroupsManagerXML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A Groups Manager command to highlight a particular element.  Also
 * releases any held locks on other groups, moves to BROWSE mode from EDIT
 *
 * @author Alex Vigdor
 * @version $Revision$
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public class HighlightGroup extends org.jasig.portal.channels.groupsmanager.commands.GroupsManagerCommand{

   public HighlightGroup() {
   }
   /**
    * put your documentation comment here
    * @param sessionData
    * @throws Exception
    */
   public void execute(CGroupsManagerSessionData sessionData) throws Exception{
      Document model = getXmlDoc(sessionData);
      sessionData.highlightedGroupID = getCommandArg(sessionData.runtimeData);
      sessionData.currentPage = 1;
      GroupsManagerCommandFactory.get("Expand").execute(sessionData);
      // expand parent
      Element expandedElem = GroupsManagerXML.getElementByTagNameAndId(model, GROUP_TAGNAME, getCommandArg(sessionData.runtimeData));
      if (expandedElem != null) {
        GroupsManagerXML.expandGroupElementXML((Element) expandedElem.getParentNode(), sessionData.getUnrestrictedData());
      }
      // unlock and discard any other group that may be held in a locked state
      if((sessionData.lockedGroup!=null) && (!sessionData.lockedGroup.getEntityIdentifier().getKey().equals(sessionData.highlightedGroupID)) && (!sessionData.mode.equals("select"))){
         sessionData.lockedGroup.getLock().release();
         sessionData.lockedGroup = null;
         sessionData.mode = BROWSE_MODE;
      }
   }
}