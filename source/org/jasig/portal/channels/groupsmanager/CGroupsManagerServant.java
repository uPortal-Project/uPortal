/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.channels.groupsmanager;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IMultithreadedChannel;
import org.jasig.portal.IServant;
import org.jasig.portal.MultithreadedCacheableChannelAdapter;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.groups.IGroupMember;

/**
 * CGroupsManagerServant is an IServant subclass of CGroupsManager
 * This will allow other channels to delegate to CGroupsManager at runtime
 * @author Alex Vigdor, av317@columbia.edu
 * @version $Revision$
 */

public class CGroupsManagerServant extends MultithreadedCacheableChannelAdapter
      implements IServant {
   final IMultithreadedChannel channel;
   final String uid;

   /**
    * put your documentation comment here
    * @param channel (IMultithreadedChannel)
    * @param uid (String)
    */
   public CGroupsManagerServant (IMultithreadedChannel channel, String uid) {
      super(channel,uid);
      this.channel = channel;
      this.uid = uid;
   }

   /**
    * True when the servant is ready to pass control back to the master channel.
    * @return boolean
    */
   public boolean isFinished () {
      CGroupsManagerSessionData sessionData = getSessionData();
      ChannelStaticData staticData = sessionData.staticData;
      boolean isFinished = false;
      if (staticData.containsKey("groupManagerFinished") && staticData.getParameter("groupManagerFinished").equals("true")) {
         isFinished = true;
      }
      return  isFinished;
   }

   CGroupsManagerSessionData getSessionData(){
    return ((CGroupsManager) channel).getSessionData(uid);
   }

   /**
    * Sets the staticData.
    * @param sd (ChannelStaticData)
    */
   public void setStaticData (ChannelStaticData sd) {
      try {
         channel.setStaticData(sd, uid);
         getSessionData().servantMode = true;
      }
      catch (PortalException pex) {
         Utility.logMessage("ERROR", this.getClass().getName()
            + ".setStaticData() : Unable to set static data for servant. "
            + "staticData parm = " + sd
            + "uid parm = " + uid, pex);
      }
   }

   /**
    * Create a SESSION_DONE event. This event is caught by CGroupsManager which deletes
    * the session data object.
    * @throws Throwable
    */
   protected void finalize() throws Throwable{
      super.finalize();
      // send SESSION_DONE event to the wrapped CGroupsManager channel.
      PortalEvent ev=PortalEvent.SESSION_DONE_EVENT;
      channel.receiveEvent(ev, uid);
   }

   /**
    * Returns an array of objects representing the result set.
    * Note that these are IGroupMembers !!
    * @return Object[]
    */
   public Object[] getResults () {
      CGroupsManagerSessionData sessionData = ((CGroupsManager) channel).getSessionData(uid);
      ChannelStaticData staticData = sessionData.staticData;
      ChannelRuntimeData runtimeData = sessionData.runtimeData;
      IGroupsManagerCommand cmd = GroupsManagerCommandFactory.get("Done");
      try{
         cmd.execute(sessionData);
      }
      catch(Exception e){
         Utility.logMessage("ERROR", e.toString(), e);
         sessionData.feedback = "Error executing command Done: "+e.getMessage();
      }
      Object[] results = (Object[])staticData.get("princResults");
      if (results == null){
        results = new IGroupMember[0];
      }
      Utility.logMessage("DEBUG", "CGroupsManagerservant.getResults()");
      return  results;
   }
}



