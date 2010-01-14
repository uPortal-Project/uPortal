/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package  org.jasig.portal.channels.groupsmanager;

import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IServant;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.groups.IGroupMember;

/**
 * CGroupsManagerServant is an IServant subclass of CGroupsManager
 * This will allow other channels to delegate to CGroupsManager at runtime
 * @author Alex Vigdor, av317@columbia.edu
 * @version $Revision$
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public class CGroupsManagerServant extends CGroupsManager
      implements IServant {

   /**
    * put your documentation comment here
s    */
   public CGroupsManagerServant () {
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

   /**
    * Sets the staticData.
    * @param sd (ChannelStaticData)
    */
   public void setStaticData (ChannelStaticData sd) {
      super.setStaticData(sd);
      getSessionData().servantMode = true;
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
      receiveEvent(ev);
   }

   /**
    * Returns an array of objects representing the result set.
    * Note that these are IGroupMembers !!
    * @return Object[]
    */
   public Object[] getResults () {
      CGroupsManagerSessionData sessionData = getSessionData();
      ChannelStaticData staticData = sessionData.staticData;
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



