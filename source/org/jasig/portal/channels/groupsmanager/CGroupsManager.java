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

/**
 * <p>Title: uPortal</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Columbia University</p>
 * @author Don Fracapane
 * @version 2.0
 */
import  java.util.*;
import  java.io.*;
import  org.jasig.portal.services.*;
import  org.apache.log4j.Priority;
import  org.jasig.portal.*;
import  org.jasig.portal.security.*;
import  org.jasig.portal.security.provider.*;
import  org.jasig.portal.utils.*;
import  org.jasig.portal.channels.permissionsmanager.*;
import  org.jasig.portal.groups.*;
import  org.w3c.dom.Node;
import  org.w3c.dom.NodeList;
import  org.w3c.dom.Element;
import  org.w3c.dom.Text;
import  org.apache.xerces.parsers.DOMParser;
import  org.apache.xerces.parsers.SAXParser;
import  org.w3c.dom.Document;
import  org.apache.xml.serialize.XMLSerializer;
import  org.apache.xml.serialize.OutputFormat;
import  org.xml.sax.ContentHandler;
import  org.xml.sax.InputSource;

/**
 * CGroupsManager allows users to graphically administer all groups for which
 * user has administrtaive permissions.
 */
public class CGroupsManager
      implements org.jasig.portal.IMultithreadedChannel, GroupsManagerConstants, IPermissible, IMultithreadedCacheable {
   // Location of the stylesheet list file
   protected static final String sslLocation = "CGroupsManager.ssl";
   protected static HashMap activities = null;
   protected HashMap targets = null;
   protected HashMap sessionsMap = new HashMap();

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
            activities.put("CREATE", "Create a group in this context");
            activities.put("VIEW", "View this group");
            activities.put("UPDATE", "Rename this group");
            activities.put("DELETE", "Delete this group");
            activities.put("SELECT", "Select this group");
            activities.put("ADD/REMOVE", "Manage this group's members");
            activities.put("ASSIGNPERMISSIONS", "Assign Permissions for this group");
         } catch (Exception e) {
            Utility.logMessage("ERROR", "CGroupsManager.init():: unable to set activities"
                  + e);
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
                              + e);
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
                              + e);
                     }
                  }
               }
            }
         }
      } catch (Exception e) {
         Utility.logMessage("ERROR", "CGroupsManager.init():: unable to set targets"
               + e);
      }
   }

   /**
    * Acquires ChannelRuntimeProperites from the channel.
    * This function may be called by the portal framework throughout the session.
    * @see ChannelRuntimeProperties
    * @param uid
    * @return ChannelRuntimeProperties
    */
   public ChannelRuntimeProperties getRuntimeProperties (String uid) {
      return  new ChannelRuntimeProperties();
   }

   /**
    * Passes an outside event to a channel.
    * Events should normally come from the LayoutBean.
    * @param ev PortalEvent object
    * @param uid
    * @see PortalEvent
    */
   public void receiveEvent (PortalEvent ev, String uid)
   //public void receiveEvent(LayoutEvent ev)
   {
      if (ev.getEventNumber() == ev.EDIT_BUTTON_EVENT) {
      // Switch to edit mode
      //m_currentState = EDITMODE;
      // Clear the content cache
      //m_cachedContent = null;
      }
   }

   /**
    * Get the viewDoc from staticData, or generate if null
    * @param uid
    * @return Document
    */
   protected Document getViewDoc (String uid) {
      Utility.logMessage("DEBUG", this.getClass().getName() + "::getViewDoc(): this = " + this);
      CGroupsManagerSessionData sessionData = getSessionData(uid);
      ChannelStaticData staticData = sessionData.staticData;
      ChannelRuntimeData runtimeData= sessionData.runtimeData;
      Document viewDoc = null;
      try {
         Utility.logMessage("DEBUG", this.getClass().getName() + "::getViewDoc() groups management START");
         //String key = "xmlDoc";
         //viewDoc = (Document)staticData.get((Object)key);
         viewDoc = (Document)sessionData.model;
         // get the groups data in xml format
         if (viewDoc == null) {
            Utility.logMessage("DEBUG", this.getClass().getName() + "::getViewDoc() about to get xmlDoc");
            viewDoc = GroupsManagerXML.getGroupsManagerXml(runtimeData, staticData);
            //staticData.put(key, viewDoc);
            sessionData.model = viewDoc;
            Utility.logMessage("DEBUG", this.getClass().getName() + "::getViewDoc(): view doc was created");
         }
         /** @todo why would we store what we just retrieved????? */
//         else {
//            staticData.put(key, viewDoc);
//            Utility.logMessage("DEBUG", this.getClass().getName() + "::getViewDoc(): view doc was cached");
//         }
      } catch (Exception e) {
         Utility.logMessage("ERROR", this.getClass().getName() + "::getViewDoc():  \n"
               + e);
      }
      return  viewDoc;
   }

   /**
    * Ask channel to render its content.
    * @param out the SAX ContentHandler to output content to
    * @exception PortalException
    * @param uid
    */
   public void renderXML (ContentHandler out, String uid) throws PortalException {
      Utility.logMessage("DEBUG", this.getClass().getName() + "::renderXML(): this = " + this);
      CGroupsManagerSessionData sessionData = getSessionData(uid);
      ChannelStaticData staticData = sessionData.staticData;
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
            viewDoc = getViewDoc(uid);
            time2 = Calendar.getInstance().getTime().getTime();
            XSLT xslt = new XSLT(this);
            xslt.setXML(viewDoc);
            xslt.setTarget(out);
            xslt.setStylesheetParameter("baseActionURL", sessionData.runtimeData.getBaseActionURL());
            IEntityGroup admin = GroupService.getDistinguishedGroup(GroupService.PORTAL_ADMINISTRATORS);
            IGroupMember currUser = AuthorizationService.instance().getGroupMember(sessionData.staticData.getAuthorizationPrincipal());
            if (admin.deepContains(currUser)) {
               xslt.setStylesheetParameter("ignorePermissions", "true");
            }
            if (hasValue(sessionData.staticData.getParameter("grpServantMessage"))) {
               Utility.logMessage("DEBUG", "CgroupsManagerServant - setting grpServantMessage to "
                     + sessionData.staticData.getParameter("grpServantMessage"));
               xslt.setStylesheetParameter("grpServantMessage", sessionData.staticData.getParameter("grpServantMessage"));
            }
            if (sessionData.runtimeData.getParameter("grpMode") != null) {
               xslt.setStylesheetParameter("grpMode", sessionData.runtimeData.getParameter("grpMode"));
            }
            if (sessionData.runtimeData.getParameter("grpViewId") != null) {
               xslt.setStylesheetParameter("grpViewId", sessionData.runtimeData.getParameter("grpViewId"));
            }
            if (sessionData.runtimeData.getParameter("commandResponse") != null) {
               xslt.setStylesheetParameter("commandResponse", sessionData.runtimeData.getParameter("commandResponse"));
            }
            if (sessionData.staticData.getParameter("grpServantMode") != null && sessionData.staticData.getParameter("grpServantMode").equals("true")) {
               xslt.setStylesheetParameter("grpServantMode", "true");
            }
            if (hasValue(sessionData.staticData.getParameter("grpAllowFinish"),"false")) {
              xslt.setStylesheetParameter("blockFinishActions", "true");
            }
            if (hasValue(sessionData.staticData.getParameter("grpBlockEntitySelect"),"true")) {
              xslt.setStylesheetParameter("blockEntitySelect", "true");
            }
            try {
               //Utility.logMessage("DEBUG", this.getClass().getName()
               //        + ".renderXML(): grpView=" + runtimeData.getParameter("grpView"));
               xslt.setXSL(sslLocation, sessionData.runtimeData.getParameter("grpView"), sessionData.runtimeData.getBrowserInfo());
               xslt.transform();
            } catch (Exception e) {
               LogService.instance().log(LogService.ERROR, e);
            }
            //StringWriter sw = new StringWriter();
            //XMLSerializer serial = new XMLSerializer(sw, new org.apache.xml.serialize.OutputFormat(viewDoc,"UTF-8", true));
            //serial.serialize(viewDoc);
            //Utility.logMessage("DEBUG", "viewXMl ready:\n"+sw.toString());

            Utility.logMessage("DEBUG","CGroupsManager::renderXML(): Servant services complete");
            //Utility.printDoc(viewDoc, "CGroupsManager::renderXML(): Final document state:");
         }
      } catch (Exception e) {
         LogService.instance().log(LogService.ERROR, e);
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
   public void setRuntimeData (ChannelRuntimeData rd, String uid) {
      Utility.logMessage("DEBUG", this.getClass().getName() + "::setRuntimeData(): this = " + this);
      CGroupsManagerSessionData sessionData = getSessionData(uid);
      sessionData.runtimeData = rd;
      ChannelStaticData staticData = sessionData.staticData;
      ChannelRuntimeData runtimeData= sessionData.runtimeData;
      sessionData.startRD = Calendar.getInstance().getTime().getTime();
      //Iterator i = this.runtimeData.entrySet().iterator();
      //while (i.hasNext())
      //{
      //  Entry m = (Entry) i.next();
      //  Utility.logMessage("DEBUG","CGroupsManager::setRuntimeData(): runtimeData "+m.getKey()+" = "+m.getValue());
      //}
      // start servant code
      ChannelRuntimeData slaveRD = runtimeData;
      if (hasValue(runtimeData.getParameter("grpView"), "AssignPermissions") && sessionData.servantChannel
            == null) {
         try {
            Utility.logMessage("DEBUG", this.getClass().getName() + ".setRuntimeData() : generating permissions servant "
                  + staticData.getParameter("grpView"));
            String[] tgts = new String[1];
            tgts[0] = (runtimeData.getParameter("grpViewKey"));
            sessionData.servantChannel = CPermissionsManagerServantFactory.getPermissionsServant((IPermissible)Class.forName(OWNER).newInstance(),
                  staticData, null, null, tgts);
            slaveRD = (ChannelRuntimeData)runtimeData.clone();
            Enumeration srd = slaveRD.keys();
            while (srd.hasMoreElements()) {
               slaveRD.remove(srd.nextElement());
            }

         } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, e);
         }
      }
      if (sessionData.servantChannel != null) {
         try {
            Utility.logMessage("DEBUG", this.getClass().getName() + ".setRuntimeData(): Setting Servant runtimedata");
            ((IChannel)sessionData.servantChannel).setRuntimeData(slaveRD);
            if (sessionData.servantChannel.isFinished()) {
               sessionData.servantChannel = null;
               // flushing runtimedata for case where GroupsManager is master and servant, to prevent dirtiness
               Enumeration rd2 = runtimeData.keys();
               while (rd2.hasMoreElements()) {
                  runtimeData.remove(rd2.nextElement());
               }
               //runtimeData.setParameter("view",getFromView(staticData.getParameter("view")));
               //staticData.remove("view");
               Utility.logMessage("DEBUG", this.getClass().getName() +
                     ".setRuntimeData(): removed servant");
            }
            else {
               Utility.logMessage("DEBUG", this.getClass().getName() +
                     ".setRuntimeData(): servant Not Finished");
            }
         } catch (Exception e) {
            Utility.logMessage("ERROR", this.getClass().getName() + ".setRuntimeDat(): Problem setting servant runtimedata "
                  + e);
         }
      }
      //end servant code
      if (sessionData.servantChannel == null) {
         if (!hasValue(runtimeData.getParameter("grpView")) || hasValue(runtimeData.getParameter("grpView"),
               "AssignPermissions")) {
            if (hasValue(staticData.get("grpView"))) {
               Utility.logMessage("DEBUG", this.getClass().getName() +
                     ".setRuntimeData() : using cached grpView " + staticData.getParameter("grpView"));
               runtimeData.setParameter("grpView", staticData.getParameter("grpView"));
            }
            else {
               runtimeData.setParameter("grpView", "tree");
            }
         }
         runtimeData.setParameter("username", "guest");
         if (sessionData.user != null) {
            runtimeData.setParameter("username", String.valueOf(sessionData.user.getID()));
         }
         if (!hasValue(runtimeData.getParameter("grpMode"))) {
            if (hasValue(staticData.get("grpMode"))) {
               Utility.logMessage("DEBUG", this.getClass().getName() +
                     ".setRuntimeData() : using cached grpMode " + staticData.getParameter("grpMode"));
               runtimeData.setParameter("grpMode", staticData.getParameter("grpMode"));
            }
            else {
               runtimeData.setParameter("grpMode", "browse");
            }
         }
         if (!hasValue(runtimeData.getParameter("grpCommand"))) {
            runtimeData.remove("grpCommand");
         }
         if (!hasValue(runtimeData.getParameter("grpCommandIds"))) {
            runtimeData.remove("grpCommandIds");
         }
         if (!hasValue(runtimeData.getParameter("grpName"))) {
            runtimeData.remove("grpName");
         }

         if (hasValue(runtimeData.getParameter("grpCommand"))) {
            GroupsManagerCommandFactory cf = GroupsManagerCommandFactory.instance();
            String theCommand = runtimeData.getParameter("grpCommand");
            Utility.logMessage("DEBUG", this.getClass().getName() + "::renderXML(): COMMAND PROCESS About to get the'"
                  + theCommand + "' command");
            IGroupsManagerCommand c = cf.get(theCommand);
            Utility.logMessage("DEBUG", this.getClass().getName() + "::renderXML(): Got the '"
                  + theCommand + "' command = " + (c != null));
            if (c != null) {
               Utility.logMessage("DEBUG", this.getClass().getName() + "::renderXML(): setup parms and about to execute command");
               c.execute(sessionData);
            }
         }
         if (!hasValue(runtimeData.getParameter("grpViewKey"))) {
            if (hasValue(staticData.getParameter("grpViewKey"))) {
               runtimeData.setParameter("grpViewKey", staticData.getParameter("grpViewKey"));
            }
            else {
               runtimeData.remove("grpViewKey");
            }
         }
         if (hasValue(runtimeData.getParameter("grpViewKey"))) {
            Document viewDoc = getViewDoc(uid);
            String grpKey = runtimeData.getParameter("grpViewKey");
            Element grpViewKeyElem;
            Iterator grpItr = GroupsManagerXML.getNodesByTagNameAndKey(viewDoc, GROUP_TAGNAME,
                  grpKey);
            IEntityGroup gm = GroupsManagerXML.retrieveGroup(grpKey);
            if (gm != null) {
               if (!grpItr.hasNext()) {
                  grpViewKeyElem = GroupsManagerXML.getGroupMemberXml(gm, true, null,
                        viewDoc);
                  Element rootElem = viewDoc.getDocumentElement();
                  rootElem.appendChild(grpViewKeyElem);
               }
               else {
                  grpViewKeyElem = (Element)grpItr.next();
                  GroupsManagerXML.getGroupMemberXml(gm, true, grpViewKeyElem, viewDoc);
               }
               runtimeData.setParameter("grpViewId", grpViewKeyElem.getAttribute("id"));
               staticData.remove("grpViewKey");
            }
            else {
               runtimeData.put("commandResponse", "Unable to locate requested group.");
            }
         }
         if (hasValue(staticData.get("grpPreSelectForMember"))){
            Document viewDoc = getViewDoc(uid);
            Element rootElem = viewDoc.getDocumentElement();
            try{
              IGroupMember gm = (IGroupMember) staticData.get("grpPreSelectForMember");
              Iterator parents = gm.getContainingGroups();
              IEntityGroup parent;
              while (parents.hasNext()){
                 parent = (IEntityGroup) parents.next();
                 Element parentElem = GroupsManagerXML.getGroupMemberXml(parent,false,null,viewDoc);
                 parentElem.setAttribute("selected","true");
                 rootElem.appendChild(parentElem);
              }
            }
            catch (Exception e){
              LogService.instance().log(LogService.ERROR,e);
            }
            staticData.remove("grpPreSelectForMember");
         }
         if (hasValue(staticData.get("grpPreSelectedMembers"))){
            Document viewDoc = getViewDoc(uid);
            Element rootElem = viewDoc.getDocumentElement();
            try{
                IGroupMember[] mems = (IGroupMember[])staticData.get("grpPreSelectedMembers");
                for (int mm = 0; mm< mems.length;mm++){
                  IGroupMember mem = mems[mm];
                  Element memelem = GroupsManagerXML.getGroupMemberXml(mem,false,null,viewDoc);
                  memelem.setAttribute("selected","true");
                  rootElem.appendChild(memelem);
                }
            }
            catch (Exception e){
              LogService.instance().log(LogService.ERROR,e);
            }
            staticData.remove("grpPreSelectedMembers");
         }
         if (!hasValue(runtimeData.getParameter("grpViewId"))) {
            if (hasValue(staticData.get("grpViewId"))) {
               Utility.logMessage("DEBUG", this.getClass().getName() +
                     ".setRuntimeData() : using cached grpViewID " + staticData.getParameter("grpViewId"));
               runtimeData.setParameter("grpViewId", staticData.getParameter("grpViewId"));
            }
            else {
               runtimeData.remove("grpViewId");
            }
         }
         else {
            staticData.setParameter("grpViewId", runtimeData.getParameter("grpViewId"));
         }
         staticData.setParameter("grpMode", runtimeData.getParameter("grpMode"));
         staticData.setParameter("grpView", runtimeData.getParameter("grpView"));         /*
          Enumeration names = runtimeData.getParameterNames();
          while (names.hasMoreElements()) {
          String n = (String)names.nextElement();
          Utility.logMessage("DEBUG", this.getClass().getName()
          + ".setRuntimeData() RuntimeData paramaters: " + n +
          " = " + runtimeData.getParameter(n));
          }
          Enumeration names2 = staticData.keys();
          while (names2.hasMoreElements()) {
          String n2 = (String)names2.nextElement();
          Utility.logMessage("DEBUG", this.getClass().getName()
          + ".setRuntimeData() StaticData paramaters: " + n2 +
          " = " + staticData.get(n2).toString());
          }
          */

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
   public void setStaticData (ChannelStaticData sd, String uid) {
      CGroupsManagerSessionData sessionData = getSessionData(uid);
      Utility.logMessage("DEBUG", this.getClass().getName() + "::setStaticData(): this = " + this);
      Utility.logMessage("DEBUG", this.getClass().getName() + "::setStaticData(): session Data = " + sessionData);
      Utility.logMessage("DEBUG", this.getClass().getName() + "::setStaticData(): sd = " + sd);
      Utility.logMessage("DEBUG", this.getClass().getName() + "::setStaticData(): uid = " + uid);
      sessionData.staticData = sd;
      //ChannelStaticData staticData = sessionData.staticData;
      sessionData.user = sessionData.staticData.getPerson();
      Utility.logMessage("DEBUG", this.getClass().getName() + "::setStaticData(): staticData Person ID = "
            + sessionData.user.getID());
      Iterator i = sessionData.staticData.entrySet().iterator();
      while (i.hasNext()) {
         Map.Entry m = (Map.Entry)i.next();
         Utility.logMessage("DEBUG", this.getClass().getName() + "::setStaticData(): staticData "
               + m.getKey() + " = " + m.getValue());
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
      return  (String)targets.get(token);
   }

   /**
    * put your documentation comment here
    * @param o
    * @return boolean
    */
   protected boolean hasValue (Object o) {
      boolean rval = false;
      if (o != null && !o.toString().trim().equals("")) {
         rval = true;
      }
      return  rval;
   }

   /**
    * put your documentation comment here
    * @param o
    * @param test
    * @return boolean
    */
   protected boolean hasValue (Object o, String test) {
      boolean rval = false;
      if (hasValue(o)) {
         if (String.valueOf(o).equals(test)) {
            rval = true;
         }
      }
      return  rval;
   }

   /**
    * put your documentation comment here
    * @param uid
    * @return ChannelCacheKey
    */
   public ChannelCacheKey generateKey (String uid) {
      Utility.logMessage("DEBUG", this.getClass().getName() + "::generateKey(): this = " + this);
      CGroupsManagerSessionData sessionData = getSessionData(uid);
      ChannelStaticData staticData = sessionData.staticData;
      ChannelRuntimeData runtimeData= sessionData.runtimeData;
      ChannelCacheKey cck;
      Utility.logMessage("DEBUG", "CGroupsManager.generateKey():: uid parm = " + uid);
      if (sessionData.servantChannel == null) {
         cck = new ChannelCacheKey();
         cck.setKey(staticData.getChannelPublishId()
            + "-"+staticData.getChannelSubscribeId()
            + "-" + String.valueOf(staticData.getPerson().getID()));
         //   + "-" + Calendar.getInstance().getTime().getTime()));
         cck.setKeyValidity(vKey(uid));
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
   private String vKey (String uid) {
      Utility.logMessage("DEBUG", this.getClass().getName() + "::vKey(): this = " + this);
      CGroupsManagerSessionData sessionData = getSessionData(uid);
      ChannelStaticData staticData = sessionData.staticData;
      ChannelRuntimeData runtimeData= sessionData.runtimeData;
      String vkey = runtimeData.getParameter("grpView") + " - " + runtimeData.getParameter("grpViewId")
            + " - " + runtimeData.getParameter("grpMode");
      Utility.logMessage("DEBUG", this.getClass().getName() + ".vKey() : vKey returns = " + vkey);
      return  vkey;
   }

   /**
    * put your documentation comment here
    * @param validity
    * @param uid
    * @return boolean
    */
   public boolean isCacheValid (Object validity, String uid) {
      Utility.logMessage("DEBUG", this.getClass().getName() + "::isCacheValid(): this = " + this);
      CGroupsManagerSessionData sessionData = getSessionData(uid);
      ChannelStaticData staticData = sessionData.staticData;
      ChannelRuntimeData runtimeData= sessionData.runtimeData;
      boolean valid = false;
      if (sessionData.servantChannel == null) {
         if (validity != null) {
            if (validity.equals(vKey(uid)) && runtimeData.get("grpCommand") == null) {
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
      return  valid;
   }

   /**
    * Returns the session data for a thread
    * @param uid
    * @return CGroupsManagerSessionData
    */
   public synchronized CGroupsManagerSessionData getSessionData (String uid) {
      CGroupsManagerSessionData sd = (CGroupsManagerSessionData) sessionsMap.get(uid);
      if (sd == null) {
         sd =  new CGroupsManagerSessionData();
         sd.uid = uid;
         sessionsMap.put(uid, sd);
      }
      Utility.logMessage("DEBUG", this.getClass().getName() + "::getSessionData(): sd = " + sd);
      return sd;
   }
}



