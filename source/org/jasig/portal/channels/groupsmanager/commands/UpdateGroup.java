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

/**
 * <p>Title: uPortal</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Columbia University</p>
 * @author Don Fracapane
 * @version 2.0
 */
import  java.util.*;
import  org.jasig.portal.ChannelStaticData;
import  org.jasig.portal.channels.groupsmanager.*;
import  org.jasig.portal.groups.IEntityGroup;
import  org.jasig.portal.groups.IGroupMember;
import  org.jasig.portal.groups.GroupsException;
import  org.jasig.portal.services.GroupService;
import  org.w3c.dom.Element;
import  org.w3c.dom.Node;
import  org.w3c.dom.NodeList;
import  org.apache.xerces.dom.DocumentImpl;


/**
 * put your documentation comment here
 */
public class UpdateGroup extends GroupsManagerCommand {

   /**
    * This class update the group with a new name. It then updates
    * all of the xml elements with the new name.
    */
   public UpdateGroup () {
   }

   /**
    * put your documentation comment here
    * @param runtimeData
    * @param staticData
    */
   public void execute (org.jasig.portal.ChannelRuntimeData runtimeData, ChannelStaticData staticData) {
      Utility.logMessage("DEBUG", "UpdateGroup::execute(): Start");
      DocumentImpl xmlDoc = (DocumentImpl)staticData.get("xmlDoc");
      String theCommand = runtimeData.getParameter("grpCommand");
      String newGrpName = runtimeData.getParameter("grpName");                  //?
      String updId = getCommandIds(runtimeData);
      Node updNode;
      Node titleNode;
      Element updElem = Utility.getElementByTagNameAndId(xmlDoc, GROUP_TAGNAME, updId);
      String updKey = updElem.getAttribute("key");
      String curGrpName = "unknown";
      String retMsg;
      NodeList nList = updElem.getElementsByTagName("dc:title");
      int nListCount = nList.getLength();
      Utility.logMessage("DEBUG", "UpdateGroup::execute(): nList length = " + nListCount);
      if (nListCount > 0) {
         titleNode = nList.item(0);
         curGrpName = titleNode.getFirstChild().getNodeValue();
      }
      else {
         Utility.logMessage("ERROR", "UpdateGroup::execute(): Cannot find dc:title element for: "
               + updElem.getAttribute("name"));
         return;
      }
      Utility.logMessage("DEBUG", "UpdateGroup::execute(): Group: " + curGrpName + "will be updated to :"
            + newGrpName);
      IEntityGroup updGroup = GroupsManagerXML.retrieveGroup(updKey);
      if (updGroup == null) {
         retMsg = "Unable to retrieve Group!";
         runtimeData.setParameter("commandResponse", retMsg);
         return;
      }
      try {
         Utility.logMessage("DEBUG", "UpdateGroup::execute(): About to update group: "
               + curGrpName);
         updGroup.setName(newGrpName);
         updGroup.update();
         Utility.logMessage("DEBUG", "UpdateGroup::execute(): About to update xml nodes for group: "
               + curGrpName);
         // update all xml nodes for this group
         Iterator updatedNodes = Utility.getNodesByTagNameAndKey(xmlDoc, GROUP_TAGNAME,
               updKey);
         Utility.logMessage("DEBUG", "UpdateGroup::execute(): About to gather all elements for key: "
               + updKey);
         while (updatedNodes.hasNext()) {
            updNode = (Node)updatedNodes.next();
            updElem = (Element)updNode;
            Utility.logMessage("DEBUG", "UpdateGroup::execute(): About to update xml for element id: "
                  + updElem.getAttribute("id"));
            nList = updElem.getElementsByTagName("dc:title");
            if (nList.getLength() > 0) {
               titleNode = nList.item(0);
               titleNode.getFirstChild().setNodeValue(newGrpName);
            }
         }
         runtimeData.setParameter("grpMode", "browse");
         runtimeData.setParameter("grpView", "tree");
         runtimeData.setParameter("grpViewId", "0");
      } catch (GroupsException ge) {
         retMsg = "Unable to create new group\n" + ge;
         runtimeData.setParameter("commandResponse", retMsg);
         Utility.logMessage("ERROR", "UpdateGroup::execute(): " + retMsg + ge);
      } catch (Exception e) {
         retMsg = "Unable to update group : " + curGrpName;
         runtimeData.setParameter("commandResponse", retMsg);
         Utility.logMessage("ERROR", "UpdateGroup::execute(): " + retMsg + ".\n" + e);
      }
      Utility.logMessage("DEBUG", "UpdateGroup::execute(): Finished");
   }
}



