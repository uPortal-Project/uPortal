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
    * @throws Exception
    * @param sessionData
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
                  GroupsManagerXML.refreshAllNodesIfRequired(model, theElement);
                  theElement.setAttribute("selected", String.valueOf(theCommand.equals("Select")));
                  Utility.logMessage("DEBUG", "SelectMembers::execute(): " + theCommand
                        + "ed element " + princeType + " " + princeKey);
               }
            }
         }
      }
   }
}
