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
    * @throws Exception
    * @param sessionData
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
        GroupsManagerXML.refreshAllNodesRecursivelyIfRequired(model, expandedElem);

        if (expandedElem.getAttribute("searchResults").equals("true")){
          expandedElem.setAttribute("expanded","true");
        }
        else{
          GroupsManagerXML.expandGroupElementXML(expandedElem,model);
        }
      }
   }
}
