/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
 */
public class CGroupsManagerSessionData extends CGroupsManagerUnrestrictedSessionData
      implements GroupsManagerConstants {
   public ChannelRuntimeData runtimeData;
   public ChannelStaticData staticData;
   public IServant servantChannel = null;
   public boolean servantMode = false;
   public boolean allowFinish = true;
   //public boolean blockEntitySelect = false;
   public String uid;
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
