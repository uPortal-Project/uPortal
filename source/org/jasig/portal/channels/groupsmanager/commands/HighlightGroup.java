/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
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
    * @throws Exception
    * @param sessionData
    */
   public void execute(CGroupsManagerSessionData sessionData) throws Exception{
      Document model = getXmlDoc(sessionData);
      sessionData.highlightedGroupID = getCommandArg(sessionData.runtimeData);
      sessionData.currentPage = 1;
      GroupsManagerCommandFactory.get("Expand").execute(sessionData);
      // expand parent
      Element expandedElem = GroupsManagerXML.getElementByTagNameAndId(model, GROUP_TAGNAME, getCommandArg(sessionData.runtimeData));
      if (expandedElem != null) {
        GroupsManagerXML.expandGroupElementXML((Element) expandedElem.getParentNode(),model);
      }
      // unlock and discard any other group that may be held in a locked state
      if((sessionData.lockedGroup!=null) && (!sessionData.lockedGroup.getEntityIdentifier().getKey().equals(sessionData.highlightedGroupID)) && (!sessionData.mode.equals("select"))){
         sessionData.lockedGroup.getLock().release();
         sessionData.lockedGroup = null;
         sessionData.mode = BROWSE_MODE;
      }
   }
}