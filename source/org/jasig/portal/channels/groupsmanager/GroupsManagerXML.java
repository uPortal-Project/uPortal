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
import  org.jasig.portal.groups.*;
import  org.jasig.portal.services.*;
import  org.jasig.portal.ChannelRuntimeData;
import  org.jasig.portal.services.GroupService;
import  org.jasig.portal.security.*;
import  org.jasig.portal.ChannelStaticData;
import  java.sql.Timestamp;
import  org.apache.xml.serialize.XMLSerializer;
import  org.apache.xml.serialize.OutputFormat;
import  org.w3c.dom.Node;
import  org.w3c.dom.NodeList;
import  org.w3c.dom.Element;
import  org.w3c.dom.Text;
import  org.apache.xerces.parsers.DOMParser;
import  org.apache.xerces.parsers.SAXParser;
import  org.apache.xerces.dom.DocumentImpl;


/**
 * @todo
 * xmlCache: should eventually check an element cache and clone the cached
 * element OR if cached element does not exist created one and use it
 *
 * refactor: reexamine common functions in GroupsManagerXML, Utility,
 * GroupsManagerCommand, and wrapper classes to come up consistent approach
 *
 * IInitialGroupsContextStore will be used to make the stores swappable through an
 * entry in portal.properties. At this point the RDBMInitialGroupContextStore is
 * hardcoded in InitialGroupsContextImpl.getFactory().
 */

 /**
 * Contains a groups of static methods used to centralize the generation and
 * retrieval of xml elements for groups and entities.
 */
public class GroupsManagerXML
      implements GroupsManagerConstants {
   private static int UID = 0;

   /**
    * Returns a DocumentImpl for all InitialContexts for which the user has
    * permissions. This method is called when CGroupsManager is instantiated.
    * @param rd
    * @return
    */
   public static DocumentImpl getGroupsManagerXml (ChannelRuntimeData rd, ChannelStaticData sd) {
      //getGroupsXML(ChannelRuntimeData rd)
      String rkey = null;
      IEntityGroup entGrp = null;
      IGroupMember aGroupMember = null;
      Element igcElement;
      DocumentImpl viewDoc = new DocumentImpl();
      Element viewRoot = viewDoc.createElement("CGroupsManager");
      viewDoc.appendChild(viewRoot);
      Element apRoot = getAuthorizationXml(viewDoc, sd);
      viewRoot.appendChild(apRoot);
      Element igcRoot = GroupsManagerXML.createElement(GROUP_TAGNAME, viewDoc, true);
      igcRoot.setAttribute("expanded", "true");
      Element rdfElem = createRdfElement(ROOT_GROUP_TITLE, ROOT_GROUP_DESCRIPTION,
            "0", viewDoc);
      igcRoot.appendChild(rdfElem);
      //* Cut this section into a new method to create group xml without a groupmember object
      viewRoot.appendChild(igcRoot);
      try {
         RDBMInitialGroupContextStore igcHome = RDBMInitialGroupContextStore.singleton();
         // have to get userID from runtimedata
         Utility.logMessage("DEBUG", "GroupsManagerXML::getGroupsManagerXML(): JUST BEFORE USER IS SET ");
         String userID = rd.getParameter("username");
         Utility.logMessage("DEBUG", "GroupsManagerXML::getGroupsManagerXML(): USER SET TO "
               + userID);
         java.util.Iterator igcItr = igcHome.findInitialGroupContextsForOwner(AuthorizationService.instance().getGroupMember(sd.getAuthorizationPrincipal()));
         IInitialGroupContext igc = null;
         Utility.logMessage("DEBUG", "GroupsManagerXML::getGroupsManagerXML(): igc contains: ");
         // Now that we know if there are any initial group contexts, we can set the attributes.
         if (igcItr.hasNext()) {
            Utility.logMessage("DEBUG", "GroupsManagerXML::getGroupsManagerXML(): Initial group contexts found for this user:");
         }
         else {
            igcRoot.setAttribute("expanded", "false");
            igcRoot.setAttribute("hasMembers", "false");
            Utility.logMessage("DEBUG", "GroupsManagerXML::getGroupsManagerXML(): No initial group contexts for this user.");
         }
         while (igcItr.hasNext()) {
            igc = (IInitialGroupContext)igcItr.next();
            Utility.logMessage("DEBUG", "GroupsManagerXML::getGroupsManagerXML(): igc = /n"
                  + igc);
            rkey = igc.getGroupID();
            entGrp = retrieveGroup(rkey);
            //aGroupMember = (IGroupMember) entGrp;
            igcElement = getGroupMemberXml(entGrp, igc.isExpanded(), null, viewDoc);
            igcElement.setAttribute("ownerType", igc.getOwnerType());
            igcRoot.appendChild(igcElement);
         }
      } catch (Exception e) {
         Utility.logMessage("ERROR", "GroupsManagerXML::getGroupsManagerXML(): ERROR"
               + e.toString());
      }
      return  viewDoc;
   }

   /**
    * Returns an Element for an IGroupMember. If an element is passed in,
    * it is acted upon (eg. expand the group), otherwise a new one is created.
    * @param gm
    * @param isContextExpanded
    * @param anElem
    * @param aDoc
    * @return
    */
   public static Element getGroupMemberXml (IGroupMember gm, boolean isContextExpanded,
         Element anElem, DocumentImpl aDoc) {
      Element rootElem = null;
      GroupsManagerWrapperFactory wf = GroupsManagerWrapperFactory.instance();
      if (gm.isEntity()) {
         //Element rootElem = (anElem != null ? anElem : GroupsManagerXML.createElement(ENTITY_TAGNAME, aDoc)) ;
         Utility.logMessage("DEBUG", "GroupsManagerXML::getGroupMemberXml(): About to get entity wrapper");
         IGroupsManagerWrapper rap = (IGroupsManagerWrapper)wf.get(ENTITY_TAGNAME);
         Utility.logMessage("DEBUG", "GroupsManagerXML::getGroupMemberXml(): Got entity wrapper");
         if (rap != null) {
            //Utility.logMessage("DEBUG", "GroupsManagerXML::retrieveEntityXml: setup parms and about to get entity xml");
            rootElem = rap.getXml(gm, anElem, aDoc);
         }
      }
      else {
         Utility.logMessage("DEBUG", "GroupsManagerXML::getGroupMemberXml(): element parm is null = "
               + (anElem == null));
         rootElem = (anElem != null ? anElem : GroupsManagerXML.createElement(GROUP_TAGNAME,
               aDoc, false));
         rootElem.setAttribute("expanded", String.valueOf(isContextExpanded));
         Utility.logMessage("DEBUG", "GroupsManagerXML::getGroupMemberXml(): About to get group wrapper");
         IGroupsManagerWrapper rap = (IGroupsManagerWrapper)wf.get(GROUP_TAGNAME);
         Utility.logMessage("DEBUG", "GroupsManagerXML::getGroupMemberXml(): Got group wrapper");
         if (rap != null) {
            //Utility.logMessage("DEBUG", "GroupsManagerXML::getGroupMemberXml(): setup parms and about to get entity xml");
            rootElem = rap.getXml(gm, rootElem, aDoc);
         }
      }
      return  rootElem;
   }

   /**
    * Returns an IEntity for the key.
    * @param aKey
    * @return
    */
   public static IEntity retrieveEntity (String aKey, String aType) {
      IEntity ent = null;
      try {
         Class iEntityClass = Class.forName(aType);
         ent = GroupService.getEntity(aKey, iEntityClass);
      } catch (Exception e) {
         Utility.logMessage("ERROR", "EntityWrapper.retrieveEntity(): ERROR retrieving entity "
               + e.toString());
      }
      return  ent;
   }

   /**
    * Returns an IGroupMember name, for a key and class
    * @param aKey
    * @param typClassName
    * @return
    */
   public static String getGroupMemberName (String aKey, String typClassName) {
      String gmName = "";
      try {
         gmName = EntityNameFinderService.instance().getNameFinder(Class.forName(typClassName)).getName(aKey);
      } catch (Exception e) {
         Utility.logMessage("ERROR", "GroupsManagerXML.getGroupMemberName(): ERROR retrieving entity "
               + e.toString());
      }
      return  gmName;
   }

   /**
    * Returns an IEntityGroup for the key.
    * @param aKey
    * @return
    */
   public static IEntityGroup retrieveGroup (String aKey) {
      Utility.logMessage("DEBUG", "GroupsManagerXML::retrieveGroup(): About to search for Group: "
            + aKey);
      //      EntityGroupStoreRDBM groupHome = null;
      //      IGroupMember printGroupMember = null;
      IEntityGroup grp = null;
      try {
         grp = GroupService.findGroup(aKey);
         //printGroupMember = (IGroupMember) grp;
         //Utility.logMessage("DEBUG", "GroupsManagerXML::retrieveGroup(): Inner Print =>\n  " + printGroupMember);
      } catch (Throwable th) {
         Utility.logMessage("ERROR", "GroupsManagerXML::retrieveGroup(): Could not retrieve Group Member ("
               + aKey + "): \n" + th);
      }
      return  grp;
   }

   /**
    * Returns the next sequential identifier which is used to uniquely
    * identify an element. This identifier is held in the Element "id" attribute.
    * "0" is reserved for the Group containing the Initial Contexts for the user.
    * @return
    */
   public static synchronized String getNextUid () {
      // max size of int = (2 to the 32 minus 1) = 2147483647
      Utility.logMessage("DEBUG", "GroupsManagerXML::getNextUid(): Start");
      if (UID > 2147483600) {
         // the value 0 is reserved for the group holding the initial group contexts
         UID = 1;
      }
      return  String.valueOf(++UID);
   }

   /**
    * Creates an element for the provided DocumentImpl. Alternatively, can
    * set default values.
    * @param name
    * @param xmlDoc
    * @param setGrpDefault
    * @return
    */
   public static Element createElement (String name, DocumentImpl xmlDoc, boolean setGrpDefault) {
      //* Maybe I should have all parms in a java.util.HashMap
      Element grpRoot = xmlDoc.createElement(name);
      grpRoot.setAttribute("selected", "false");
      // set default values
      if (setGrpDefault) {
         grpRoot.setAttribute("id", "0");
         //grpRoot.setAttribute("key", "");
         grpRoot.setAttribute("expanded", "false");
         // hasMembers is used to determine if the GroupMember has been retrieved
         // (ie. the element has been fully setup)
         //grpRoot.setAttribute("hasMembers", "false");
      }
      // ?? Element rdf = createRdfElement(xmlDoc, title, description, creator);
      // ?? grpRoot.appendChild(rdf);
      //* Cut this section into a new method to create group xml without a groupmember object
      return  grpRoot;
   }

   /**
    * Returns an RDF element for the provided DocumentImpl
    * @param title
    * @param description
    * @param creator
    * @param xmlDoc
    * @return
    */
   public static Element createRdfElement (String title, String description, String creator,
         DocumentImpl xmlDoc) {
      //* Maybe I should have all parms in a java.util.HashMap
      Element rdfElem = (Element)xmlDoc.createElement("rdf:RDF");
      rdfElem.setAttribute("xmlns:dc", "http://purl.org/dc/elements/1.1/");
      rdfElem.setAttribute("xmlns:rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
      Utility.logMessage("DEBUG", "GroupsManagerXML::createRdfElement(): CREATING ELEMENT RDF DESCRIPTION");
      Element rdfDesc = (Element)xmlDoc.createElement("rdf:Description");
      Utility.logMessage("DEBUG", "GroupsManagerXML::createRdfElement(): CREATING ELEMENT DCTITLE");
      Element dcTitle = (Element)xmlDoc.createElement("dc:title");
      dcTitle.appendChild(xmlDoc.createTextNode(title));
      rdfDesc.appendChild(dcTitle);
      Utility.logMessage("DEBUG", "GroupsManagerXML::createRdfElement(): CREATING ELEMENT dcDESCRIPTION");
      Element dcDescription = (Element)xmlDoc.createElement("dc:description");
      dcDescription.appendChild(xmlDoc.createTextNode(description));
      rdfDesc.appendChild(dcDescription);
      Utility.logMessage("DEBUG", "GroupsManagerXML::createRdfElement(): CREATING ELEMENT dcCREATOR");
      Element dcCreator = (Element)xmlDoc.createElement("dc:creator");
      Utility.logMessage("DEBUG", "GroupsManagerXML::createRdfElement(): APPENDING TO dcCREATOR");
      dcCreator.appendChild(xmlDoc.createTextNode(creator));
      Utility.logMessage("DEBUG", "GroupsManagerXML::createRdfElement(): APPENDING TO RDFDESC");
      rdfDesc.appendChild(dcCreator);
      Utility.logMessage("DEBUG", "GroupsManagerXML::createRdfElement(): APPENDING TO RDF");
      rdfElem.appendChild(rdfDesc);
      return  rdfElem;
   }

   /**
    * Returns an element holding the user's permissions used to determine access
    * privileges in the Groups Manager channel.
    * @param title
    * @param description
    * @param creator
    * @param xmlDoc
    * @return
    */
   public static Element getAuthorizationXml (DocumentImpl xmlDoc, ChannelStaticData sd) {
      IAuthorizationPrincipal ap = sd.getAuthorizationPrincipal();
      Element apRoot = null;
      if (ap != null) {
         apRoot = xmlDoc.createElement("principal");
         apRoot.setAttribute("token", ap.getPrincipalString());
         apRoot.setAttribute("type", ap.getType().getName());
         String name = ap.getKey();
         try {
            name = EntityNameFinderService.instance().getNameFinder(ap.getType()).getName(name);
         } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, e);
         }
         apRoot.setAttribute("name", name);
      }
      try {
         // owner, activity, target
         IPermission[] perms = ap.getAllPermissions(OWNER, null, null);
         for (int yy = 0; yy < perms.length; yy++) {
            Element prm = xmlDoc.createElement("permission");
            prm.setAttribute("principal", perms[yy].getPrincipal());
            prm.setAttribute("activity", perms[yy].getActivity());
            prm.setAttribute("type", perms[yy].getType());
            prm.setAttribute("target", perms[yy].getTarget());
            apRoot.appendChild(prm);
         }
      } catch (org.jasig.portal.AuthorizationException ae) {
         Utility.logMessage("ERROR", "GroupsManagerXML::getAuthorzationXml: authorization exception "
               + ae.getMessage());
      }
      return  apRoot;
   }
}
