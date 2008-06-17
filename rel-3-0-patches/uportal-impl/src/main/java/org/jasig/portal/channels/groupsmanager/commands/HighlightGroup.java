/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
 */
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