/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.channels.groupsmanager;

import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.ICacheable;
import org.jasig.portal.IChannel;
import org.jasig.portal.IPermissible;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.services.EntityNameFinderService;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.utils.XSLT;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;

/**
 * CGroupsManager allows users to graphically administer all groups for which
 * user has administrtaive permissions.
 * @author Don Fracapane
 * @version $Revision$
 */
public class CGroupsManager
      implements IChannel, GroupsManagerConstants, IPermissible, ICacheable {
   // Location of the stylesheet list file
   protected static final String sslLocation = "CGroupsManager.ssl";
   protected static HashMap activities = null;
   protected HashMap targets = null;
//   protected HashMap sessionsMap = new HashMap();
   private CGroupsManagerSessionData sessionData;

   /** Creates new GroupsManagerChannel */
   public CGroupsManager () {
      init();
   }

   /**
    * put your documentation comment here
    */
   protected synchronized void init () {
      Utility.logMessage("DEBUG", this.getClass().getName() + "::init(): this = " + this);
      if (activities == null) {
         activities = new HashMap();
         try {
            activities.put(CREATE_PERMISSION, "Create a group in this context");
            activities.put(VIEW_PERMISSION, "View this group");
            activities.put(UPDATE_PERMISSION, "Rename this group");
            activities.put(DELETE_PERMISSION, "Delete this group");
            activities.put(SELECT_PERMISSION, "Select this group");
            activities.put(ADD_REMOVE_PERMISSION, "Manage this group's members");
            activities.put(ASSIGN_PERMISSION, "Assign Permissions for this group");
         } catch (Exception e) {
            Utility.logMessage("ERROR", "CGroupsManager.init():: unable to set activities"
                  + e, e);
         }
      }
      try {
         if (targets == null) {
            targets = new HashMap();
            IEntityGroup everyone = GroupService.getDistinguishedGroup(GroupService.EVERYONE);
            targets.put(everyone.getKey(), everyone.getName());
            Iterator allgroups = everyone.getAllMembers();
            while (allgroups.hasNext()) {
               IGroupMember g = (IGroupMember)allgroups.next();
               if (g.isGroup()) {
                  if (targets.get(g.getKey()) == null) {
                     try {
                        targets.put(g.getKey(), ((IEntityGroup)g).getName());
                     } catch (Exception e) {
                        Utility.logMessage("ERROR", "CGroupsManager.init():: unable to add target"
                              + e, e);
                     }
                  }
               }
            }

            IEntityGroup allChans = GroupService.getDistinguishedGroup(GroupService.CHANNEL_CATEGORIES);
            targets.put(allChans.getKey(), allChans.getName());
            Iterator allcgroups = allChans.getAllMembers();
            while (allcgroups.hasNext()) {
               IGroupMember g = (IGroupMember)allcgroups.next();
               if (g.isGroup()) {
                  if (targets.get(g.getKey()) == null) {
                     try {
                        targets.put(g.getKey(), ((IEntityGroup)g).getName());
                     } catch (Exception e) {
                        Utility.logMessage("ERROR", "CGroupsManager.init():: unable to add target"
                              + e, e);
                     }
                  }
               }
            }
         }
      } catch (Exception e) {
         Utility.logMessage("ERROR", "CGroupsManager.init():: unable to set targets"
               + e, e);
      }
   }

   /**
    * Acquires ChannelRuntimeProperites from the channel.
    * This function may be called by the portal framework throughout the session.
    * @see ChannelRuntimeProperties
    * @param uid
    * @return ChannelRuntimeProperties
    */
   public ChannelRuntimeProperties getRuntimeProperties () {
      return  new ChannelRuntimeProperties();
   }

   /**
    * Passes an outside event to a channel.
    * Events should normally come from the LayoutBean.
    * @param ev PortalEvent object
    * @param uid
    * @see PortalEvent
    */
   public void receiveEvent (PortalEvent ev)
   {
      if (ev.getEventNumber() == PortalEvent.SESSION_DONE) {
         try{
            CGroupsManagerSessionData sd = sessionData;
            if (sd.lockedGroup != null){
               sd.lockedGroup.getLock().release();
               sd.lockedGroup = null;
               //GroupsManagerCommandFactory.get("Unlock").execute(sd);
            }
            if (sd.servantChannel != null){
               sd.servantChannel.receiveEvent(ev);
            }
         } catch (Exception e){
            Utility.logMessage("ERROR", this.getClass().getName() + "::receiveEvent(): Exception = " + e, e);
         }
      }
   }

   /**
    * Ask channel to render its content.
    * @param out the SAX ContentHandler to output content to
    * @param uid
    * @throws PortalException
    */
   public void renderXML (ContentHandler out) throws PortalException {
      Utility.logMessage("DEBUG", this.getClass().getName() + "::renderXML(): this = " + this);
      CGroupsManagerSessionData sessionData = getSessionData();
      ChannelRuntimeData runtimeData= sessionData.runtimeData;
      long time1 = Calendar.getInstance().getTime().getTime();
      long time2 = 0;
      Document viewDoc = null;
      try {
         if (sessionData.servantChannel != null) {
            ((IChannel)sessionData.servantChannel).renderXML(out);
            Utility.logMessage("DEBUG", this.getClass().getName() + ".renderXML(): Defering to servant render");
         }
         else {
            viewDoc = sessionData.model;
            time2 = Calendar.getInstance().getTime().getTime();
            XSLT xslt = XSLT.getTransformer(this, runtimeData.getLocales());
            xslt.setXML(viewDoc);
            xslt.setTarget(out);
            xslt.setStylesheetParameter("baseActionURL", sessionData.runtimeData.getBaseActionURL());
           if (sessionData.isAdminUser) {
               xslt.setStylesheetParameter("ignorePermissions", "true");
            }
            if (sessionData.customMessage !=null) {
               xslt.setStylesheetParameter("customMessage", sessionData.customMessage);
            }
            xslt.setStylesheetParameter("mode", sessionData.mode);
            xslt.setStylesheetParameter("page", String.valueOf(sessionData.currentPage));
            if (sessionData.highlightedGroupID != null) {
               xslt.setStylesheetParameter("highlightedGroupID", sessionData.highlightedGroupID);
            }
            if (sessionData.rootViewGroupID != null) {
               xslt.setStylesheetParameter("rootViewGroupID", sessionData.rootViewGroupID);
            }
            else if (sessionData.defaultRootViewGroupID != null){
              xslt.setStylesheetParameter("rootViewGroupID", sessionData.defaultRootViewGroupID);
            }
            if (sessionData.feedback != null) {
               xslt.setStylesheetParameter("feedback", sessionData.feedback);
               sessionData.feedback = null;
            }
            if (sessionData.servantMode) {
               xslt.setStylesheetParameter("grpServantMode", "true");
            }
            if (!sessionData.allowFinish) {
              xslt.setStylesheetParameter("blockFinishActions", "true");
            }
            // now handled in the permissions policy
            //if (sessionData.blockEntitySelect) {
            //  xslt.setStylesheetParameter("blockEntitySelect", "true");
            //}
            try {
               //Utility.logMessage("DEBUG", this.getClass().getName()
               //        + ".renderXML(): grpView=" + runtimeData.getParameter("grpView"));
               xslt.setXSL(sslLocation, "main", sessionData.runtimeData.getBrowserInfo());
               xslt.transform();
            }
            catch (PortalException pe){
               Utility.logMessage("ERROR", pe.toString(), pe);
               if (pe.getCause()!=null){
                Utility.logMessage("ERROR", pe.getCause().toString(), pe.getCause());
               }
            }
            catch (Exception e) {
               Utility.logMessage("ERROR", e.toString(), e);
            }
            //Utility.printDoc(viewDoc, "viewXMl ready:\n");

            Utility.logMessage("DEBUG","CGroupsManager::renderXML(): Servant services complete");
            /* @todo remove following print statement */
            Utility.printDoc(viewDoc, "CGroupsManager::renderXML(): Final document state:");
         }
      } catch (Exception e) {
         Utility.logMessage("ERROR", e.toString(), e);
      }
      //Utility.logMessage("DEBUG", this.getClass().getName() + "::renderXML(): Finished with Groups Management");
      //Utility.logMessage("DEBUG", this.getClass().getName() + "::renderXML(): =-+_=-+_=-+_=-+_=-+_=-+_=-+_=-+_ XXXXXXXXXXXXXX _=-+_=-+_=-+_=-+_=-+_=-+_=-+_=-+_");
      long time3 = Calendar.getInstance().getTime().getTime();
      Utility.logMessage("DEBUG", this.getClass().getName() + ".renderXML() timer: "
            + String.valueOf((time3 - time1)) + " ms total, xsl took " + String.valueOf((
            time3 - time2)) + " ms for view " + runtimeData.getParameter("grpView"));
      Utility.logMessage("DEBUG", this.getClass().getName() + ".renderXML() time since setRD: "
            + String.valueOf((time3 - sessionData.startRD)));
      return;
   }

   /**
    * Passes ChannelRuntimeData to the channel.
    * This function is called prior to the renderXML() call.
    * @param rd channel runtime data
    * @see ChannelRuntimeData
    * @param uid
    */
   public void setRuntimeData (ChannelRuntimeData rd) {
      Utility.logMessage("DEBUG", this.getClass().getName() + "::setRuntimeData(): this = " + this);
      CGroupsManagerSessionData sessionData = getSessionData();
      sessionData.runtimeData = rd;
      ChannelRuntimeData runtimeData= sessionData.runtimeData;
      sessionData.startRD = Calendar.getInstance().getTime().getTime();
      if(sessionData.servantChannel == null){
        if (Utility.hasValue(runtimeData.getParameter("grpCommand"))) {
            String theCommand = runtimeData.getParameter("grpCommand");
            Utility.logMessage("DEBUG", this.getClass().getName() + "::renderXML(): COMMAND PROCESS About to get the'"
                  + theCommand + "' command");
            IGroupsManagerCommand c = GroupsManagerCommandFactory.get(theCommand);
            Utility.logMessage("DEBUG", this.getClass().getName() + "::renderXML(): Got the '"
                  + theCommand + "' command = " + (c != null));
            if (c != null) {
               Utility.logMessage("DEBUG", this.getClass().getName() + "::renderXML(): setup parms and about to execute command");
               try{
                c.execute(sessionData);
               }
               catch(Exception e){
                  Utility.logMessage("ERROR", e.toString(), e);
                  sessionData.feedback = "Error executing command "+theCommand+": "+e.getMessage();
               }
            }
         }
         if (Utility.hasValue(runtimeData.getParameter("grpPageForward"))){
            sessionData.currentPage += Integer.parseInt(runtimeData.getParameter("grpPageForward"));
         }
         if (Utility.hasValue(runtimeData.getParameter("grpPageBack"))){
            sessionData.currentPage -= Integer.parseInt((String)runtimeData.getParameter("grpPageBack"));
         }
      }

      if (sessionData.servantChannel != null) {
         try {
            Utility.logMessage("DEBUG", this.getClass().getName() + ".setRuntimeData(): Setting Servant runtimedata");
            ((IChannel)sessionData.servantChannel).setRuntimeData(sessionData.runtimeData);
            if (sessionData.servantChannel.isFinished()) {
               sessionData.servantChannel = null;
               // flushing runtimedata for case where GroupsManager is master and servant, to prevent dirtiness
               Enumeration rd2 = runtimeData.keys();
               while (rd2.hasMoreElements()) {
                  runtimeData.remove(rd2.nextElement());
               }
               Utility.logMessage("DEBUG", this.getClass().getName() +
                     ".setRuntimeData(): removed servant");
            }
            else {
               Utility.logMessage("DEBUG", this.getClass().getName() +
                     ".setRuntimeData(): servant Not Finished");
            }
         } catch (Exception e) {
            Utility.logMessage("ERROR", this.getClass().getName() + ".setRuntimeDat(): Problem setting servant runtimedata "
                  + e, e);
         }
      }
   }

   /**
    * Passes ChannelStaticData to the channel.
    * This is done during channel instantiation time.
    * see org.jasig.portal.StaticData
    * @param sd channel static data
    * @see ChannelStaticData
    * @param uid
    */
   public void setStaticData (ChannelStaticData sd) {
      try{
         CGroupsManagerSessionData sessionData = getSessionData();
         Utility.logMessage("DEBUG", this.getClass().getName() + "::setStaticData(): this = " + this);
         Utility.logMessage("DEBUG", this.getClass().getName() + "::setStaticData(): session Data = " + sessionData);
         Utility.logMessage("DEBUG", this.getClass().getName() + "::setStaticData(): sd = " + sd);
         sessionData.staticData = sd;
         IEntityGroup admin = GroupService.getDistinguishedGroup(GroupService.PORTAL_ADMINISTRATORS);
         IGroupMember currUser = AuthorizationService.instance().getGroupMember(sessionData.staticData.getAuthorizationPrincipal());
         sessionData.isAdminUser = (admin.deepContains(currUser));
         sessionData.user = sessionData.staticData.getPerson();
         sessionData.authPrincipal = sd.getAuthorizationPrincipal();
         sessionData.model = GroupsManagerXML.getGroupsManagerXml(sessionData);
         Utility.logMessage("DEBUG", this.getClass().getName() + "::setStaticData(): staticData Person ID = "
               + sessionData.user.getID());
         Iterator i = sessionData.staticData.entrySet().iterator();
         while (i.hasNext()) {
            Map.Entry m = (Map.Entry)i.next();
            Utility.logMessage("DEBUG", this.getClass().getName() + "::setStaticData(): staticData "
                  + m.getKey() + " = " + m.getValue());
         }
      } catch (Exception e) {
          Utility.logMessage("ERROR", e.toString(), e);
      }
   }

   /**
    * put your documentation comment here
    * @return String
    */
   public String getOwnerName () {
      return  "Groups Manager";
   }

   /**
    * put your documentation comment here
    * @return String[]
    */
   public String[] getActivityTokens () {
      init();
      return  (String[])activities.keySet().toArray(new String[0]);
   }

   /**
    * put your documentation comment here
    * @return String
    */
   public String getOwnerToken () {
      return  OWNER;
   }

   /**
    * put your documentation comment here
    * @param token
    * @return String
    */
   public String getActivityName (String token) {
      return  (String)activities.get(token);
   }

   /**
    * put your documentation comment here
    * @return String[]
    */
   public String[] getTargetTokens () {
      init();
      return  (String[])targets.keySet().toArray(new String[0]);
   }

   /**
    * put your documentation comment here
    * @param token
    * @return String
    */
   public String getTargetName (String token) {
      String r = (String) targets.get(token);
      if (r ==null){
        try{
          r= EntityNameFinderService.instance().getNameFinder(IEntityGroup.class).getName(token);
        }
        catch (Exception e){
          Utility.logMessage("ERROR", e.toString(), e);
        }
      }
      return  r;
   }

   /**
    * put your documentation comment here
    * @param uid
    * @return ChannelCacheKey
    */
   public ChannelCacheKey generateKey () {
      Utility.logMessage("DEBUG", this.getClass().getName() + "::generateKey(): this = " + this);
      CGroupsManagerSessionData sessionData = getSessionData();
      ChannelStaticData staticData = sessionData.staticData;
      ChannelCacheKey cck;
      if (sessionData.servantChannel == null) {
         cck = new ChannelCacheKey();
         cck.setKey(staticData.getChannelPublishId()
            + "-"+staticData.getChannelSubscribeId()
            + "-" + String.valueOf(staticData.getPerson().getID()));
         //   + "-" + Calendar.getInstance().getTime().getTime()));
         cck.setKeyValidity(vKey());
         Utility.logMessage("DEBUG", "CGroupsManager.generateKey():: [NO SERVANT] key = " + cck.getKey());
      }
      else {
         cck = ((ICacheable)sessionData.servantChannel).generateKey();
         Utility.logMessage("DEBUG", "CGroupsManager.generateKey():: [SERVANT] key = " + cck.getKey());
      }
      Utility.logMessage("DEBUG", "CGroupsManager.generateKey():: ChannelCacheKey.getKeyValidity = " + cck.getKeyValidity());
      return  cck;
   }

   /**
    * put your documentation comment here
    * @param uid
    * @return String
    */
   private String vKey () {
      Utility.logMessage("DEBUG", this.getClass().getName() + "::vKey(): this = " + this);
      CGroupsManagerSessionData sessionData = getSessionData();
      String vkey = sessionData.currentPage+" - "+sessionData.feedback+" - "+sessionData.highlightedGroupID+" - "
        +sessionData.mode+" - "+sessionData.rootViewGroupID;
      Utility.logMessage("DEBUG", this.getClass().getName() + ".vKey() : vKey returns = " + vkey);
      return  vkey;
   }

   /**
    * put your documentation comment here
    * @param validity
    * @param uid
    * @return boolean
    */
   public boolean isCacheValid (Object validity) {
      Utility.logMessage("DEBUG", this.getClass().getName() + "::isCacheValid(): this = " + this);
      CGroupsManagerSessionData sessionData = getSessionData();
      ChannelRuntimeData runtimeData= sessionData.runtimeData;
      boolean valid = false;
      if (sessionData.servantChannel == null) {
         if (validity != null) {
            if (validity.equals(vKey()) && runtimeData.get("grpCommand") == null) {
               valid = true;
            }
         }
         long time3 = Calendar.getInstance().getTime().getTime();
         Utility.logMessage("DEBUG", this.getClass().getName() + ".isCacheValid() time since setRD: "
               + String.valueOf((time3 - sessionData.startRD)) + ", valid=" + valid);
      }
      else {
         valid = ((ICacheable)sessionData.servantChannel).isCacheValid(validity);
      }
      return valid;
   }

   /**
    * Returns the session data for a thread
    * @param uid
    * @return CGroupsManagerSessionData
    */
   public synchronized CGroupsManagerSessionData getSessionData () {
      CGroupsManagerSessionData sd = sessionData;
      if (sd == null) {
         sd =  new CGroupsManagerSessionData();
         sd.permissible = this;
         sessionData = sd;
      }
      Utility.logMessage("DEBUG", this.getClass().getName() + "::getSessionData(): sd = " + sd);
      return sd;
   }
}



