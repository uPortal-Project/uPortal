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

package  org.jasig.portal.channels.groupsmanager;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IMultithreadedChannel;
import org.jasig.portal.IServant;
import org.jasig.portal.MultithreadedCacheableChannelAdapter;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.services.LogService;

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
            + "uid parm = " + uid);
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
      PortalEvent ev=new PortalEvent(PortalEvent.SESSION_DONE);
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
      IGroupsManagerCommand cmd = GroupsManagerCommandFactory.instance().get("Done");
      try{
         cmd.execute(sessionData);
      }
      catch(Exception e){
         LogService.log(LogService.ERROR,e);
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



