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
 *
 */


package  org.jasig.portal.channels.permissionsmanager;

import  org.jasig.portal.*;
import  org.jasig.portal.security.*;
import  org.jasig.portal.security.provider.*;
import  org.jasig.portal.services.*;
import  org.jasig.portal.groups.*;
import  org.w3c.dom.Document;
import  org.w3c.dom.Node;
import  org.w3c.dom.NodeList;
import  org.w3c.dom.Element;
import  org.w3c.dom.Text;
import  java.util.*;
import  java.sql.*;
import  org.apache.xml.serialize.XMLSerializer;
import  org.apache.xml.serialize.OutputFormat;
import  java.io.StringReader;
import  java.io.StringWriter;
import  org.apache.xerces.parsers.DOMParser;
import  org.apache.xerces.parsers.SAXParser;
import  org.apache.xerces.dom.DocumentImpl;


/**
 * The class responsible for generating the monolithic XML file for CPermissionsManager
 *
 * @author Alex Vigdor
 * @version $Revision$
 */
public class PermissionsXML {
    private static final String findPermissibles = "SELECT IPERMISSIBLE_CLASS FROM UPC_PERM_MGR";

    /** Creates new PermissionsXML */
    protected PermissionsXML () {
    }

    /**
     * put your documentation comment here
     * @param session
     * @return
     */
    public static Document getViewDoc (PermissionsSessionData session) {
        return  getViewDoc(session, false);
    }

    /**
     * put your documentation comment here
     * @param rd
     * @param sd
     * @return
     */
    public static Document getViewDoc (PermissionsSessionData session, boolean forceRefresh) {
        synchronized (session) {
            if (session.XML == null || forceRefresh) {
                DocumentImpl rDoc = new DocumentImpl();
                session.XML = rDoc;
                IPermissible[] owners = new IPermissible[0];
                IAuthorizationPrincipal[] principals;
                try {
                    Element root = rDoc.createElement("CPermissionsManager");
                    rDoc.appendChild(root);

                    // use owners found in DB
                    owners = RDBMPermissibleRegistry.getAllPermissible();
                    for (int i = 0; i < owners.length; i++) {
                        Element owner = rDoc.createElement("owner");
                        owner.setAttribute("name", owners[i].getOwnerName());
                        owner.setAttribute("token", owners[i].getOwnerToken());
                        owner.setAttribute("ipermissible", owners[i].getClass().getName());

                        String[] activities = owners[i].getActivityTokens();
                        for (int j = 0; j < activities.length; j++) {
                            Element act = rDoc.createElement("activity");
                            act.setAttribute("token", activities[j]);
                            act.setAttribute("name", owners[i].getActivityName(activities[j]));
                            owner.appendChild(act);
                        }

                        String[] targets = owners[i].getTargetTokens();
                        for (int k = 0; k < targets.length; k++) {
                            Element tgt = rDoc.createElement("target");
                            tgt.setAttribute("token", targets[k]);
                            tgt.setAttribute("name", owners[i].getTargetName(targets[k]));
                            owner.appendChild(tgt);
                        }
                        root.appendChild(owner);
                    }

                    //LogService.instance().log(LogService.DEBUG,"PermissionsManager:: checking for principals");
                   // if (session.principals != null) {
                    //  populatePrincipals(session);
                    //}
                    /*
                     StringWriter sw = new java.io.StringWriter();
                     XMLSerializer serial = new XMLSerializer(sw, new OutputFormat(rDoc,"UTF-8",true));
                     serial.serialize(rDoc);
                     LogService.instance().log(LogService.DEBUG,"Permissions viewXMl ready:\n"+sw.toString());
                     */

                } catch (Exception e) {
                    LogService.instance().log(LogService.ERROR, e);
                }
            }
        }
        return  session.XML;
    }
    public static void setSelected(PermissionsSessionData session, String ipermissible, String type, String token, boolean selected){
        LogService.instance().log(LogService.DEBUG,"PermissionsXML.setSelected(): processing "+ipermissible+" / "+type+" / "+token+" / "+selected);
        Element owner = getOwner(session,ipermissible);
        Element s = owner;
        if (!type.equals("owner")){
          NodeList nl = owner.getElementsByTagName(type);
          for(int i=0; i<nl.getLength(); i++){
            Element x = (Element) nl.item(i);
            if (x.getAttribute("token").equals(token)){
              s= x;
              break;
            }
          }
        }
       String value = "false";
       if (selected){
          value="true";
       }
       s.setAttribute("selected",value);
    }

    public static void populatePrincipals(PermissionsSessionData session){
        if (session.principals != null) {
          Document rDoc;
          if (session.XML == null){
            rDoc = getViewDoc(session);
          }
          else {
            rDoc = session.XML;
          }
          Element root = rDoc.getDocumentElement();
          IPermissible[] owners;
          if (session.staticData.get("prmOwners") != null) {
              // use specified set of owners
              owners = (IPermissible[])session.staticData.get("prmOwners");
          }
          else {
              // use owners found in DB
              owners = RDBMPermissibleRegistry.getAllPermissible();
          }

          ArrayList ownerKeys = new ArrayList();
          for (int x = 0; x < owners.length; x++) {
              ownerKeys.add(owners[x].getOwnerToken());
          }
          //LogService.instance().log(LogService.DEBUG,"PermissionsManager:: getting principals");
          IAuthorizationPrincipal[] aps = session.principals;
          for (int m = 0; m < aps.length; m++) {
              try {
              //LogService.instance().log(LogService.DEBUG,"PermissionsManager:: iterating over principals");
              Element ppl = rDoc.createElement("principal");
              ppl.setAttribute("token", aps[m].getPrincipalString());
              ppl.setAttribute("type", aps[m].getType().getName());
              String name = aps[m].getKey();
              try {
                  name = EntityNameFinderService.instance().getNameFinder(aps[m].getType()).getName(name);
              } catch (Exception e) {
                  LogService.instance().log(LogService.ERROR, e);
              }
              ppl.setAttribute("name", name);
              IPermission[] pms = aps[m].getAllPermissions();
              for (int n = 0; n < pms.length; n++) {
                  if (ownerKeys.contains(pms[n].getOwner())) {
                      Element perm = rDoc.createElement("permission");
                      perm.setAttribute("owner", pms[n].getOwner());
                      perm.setAttribute("principal", pms[n].getPrincipal());
                      perm.setAttribute("activity", pms[n].getActivity());
                      perm.setAttribute("type", pms[n].getType());
                      perm.setAttribute("target", pms[n].getTarget());
                      if (pms[n].getEffective() != null) {
                          perm.setAttribute("effective", pms[n].getEffective().toString());
                      }
                      if (pms[n].getExpires() != null) {
                          perm.setAttribute("expires", pms[n].getExpires().toString());
                      }
                      ppl.appendChild(perm);
                  }
              }
              root.appendChild(ppl);
              }
              catch(Exception e){
                LogService.instance().log(LogService.ERROR,e);
              }
          }
      }
    }

    /**
     * put your documentation comment here
     * @param session
     * @param tagname
     */
    public static void autoSelectSingleChoice (PermissionsSessionData session, String tagname) {
        boolean allsingle = true;
        Element docRoot = PermissionsXML.getViewDoc(session).getDocumentElement();
        NodeList nl = docRoot.getElementsByTagName("owner");
        for (int l = 0; l < nl.getLength(); l++) {
            Element o = (Element)nl.item(l);
            if (o.getAttribute("selected") != null && o.getAttribute("selected").equals("true")) {
                int count = 0;
                NodeList cl = o.getElementsByTagName(tagname);
                if (cl.getLength() == 1) {
                    ((Element)cl.item(0)).setAttribute("selected", "true");
                }
                else {
                    allsingle = false;
                }
            }
        }
        if (allsingle) {
            if (tagname.equals("activity")) {
                session.gotActivities = true;
            }
            if (tagname.equals("target")) {
                session.gotTargets = true;
            }
        }
    }

    public static String[] getSelectedTargets(PermissionsSessionData session, String owner) {
      LogService.instance().log(LogService.DEBUG,"PermissionsXML.getSelectedTargets(): processing for "+owner);
      ArrayList targets = new ArrayList();
      Element o = getOwner(session,owner);
      if (o != null){
        NodeList tl = o.getElementsByTagName("target");
         for (int i=0;i<tl.getLength();i++){
          Element target = (Element)tl.item(i);
          if ((target.getAttribute("selected") != null) && (target.getAttribute("selected").equals("true"))){
            targets.add(target.getAttribute("token"));
            LogService.instance().log(LogService.DEBUG,"PermissionsXML.getSelectedTargets(): adding "+target.getAttribute("token"));
          }
         }
      }
      return (String[])targets.toArray(new String[0]);
    }

    public static String[] getSelectedActivities(PermissionsSessionData session, String owner) {
      LogService.instance().log(LogService.DEBUG,"PermissionsXML.getSelectedActivities(): processing for "+owner);
      ArrayList activities = new ArrayList();
      Element o = getOwner(session,owner);
      if (o != null){
        NodeList al = o.getElementsByTagName("activity");
         for (int i=0;i<al.getLength();i++){
          Element activity = (Element)al.item(i);
          if ((activity.getAttribute("selected") != null) && (activity.getAttribute("selected").equals("true"))){
            activities.add(activity.getAttribute("token"));
            LogService.instance().log(LogService.DEBUG,"PermissionsXML.getSelectedActivities(): adding "+activity.getAttribute("token"));
          }
         }
      }
      return (String[])activities.toArray(new String[0]);
    }

    public static String[] getSelectedOwners(PermissionsSessionData session){
        ArrayList owners = new ArrayList();
        Document doc = getViewDoc(session);
        NodeList ol = doc.getElementsByTagName("owner");
        for (int i=0;i<ol.getLength();i++){
          Element owner = (Element)ol.item(i);
          if ((owner.getAttribute("selected") != null) && (owner.getAttribute("selected").equals("true"))){
            owners.add(owner.getAttribute("ipermissible"));
          }
        }
        return (String[])owners.toArray(new String[0]);
    }

    public static Element getOwner(PermissionsSessionData session, String owner){
       Document doc = getViewDoc(session);
       Element ro = null;
       NodeList ol = doc.getElementsByTagName("owner");
        for (int i=0;i<ol.getLength();i++){
          Element o = (Element)ol.item(i);
          if(o.getAttribute("ipermissible").equals(owner)){
            ro = o;
            break;
          }
        }
        return ro;
    }
}



