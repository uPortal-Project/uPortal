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

package  org.jasig.portal.channels.groupsmanager;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IPermissible;
import org.jasig.portal.IServant;
import org.jasig.portal.groups.ILockableEntityGroup;


/**
 * Session data for a cached thread stored in a Map in CGroupsManager
 * @author Don Fracapane
 * @version $Revision$
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public class CGroupsManagerSessionData extends CGroupsManagerUnrestrictedSessionData
      implements GroupsManagerConstants {
   public ChannelRuntimeData runtimeData;
   public ChannelStaticData staticData;
   public IServant servantChannel = null;
   public boolean servantMode = false;
   public boolean allowFinish = true;
   //public boolean blockEntitySelect = false;
   //public String uid;
   public long startRD;
   public ILockableEntityGroup lockedGroup = null;
   public String highlightedGroupID;
   public int currentPage = 1;
   public String rootViewGroupID;
   public String defaultRootViewGroupID = "0";
   public String mode = BROWSE_MODE;   //"browse", "edit" or "select"
   public String returnToMode;
   public String feedback;             // use to display info to user (eg. "Unable to lock...")
   public String customMessage;
   public IPermissible permissible;

   /**
    * Returns a subset of unrestricted variables to be used for Document manipulation.
    * This is accomplished by casting this into the unrestricted partent class.
    * @return CGroupsManagerUnrestrictedSessionData
    */
   public CGroupsManagerUnrestrictedSessionData getUnrestrictedData () {
      return  (CGroupsManagerUnrestrictedSessionData) this;
   }
}
