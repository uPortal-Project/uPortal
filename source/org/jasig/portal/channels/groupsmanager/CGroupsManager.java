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
import  org.jasig.portal.ICacheable;
import  org.jasig.portal.ChannelCacheKey;
import  org.jasig.portal.IChannel;
import  org.jasig.portal.ChannelRuntimeData;
import  org.jasig.portal.ChannelRuntimeProperties;
import  org.jasig.portal.StylesheetSet;
import  org.jasig.portal.ChannelStaticData;
import  org.jasig.portal.PortalException;
import  org.jasig.portal.PortalEvent;
import  org.jasig.portal.IServant;
import  org.jasig.portal.IPermissible;
import  org.jasig.portal.security.*;
import  org.jasig.portal.security.provider.*;
import  org.jasig.portal.utils.*;
import  org.jasig.portal.channels.permissionsmanager.CPermissionsManagerServantFactory;
import  org.jasig.portal.groups.IGroupMember;
import  org.jasig.portal.groups.IEntityGroup;
import  org.jasig.portal.groups.IGroupMember;
import  org.w3c.dom.Node;
import  org.w3c.dom.NodeList;
import  org.w3c.dom.Element;
import  org.w3c.dom.Text;
import  org.apache.xerces.parsers.DOMParser;
import  org.apache.xerces.parsers.SAXParser;
import  org.apache.xerces.dom.DocumentImpl;
import  org.apache.xml.serialize.XMLSerializer;
import  org.apache.xml.serialize.OutputFormat;
import  org.xml.sax.ContentHandler;
import  org.xml.sax.InputSource;


/**
 * CGroupsManager allows users to graphically administer all groups for which
 * user has administrtaive permissions.
 */
public class CGroupsManager
      implements org.jasig.portal.IChannel, GroupsManagerConstants, IPermissible, ICacheable {
   protected ChannelRuntimeData runtimeData;
   protected ChannelStaticData staticData;
   // Location of the stylesheet list file
   protected static final String sslLocation = "CGroupsManager.ssl";
   protected IPerson user;
   protected static HashMap activities = null;
   protected HashMap targets = null;
   private IServant servantChannel = null;
   private long startRD;

   /** Creates new GroupsManagerChannel */
   public CGroupsManager () {
      init();
   }

   /**
    * put your documentation comment here
    */
   protected synchronized void init () {
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
            LogService.instance().log(LogService.ERROR, "CGroupsManager.init():: unable to set activities"
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
                        LogService.instance().log(LogService.ERROR, "CGroupsManager.init():: unable to add target"
                              + e);
                     }
                  }
               }
            }
         }
      } catch (Exception e) {
         LogService.instance().log(LogService.ERROR, "CGroupsManager.init():: unable to set targets"
               + e);
      }
   }

   /**
    * Acquires ChannelRuntimeProperites from the channel.
    * This function may be called by the portal framework throughout the session.
    * @see ChannelRuntimeProperties
    */
   public ChannelRuntimeProperties getRuntimeProperties () {
      return  new ChannelRuntimeProperties();
   }

   /**
    * Acquires ChannelSubscriptionProperties from the channel.
    * This function should be called at the Publishing/Subscription time.
    * @see ChannelSubscriptionProperties
    */
   //public ChannelSubscriptionProperties getSubscriptionProperties()
   //{
   //  ChannelSubscriptionProperties csp = new ChannelSubscriptionProperties();
   //
   //  csp.setName("GroupsManager");
   //  csp.setEditable(true);
   //
   //  return(csp);
   //}
   /**
    * Passes an outside event to a channel.
    * Events should normally come from the LayoutBean.
    * @param ev PortalEvent object
    * @see PortalEvent
    */
   public void receiveEvent (PortalEvent ev)
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
    *
    */
   protected DocumentImpl getViewDoc () {
      DocumentImpl viewDoc = null;
      try {
         Utility.logMessage("DEBUG", this.getClass().getName() + "::getViewDoc() groups management START");
         String key = "xmlDoc";
         viewDoc = (DocumentImpl)this.staticData.get((Object)key);
         // get the groups data in xml format
         if (viewDoc == null) {
            Utility.logMessage("DEBUG", this.getClass().getName() + "::getViewDoc() about to get xmlDoc");
            viewDoc = GroupsManagerXML.getGroupsManagerXml(runtimeData, staticData);
            // Utility.printDoc(viewDoc, "View doc was CREATED");
            this.staticData.put(key, viewDoc);
            Utility.logMessage("DEBUG", this.getClass().getName() + "::getViewDoc(): view doc was created");
         }
         else {
            // Utility.printDoc(viewDoc, "View doc was PASSED");
            Utility.logMessage("DEBUG", this.getClass().getName() + "::getViewDoc(): view doc was cached");
         }
      } catch (Exception e) {
         Utility.logMessage("ERROR", this.getClass().getName() + "::getViewDoc():  \n"
               + e);
      }
      return  viewDoc;
   }

   /**
    * Ask channel to render its content.
    * @param out the SAX ContentHandler to output content to
    */
   public void renderXML (ContentHandler out) throws PortalException {
      long time1 = Calendar.getInstance().getTime().getTime();
      long time2 = 0;
      DocumentImpl viewDoc = null;
      try {
         if (servantChannel != null) {
            ((IChannel)servantChannel).renderXML(out);
            LogService.instance().log(LogService.DEBUG, this.getClass().getName() + ".renderXML(): Defering to servant render");
         }
         else {
            viewDoc = getViewDoc();
            time2 = Calendar.getInstance().getTime().getTime();
            XSLT xslt = new XSLT(this);
            xslt.setXML(viewDoc);
            xslt.setTarget(out);
            xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
            IEntityGroup admin = GroupService.getDistinguishedGroup(GroupService.PORTAL_ADMINISTRATORS);
            IGroupMember me = AuthorizationService.instance().getGroupMember(staticData.getAuthorizationPrincipal());
            if (admin.deepContains(me)) {
               xslt.setStylesheetParameter("ignorePermissions", "true");
            }
            if (hasValue(staticData.getParameter("grpServantMessage"))) {
               LogService.instance().log(LogService.DEBUG, "CgroupsManagerServant - setting grpServantMessage to "
                     + staticData.getParameter("grpServantMessage"));
               xslt.setStylesheetParameter("grpServantMessage", staticData.getParameter("grpServantMessage"));
            }
            if (runtimeData.getParameter("grpMode") != null) {
               xslt.setStylesheetParameter("grpMode", runtimeData.getParameter("grpMode"));
            }
            if (runtimeData.getParameter("grpViewId") != null) {
               xslt.setStylesheetParameter("grpViewId", runtimeData.getParameter("grpViewId"));
            }
            if (runtimeData.getParameter("commandResponse") != null) {
               xslt.setStylesheetParameter("commandResponse", runtimeData.getParameter("commandResponse"));
            }
            if (staticData.getParameter("grpServantMode") != null && staticData.getParameter("grpServantMode").equals("true")) {
               xslt.setStylesheetParameter("grpServantMode", "true");
            }
            try {
               //LogService.instance().log(LogService.DEBUG, this.getClass().getName()
               //        + ".renderXML(): grpView=" + runtimeData.getParameter("grpView"));
               xslt.setXSL(sslLocation, runtimeData.getParameter("grpView"), runtimeData.getBrowserInfo());
               xslt.transform();
            } catch (Exception e) {
               LogService.instance().log(LogService.ERROR, e);
            }
            /*
            StringWriter sw = new StringWriter();
            XMLSerializer serial = new XMLSerializer(sw, new org.apache.xml.serialize.OutputFormat(viewDoc,"UTF-8", true));
            serial.serialize(viewDoc);
            LogService.log(LogService.DEBUG,"viewXMl ready:\n"+sw.toString());
            */
            //Utility.logMessage("DEBUG","CGroupsManager::renderXML(): Servant services complete");
            //Utility.printDoc(viewDoc, "CGroupsManager::renderXML(): Final document state:");
         }
      } catch (Exception e) {
         LogService.instance().log(LogService.ERROR, e);
      }
      //Utility.logMessage("DEBUG", this.getClass().getName() + "::renderXML(): Finished with Groups Management");
      //Utility.logMessage("DEBUG", this.getClass().getName() + "::renderXML(): =-+_=-+_=-+_=-+_=-+_=-+_=-+_=-+_ XXXXXXXXXXXXXX _=-+_=-+_=-+_=-+_=-+_=-+_=-+_=-+_");
      long time3 = Calendar.getInstance().getTime().getTime();
      LogService.instance().log(LogService.DEBUG, this.getClass().getName() + ".renderXML() timer: "
            + String.valueOf((time3 - time1)) + " ms total, xsl took " + String.valueOf((
            time3 - time2)) + " ms for view " + runtimeData.getParameter("grpView"));
      LogService.instance().log(LogService.DEBUG, this.getClass().getName() + ".renderXML() time since setRD: "
            + String.valueOf((time3 - startRD)));
      return;
   }

   /**
    * Passes ChannelRuntimeData to the channel.
    * This function is called prior to the renderXML() call.
    * @param rd channel runtime data
    * @see ChannelRuntimeData
    */
   public void setRuntimeData (ChannelRuntimeData rd) {
      startRD = Calendar.getInstance().getTime().getTime();
      this.runtimeData = rd;
      //Iterator i = this.runtimeData.entrySet().iterator();
      //while (i.hasNext())
      //{
      //  Entry m = (Entry) i.next();
      //  Utility.logMessage("DEBUG","CGroupsManager::setRuntimeData(): runtimeData "+m.getKey()+" = "+m.getValue());
      //}
      // start servant code
      ChannelRuntimeData slaveRD = runtimeData;
      if (hasValue(runtimeData.getParameter("grpView"), "AssignPermissions") && servantChannel
            == null) {
         try {
            LogService.instance().log(LogService.DEBUG, this.getClass().getName() + ".setRuntimeData() : generating permissions servant "
                  + staticData.getParameter("grpView"));
            String[] tgts = new String[1];
            tgts[0] = (runtimeData.getParameter("grpViewKey"));
            servantChannel = CPermissionsManagerServantFactory.getPermissionsServant((IPermissible)Class.forName(OWNER).newInstance(),
                  staticData, null, null, tgts);
            slaveRD = (ChannelRuntimeData)runtimeData.clone();
            Enumeration srd = slaveRD.keys();
            while (srd.hasMoreElements()) {
               slaveRD.remove(srd.nextElement());
            }            /*
             ChannelStaticData slaveSD = (ChannelStaticData)staticData.clone();
             Enumeration sdkeys = slaveSD.keys();
             while (sdkeys.hasMoreElements()){
             Object tk = sdkeys.nextElement();
             //LogService.instance().log(LogService.DEBUG, "CGroupsManager.setRuntimeData() : removing sd element " +tk);
             slaveSD.remove(tk);
             }
             IPermissible[] owners = new IPermissible[1];
             owners[0] = (IPermissible) Class.forName(OWNER).newInstance();
             String[] tgts = new String[1];
             tgts[0] = (runtimeData.getParameter("grpViewKey"));
             runtimeData.remove("grpViewKey");
             HashMap targets = new HashMap(1);
             targets.put(getOwnerToken(), tgts);
             HashMap acts = new HashMap(1);
             acts.put(getOwnerToken(), this.getActivityTokens());
             slaveSD.put("prmOwners", owners);
             slaveSD.put("prmActivities", acts);
             slaveSD.put("prmTargets", targets);
             slaveSD.put("prmView", "Assign By Owner");
             ((IChannel)servantChannel).setStaticData(slaveSD);
             */

         } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, e);
         }
      }
      if (servantChannel != null) {
         try {
            LogService.instance().log(LogService.DEBUG, this.getClass().getName() + ".setRuntimeData(): Setting Servant runtimedata");
            ((IChannel)servantChannel).setRuntimeData(slaveRD);
            if (servantChannel.isFinished()) {
               servantChannel = null;
               // flushing runtimedata for case where GroupsManager is master and servant, to prevent dirtiness
               Enumeration rd2 = runtimeData.keys();
               while (rd2.hasMoreElements()) {
                  runtimeData.remove(rd2.nextElement());
               }
               //runtimeData.setParameter("view",getFromView(staticData.getParameter("view")));
               //staticData.remove("view");
               LogService.instance().log(LogService.DEBUG, this.getClass().getName() +
                     ".setRuntimeData(): removed servant");
            }
            else {
               LogService.instance().log(LogService.DEBUG, this.getClass().getName() +
                     ".setRuntimeData(): servant Not Finished");
            }
         } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, this.getClass().getName() + ".setRuntimeDat(): Problem setting servant runtimedata "
                  + e);
         }
      }
      //end servant code
      if (servantChannel == null) {
         if (!hasValue(runtimeData.getParameter("grpView")) || hasValue(runtimeData.getParameter("grpView"),
               "AssignPermissions")) {
            if (hasValue(staticData.get("grpView"))) {
               LogService.instance().log(LogService.DEBUG, this.getClass().getName() +
                     ".setRuntimeData() : using cached grpView " + staticData.getParameter("grpView"));
               runtimeData.setParameter("grpView", staticData.getParameter("grpView"));
            }
            else {
               runtimeData.setParameter("grpView", "tree");
            }
         }
         this.runtimeData.setParameter("username", "guest");
         if (user != null) {
            this.runtimeData.setParameter("username", String.valueOf(user.getID()));
         }
         if (!hasValue(runtimeData.getParameter("grpMode"))) {
            if (hasValue(staticData.get("grpMode"))) {
               LogService.instance().log(LogService.DEBUG, this.getClass().getName() +
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
               c.execute(runtimeData, this.staticData);
               //Utility.printDoc(viewDoc, "CGroupsManager::renderXML(): XML Document AFTER COMMAND: " + "\n");
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
            DocumentImpl viewDoc = getViewDoc();
            String grpKey = runtimeData.getParameter("grpViewKey");
            Element grpViewKeyElem;
            Iterator grpItr = Utility.getNodesByTagNameAndKey(viewDoc, GROUP_TAGNAME,
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
         if (!hasValue(runtimeData.getParameter("grpViewId"))) {
            if (hasValue(staticData.get("grpViewId"))) {
               LogService.instance().log(LogService.DEBUG, this.getClass().getName() +
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
          LogService.instance().log(LogService.DEBUG, this.getClass().getName()
          + ".setRuntimeData() RuntimeData paramaters: " + n +
          " = " + runtimeData.getParameter(n));
          }
          Enumeration names2 = staticData.keys();
          while (names2.hasMoreElements()) {
          String n2 = (String)names2.nextElement();
          LogService.instance().log(LogService.DEBUG, this.getClass().getName()
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
    */
   public void setStaticData (ChannelStaticData sd) {
      this.staticData = sd;
      user = sd.getPerson();
      Utility.logMessage("DEBUG", this.getClass().getName() + "::setStaticData(): staticData Person ID = "
            + user.getID());
      Iterator i = this.staticData.entrySet().iterator();
      while (i.hasNext()) {
         Map.Entry m = (Map.Entry)i.next();
         Utility.logMessage("DEBUG", this.getClass().getName() + "::setStaticData(): staticData "
               + m.getKey() + " = " + m.getValue());
      }
   }

   /**
    * put your documentation comment here
    * @return
    */
   public String getOwnerName () {
      return  "Groups Manager";
   }

   /**
    * put your documentation comment here
    * @return
    */
   public String[] getActivityTokens () {
      init();
      return  (String[])activities.keySet().toArray(new String[0]);
   }

   /**
    * put your documentation comment here
    * @return
    */
   public String getOwnerToken () {
      return  OWNER;
   }

   /**
    * put your documentation comment here
    * @param token
    * @return
    */
   public String getActivityName (String token) {
      return  (String)activities.get(token);
   }

   /**
    * put your documentation comment here
    * @return
    */
   public String[] getTargetTokens () {
      init();
      return  (String[])targets.keySet().toArray(new String[0]);
   }

   /**
    * put your documentation comment here
    * @param token
    * @return
    */
   public String getTargetName (String token) {
      return  (String)targets.get(token);
   }

   /**
    * put your documentation comment here
    * @param o
    * @return
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
    * @return
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
    * @return
    */
   public ChannelCacheKey generateKey () {
      ChannelCacheKey cck;
      if (servantChannel == null) {
         cck = new ChannelCacheKey();
         cck.setKey(staticData.getChannelPublishId()+"-"+staticData.getChannelSubscribeId() + "-" + String.valueOf(staticData.getPerson().getID()));
         cck.setKeyValidity(vKey());
         LogService.instance().log(LogService.DEBUG, this.getClass().getName() + ".generateKey() : set validity to "
               + vKey());
      }
      else {
         cck = ((ICacheable)servantChannel).generateKey();
      }
      return  cck;
   }

   /**
    * put your documentation comment here
    * @return
    */
   private String vKey () {
      String vkey = runtimeData.getParameter("grpView") + " - " + runtimeData.getParameter("grpViewId")
            + " - " + runtimeData.getParameter("grpMode");
      return  vkey;
   }

   /**
    * put your documentation comment here
    * @param validity
    * @return
    */
   public boolean isCacheValid (Object validity) {
      boolean valid = false;
      if (servantChannel == null) {
         if (validity != null) {
            if (validity.equals(vKey()) && runtimeData.get("grpCommand") == null) {
               valid = true;
            }
         }
         long time3 = Calendar.getInstance().getTime().getTime();
         LogService.instance().log(LogService.DEBUG, this.getClass().getName() + ".isCacheValid() time since setRD: "
               + String.valueOf((time3 - startRD)) + ", valid=" + valid);
      }
      else {
         valid = ((ICacheable)servantChannel).isCacheValid(validity);
      }
      return  valid;
   }
}



