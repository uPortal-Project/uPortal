/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.channels.permissionsmanager;

import java.util.ArrayList;

import org.jasig.portal.IPermissible;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.services.EntityNameFinderService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * The class responsible for generating the monolithic XML file for CPermissionsManager
 *
 * @author Alex Vigdor
 * @version $Revision$
 */
public class PermissionsXML {
    private static final Log log = LogFactory.getLog(PermissionsXML.class);
    private static final String findPermissibles = "SELECT IPERMISSIBLE_CLASS FROM UPC_PERM_MGR";

    /** Creates new PermissionsXML */
    protected PermissionsXML () {
    }

    /**
     * @param session
     * @return a <code>Document</code>
     */
    public static Document getViewDoc (PermissionsSessionData session){
        return getViewDoc(session, false);
    }

    public static Document getViewDoc (PermissionsSessionData session, boolean forceRefresh){
        synchronized (session) {
            if (session.XML == null || forceRefresh) {
                Document rDoc = org.jasig.portal.utils.DocumentFactory.getNewDocument();
                session.XML = rDoc;
                session.owners = new IPermissible[0];
                IAuthorizationPrincipal[] principals;
                try {
                    Element root = rDoc.createElement("CPermissionsManager");
                    rDoc.appendChild(root);
                    
                    if (session.staticData.get("prmOwners") != null) {
                        // use specified set of owners
                        log.debug("PermissionsXML.getViewDoc(): using specified owners");
                        session.owners = (IPermissible[])session.staticData.get("prmOwners");
                    }
                    else {
                        // use owners found in DB
                        log.debug("PermissionsXML.getViewDoc(): using DB owners");
                        session.owners = RDBMPermissibleRegistry.getAllPermissible();
                    }

                    for (int i = 0; i < session.owners.length; i++) {
                        log.debug("PermissionsXML.getViewDoc(): Configuring element for owner "+session.owners[i].getOwnerName());
                        Element owner = rDoc.createElement("owner");
                        owner.setAttribute("name", session.owners[i].getOwnerName());
                        owner.setAttribute("token", session.owners[i].getOwnerToken());
                        owner.setAttribute("ipermissible", session.owners[i].getClass().getName());

                        String[] activities = session.owners[i].getActivityTokens();
                        for (int j = 0; j < activities.length; j++) {
                            Element act = rDoc.createElement("activity");
                            act.setAttribute("token", activities[j]);
                            act.setAttribute("name", session.owners[i].getActivityName(activities[j]));
                            owner.appendChild(act);
                        }

                        String[] targets = session.owners[i].getTargetTokens();
                        for (int k = 0; k < targets.length; k++) {
                            addTargetToOwnerElement(session.owners[i],owner,targets[k]);
                        }
                        root.appendChild(owner);
                    }

                } catch (Exception e) {
                    log.error("Error getting the view doc for session " + session, e);
                }
            }
        }
        return  session.XML;
    }
    
    public static Element addTargetToOwnerElement(IPermissible owner, Element ownerEl, String target){
       Element tgt = ownerEl.getOwnerDocument().createElement("target");
        tgt.setAttribute("token", target);
        tgt.setAttribute("name", owner.getTargetName(target));
        ownerEl.appendChild(tgt);
        return tgt;
    }
    
    public static void setSelected(PermissionsSessionData session, String ipermissible, String type, String token, boolean selected){
        log.debug("PermissionsXML.setSelected(): processing "+ipermissible+" / "+type+" / "+token+" / "+selected);
        Element owner = getOwner(session,ipermissible);
        Element s = owner;
        String otoken = owner.getAttribute("token");
        if (!type.equals("owner")){
          NodeList nl = owner.getElementsByTagName(type);
          boolean found = false;
          for(int i=0; i<nl.getLength(); i++){
            Element x = (Element) nl.item(i);
            if (x.getAttribute("token").equals(token)){
              s= x;
              found=true;
              break;
            }
          }
          if (!found){
            if (type.equals("target")){
              for (int i=0; i<session.owners.length; i++){
                if (session.owners[i].getOwnerToken().equals(otoken)){
                  s = addTargetToOwnerElement(session.owners[i],owner,token);
                  break; 
                }
              }
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
          //log.debug("PermissionsManager:: getting principals");
          IAuthorizationPrincipal[] aps = session.principals;
          for (int m = 0; m < aps.length; m++) {
              try {
              //log.debug("PermissionsManager:: iterating over principals");
              Element ppl = rDoc.createElement("principal");
              ppl.setAttribute("token", aps[m].getPrincipalString());
              ppl.setAttribute("type", aps[m].getType().getName());
              String name = aps[m].getKey();
              try {
                  name = EntityNameFinderService.instance().getNameFinder(aps[m].getType()).getName(name);
              } catch (Exception e) {
                  log.error(e, e);
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
                log.error(e, e);
              }
          }
      }
    }

    /**
     * put your documentation comment here
     * @param session
     * @param tagname
     */
    public static void autoSelectSingleChoice (PermissionsSessionData session, String tagname){
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

    public static String[] getSelectedTargets(PermissionsSessionData session, Element owner){
      log.debug("PermissionsXML.getSelectedTargets(): processing for "+owner.getAttribute("name"));
      ArrayList targets = new ArrayList();
      Element o =owner;
      if (o != null){
        NodeList tl = o.getElementsByTagName("target");
         for (int i=0;i<tl.getLength();i++){
          Element target = (Element)tl.item(i);
          if ((target.getAttribute("selected") != null) && (target.getAttribute("selected").equals("true"))){
            targets.add(target.getAttribute("token"));
            log.debug("PermissionsXML.getSelectedTargets(): adding "+target.getAttribute("token"));
          }
         }
      }
      return (String[])targets.toArray(new String[0]);
    }

    public static String[] getSelectedActivities(PermissionsSessionData session, Element owner){
      log.debug("PermissionsXML.getSelectedActivities(): processing for "+owner.getAttribute("name"));
      ArrayList activities = new ArrayList();
      Element o = owner;
      if (o != null){
        NodeList al = o.getElementsByTagName("activity");
         for (int i=0;i<al.getLength();i++){
          Element activity = (Element)al.item(i);
          if ((activity.getAttribute("selected") != null) && (activity.getAttribute("selected").equals("true"))){
            activities.add(activity.getAttribute("token"));
            log.debug("PermissionsXML.getSelectedActivities(): adding "+activity.getAttribute("token"));
          }
         }
      }
      return (String[])activities.toArray(new String[0]);
    }

    public static Element[] getSelectedOwners(PermissionsSessionData session){
        ArrayList owners = new ArrayList();
        Document doc = getViewDoc(session);
        NodeList ol = doc.getElementsByTagName("owner");
        for (int i=0;i<ol.getLength();i++){
          Element owner = (Element)ol.item(i);
          if ((owner.getAttribute("selected") != null) && (owner.getAttribute("selected").equals("true"))){
            owners.add(owner);
          }
        }
        return (Element[])owners.toArray(new Element[0]);
    }

    public static Element getOwner(PermissionsSessionData session, String ipermissible){
       log.debug("PermissionsXML.getOwner(): looking for owner of class "+ipermissible);
       Document doc = getViewDoc(session);
       Element ro = null;
       NodeList ol = doc.getElementsByTagName("owner");
        for (int i=0;i<ol.getLength();i++){
          Element o = (Element)ol.item(i);
          if(o.getAttribute("ipermissible").equals(ipermissible)){
            log.debug("PermissionsXML.getOwner(): found owner of class "+ipermissible+" and token "+o.getAttribute("token"));
            ro = o;
            break;
          }
        }
        return ro;
    }
}



