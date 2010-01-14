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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.channels.groupsmanager.CGroupsManagerSessionData;
import org.jasig.portal.channels.groupsmanager.GroupsManagerXML;
import org.jasig.portal.channels.groupsmanager.Utility;
import org.jasig.portal.concurrency.LockingException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** A select cycle could be started in Servant mode or it could be started by
 *  the AddMembers command. The AddMembers command sets the id of the parent
 *  group (ie. the group to which child  members will be added). Control is then
 *  passed to a selection view where the child group members will be selected
 *  for addition. When the selection has been completed by the user, the
 *  DoneWithSelection command will be invoked where the selected children
 *  group members are actually processed. Alternatively, the CancelSelection
 *  command is invoked to cancel the selection process and reset the mode and
 *  view control parameters.
 * @author Don Fracapane
 * @version $Revision$
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public class CancelSelection extends GroupsManagerCommand {
    
   private static final Log log = LogFactory.getLog(GroupsManagerCommand.class);

   public CancelSelection () {
   }

   /**
    * This is the public method
    * @param sessionData
    * @throws Exception
    */
   public void execute (CGroupsManagerSessionData sessionData) throws Exception {
      ChannelStaticData staticData = sessionData.staticData;

      Document model = getXmlDoc(sessionData);
      /** @todo move to GroupManagerCommand.cleanUp */
      Utility.logMessage("DEBUG", "CancelSelection::execute(): Start");
      clearSelected(sessionData);
      String parentId = getParentId(staticData);
      if (parentId != null) {
         // go back to stored mode
         sessionData.mode = sessionData.returnToMode;
         sessionData.highlightedGroupID = parentId;
         sessionData.rootViewGroupID=null;
         // Parent is locked so no other thread or process could have changed it, but
         // child members could have changed.
         Element parentElem = GroupsManagerXML.getElementById(model, parentId);
         GroupsManagerXML.refreshAllNodesRecursivelyIfRequired(sessionData.getUnrestrictedData(), parentElem);
         sessionData.staticData.remove("groupParentId");
      }
      else {
        // @todo refactor into separate servant finish command
        if (sessionData.lockedGroup!=null){
          try{
            sessionData.lockedGroup.getLock().release();
          }catch(LockingException e){
              log.error(e,e);
          }
        }
         staticData.setParameter("groupManagerFinished", "true");
      }
   }
}
