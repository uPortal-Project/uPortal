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
import  org.apache.log4j.Priority;
import  org.jasig.portal.services.LogService;
import  org.jasig.portal.groups.IGroupMember;
import  java.lang.*;
import  java.io.*;
import  java.util.*;
import  java.sql.Timestamp;
import  org.apache.xerces.dom.DocumentImpl;
import  org.apache.xml.serialize.XMLSerializer;
import  org.w3c.dom.Element;
import  org.w3c.dom.Node;


/**
 * A class holding utility functions used by the Groups Manager channel.
 */
public class Utility
      implements GroupsManagerConstants {

   /** Creates new Utility */
   public Utility () {
   }

   /**
    * Returns an element from an xml document for a unique id. An error is
    * displayed if more than one element is found.
    * @param aDoc
    * @param id
    * @return Element
    */
   public static Element getElementById (DocumentImpl aDoc, String id) {
      int i;
      Collection elems = new java.util.ArrayList();
      Element elem = null;
      Element retElem = null;
      org.w3c.dom.NodeList nList;
      String tagName = ENTITY_TAGNAME;
      boolean isDone = false;
      while (!isDone) {
         nList = aDoc.getElementsByTagName(tagName);
         for (i = 0; i < nList.getLength(); i++) {
            elem = (Element)nList.item(i);
            if (elem.getAttribute("id").equals(id)) {
               elems.add(elem);
            }
         }
         if (tagName.equals(ENTITY_TAGNAME)) {
            tagName = GROUP_TAGNAME;
         }
         else {
            isDone = true;
         }
         if (elems.size() != 1) {
            if (elems.size() > 1) {
               LogService.log(LogService.ERROR, "Utility::getElementById:  More than one element found for Id: "
                     + id);
            }
         }
         else {
            retElem = (Element)elems.iterator().next();
         }
      }
      return  retElem;
   }

   /**
    * Returns an iterator of Nodes for a DocumentImpl for a tagname and IGroupMember key
    * @param aDoc
    * @param tagname
    * @param key
    * @return Iterator
    */
   public static java.util.Iterator getNodesByTagNameAndKey (DocumentImpl aDoc, String tagname,
         String key) {
      int i;
      Collection nodes = new java.util.ArrayList();
      Element elem = null;
      org.w3c.dom.NodeList nList = aDoc.getElementsByTagName(tagname);
      ;
      for (i = 0; i < nList.getLength(); i++) {
         elem = (Element)nList.item(i);
         if (elem.getAttribute("key").equals(key)) {
            nodes.add(nList.item(i));
         }
      }
      Utility.logMessage("DEBUG", "Utility::getNodesByTagNameAndKey: Number of nodes found for tagname " + tagname + " and Key: "
            + key + " is: " + nodes.size());
      return  nodes.iterator();
   }

   /**
    * Even though we know we will find a single element, we sometimes want
    * it returned in an iterator in order to streamline processing.
    * @param aDoc
    * @param id
    * @return iterator
    */
   public static java.util.Iterator getNodesById (DocumentImpl aDoc, String id) {
      int i;
      Collection nodes = new java.util.ArrayList();
      Element elem = (Element)getElementById(aDoc, id);
      nodes.add(elem);
      return  nodes.iterator();
   }

   /**
    * Returns an iterator of Nodes for an Element for a tagname and IGroupMember key
    * @param anElem
    * @param tagname
    * @param key
    * @return Iterator
    */
   public static java.util.Iterator getNodesByTagNameAndKey (Element anElem, String tagname,
         String key) {
      int i;
      Collection nodes = new java.util.ArrayList();
      Element elem = null;
      org.w3c.dom.NodeList nList = anElem.getElementsByTagName(tagname);
      ;
      for (i = 0; i < nList.getLength(); i++) {
         elem = (Element)nList.item(i);
         if (elem.getAttribute("key").equals(key)) {
            nodes.add(nList.item(i));
         }
      }
      Utility.logMessage("DEBUG", "Utility::getNodesByTagNameAndKey: Number of nodes found for tagname " + tagname + " and Key: "
            + key + " is: " + nodes.size());
      return  nodes.iterator();
   }

   /**
    * Returns an Element from a DocumentImpl for a tagname and element id
    * @param aDoc
    * @param tagname
    * @param id
    * @return Element
    */
   public static Element getElementByTagNameAndId (DocumentImpl aDoc, String tagname,
         String id) {
      int i;
      Element elem = null;
      Element selElem = null;
      org.w3c.dom.NodeList nList = aDoc.getElementsByTagName(tagname);
      for (i = 0; i < nList.getLength(); i++) {
         elem = (Element)nList.item(i);
         if (elem.getAttribute("id").equals(id)) {
            selElem = elem;
            break;
         }
      }
      return  selElem;
   }

   /**
    * Returns an Element with the expanded attribute set to true from a
    * DocumentImpl for a tagname and IGroupMember key. This could be used for
    * cloning elements that have already be expanded thereby avoiding the extra
    * time required to retrieve and create an element. see @todo xmlCache:
    * @param aDoc
    * @param tagname
    * @param key
    * @return Element
    */
   public static Element getExpandedElementForTagNameAndKey (DocumentImpl aDoc, String tagname,
         String key) {
      java.util.Iterator nodeItr = Utility.getNodesByTagNameAndKey(aDoc, tagname, key);
      Element curElem = null;
      Element expElem = null;
      while (nodeItr.hasNext()) {
         curElem = (Element)nodeItr.next();
         if (curElem.getAttribute("expanded").equals("true")) {
            expElem = curElem;
            break;
         }
      }
      return  expElem;
   }

   /**
    * Returns an element for a uniquely named tagname
    * @param aDoc
    * @param tagname
    * @return Element
    */
   public static Element getUniqueElementByTagName (DocumentImpl aDoc, String tagname) {
      // Do I really need this method. I could just use the DocumentImpl.getElementsByTagName().
      // I think the only reason I should keep this method is if I build in error checking for more
      // than one element being returned.
      int i;
      Element elem = null;
      org.w3c.dom.NodeList nList = aDoc.getElementsByTagName(tagname);
      for (i = 0; i < nList.getLength(); i++) {
         elem = (Element)nList.item(i);
      }
      //String rootOwner = Utility.asString((DocumentImpl) ((Element) nList.item(i)).getOwnerDocument());
      //LogService.log(LogService.DEBUG, "getElementByTagnameAndID:  expanded element owner = " + rootOwner);
      return  elem;
   }

   /**
    * An attempt to extract all calls to logger to expedite assumed future
    * upgrades.
    * @param msgTypeStr
    * @param msg
    */
   public static void logMessage (String msgTypeStr, String msg) {
      Priority msgType;
      if (msgTypeStr == null | msgTypeStr.equals(""))
         msgType = LogService.DEBUG;
      else
         msgType = Priority.toPriority("LogService." + msgTypeStr.toUpperCase());
      LogService.log(msgType, msg);
      return;
   }

   /**
    * Prints a DocumentImpl. Used for debugging.
    * @param aDoc
    * @param aMessage
    */
   public static void printDoc (DocumentImpl aDoc, String aMessage) {
      String aMsg = (aMessage != null ? aMessage : "");
      try {
         StringWriter sw = new StringWriter();
         XMLSerializer serial = new XMLSerializer(sw, new org.apache.xml.serialize.OutputFormat(aDoc,
               "UTF-8", true));
         serial.serialize(aDoc);
         //LogService.log(LogService.DEBUG,"viewXMl ready:\n"+sw.toString());
         Utility.logMessage("DEBUG", "****************************************");
         Utility.logMessage("DEBUG", aMsg + "\n" + sw.toString());
         Utility.logMessage("DEBUG", "########################################");
      } catch (Exception e) {
         //Utility.logMessage("ERROR",e.toString());
         Utility.logMessage("ERROR", e.toString());
      }
      return;
   }

   /**
    * Prints an Element. Used for debugging.
    * @param anElem
    * @param aMessage
    */
   public static void printElement (Element anElem, String aMessage) {
      DocumentImpl prtDoc = new DocumentImpl();
      org.w3c.dom.Node cpy = anElem.cloneNode(true);
      prtDoc.adoptNode(cpy);
      prtDoc.appendChild(cpy);
      Utility.printDoc(prtDoc, aMessage);
      return;
   }

   /**
    * Prints a DocumentImpl as a string
    * @param aDoc
    * @return String
    */
   public static String asString (DocumentImpl aDoc) {
      StringWriter sw = new StringWriter();
      try {
         XMLSerializer serial = new XMLSerializer(sw, new org.apache.xml.serialize.OutputFormat(aDoc,
               "UTF-8", true));
         serial.serialize(aDoc);
      } catch (Exception e) {
         Utility.logMessage("ERROR", "Utility::asString(): " + e.toString());
      }
      return  sw.toString();
   }

   /**
    * Extracts the value of a key sandwiched by delimiters from a string.
    * @param fromDelim
    * @param source
    * @param toDelim
    * @return String
    */
   public static String parseStringDelimitedBy (String fromDelim, String source, String toDelim) {
      Utility.logMessage("DEBUG", "Utility::parseStringDelimitedBy(): fromDelim = " + fromDelim +
            " source = " + source + " toDelim = " + toDelim);
      String parsedString = null;
      String tagString = null;
      int idxFrom = source.indexOf(fromDelim);
      int idxTo;
      //Utility.logMessage("DEBUG", "Utility::parseStringDelimitedBy(): idxFrom = " + idxFrom);
      if (idxFrom > -1) {
         tagString = source.substring(idxFrom);
         //Utility.logMessage("DEBUG", "Utility::parseStringDelimitedBy(): tagString = " + tagString);
         idxTo = tagString.indexOf(toDelim);
         if (idxTo < 0) {
            parsedString = tagString.substring(fromDelim.length());
         }
         else {
            parsedString = tagString.substring(fromDelim.length(), tagString.indexOf(toDelim));
         }
      }
      Utility.logMessage("DEBUG", "Utility::parseStringDelimitedBy(): Returning parsedString = "
            + parsedString);
      return  parsedString;
   }

   /**
    * Returns the IGroupMember represented by an Element
    * @param aDoc
    * @param id
    * @return IGroupMember
    */
   public static IGroupMember retrieveGroupMemberForElementId (DocumentImpl aDoc, String id) {

      /** @todo come up with a grpMgr exception, this should be used to return the err msg */
      Element gmElem = Utility.getElementById(aDoc, id);
      IGroupMember gm;
      if (gmElem == null) {
         Utility.logMessage("ERROR", "Utility::retrieveGroupMemberForElementId(): Unable to retrieve the element with id = "
               + id);
         return  null;
      }
      else {
         //gmElem = (Element)gmElem;
         Utility.logMessage("DEBUG", "Utility::retrieveGroupMemberForElementId(): The child type = "
               + gmElem.getTagName());
      }
      String gmKey = gmElem.getAttribute("key");
      Utility.logMessage("DEBUG", "Utility::retrieveGroupMemberForElementId(): About to retrieve group member ("
            + gmElem.getTagName() + " for key: " + gmKey);
      if (gmElem.getTagName().equals(GROUP_TAGNAME)) {
         gm = (IGroupMember)GroupsManagerXML.retrieveGroup(gmKey);
      }
      else {
         gm = (IGroupMember)GroupsManagerXML.retrieveEntity(gmKey,gmElem.getAttribute("type"));
      }
      return  gm;
   }

   /**
    * Retrieves a Group Member for the provided key and of the provided type.
    * @param key
    * @param type
    * @return IGroupMember
    */
   public static IGroupMember retrieveGroupMemberForKeyAndType (String key, String type) {
      IGroupMember gm = null;

      /** @todo come up with a grpMgr exception, this should be used to return the err msg */
      if (type.equals(GROUP_CLASSNAME)) {
         gm = GroupsManagerXML.retrieveGroup(key);
         Utility.logMessage("DEBUG", "Utility::retrieveGroupMemberForKeyAndType(): Retrieved group for Type: = "
               + type + " and key: " + key);
      }
      else {
         gm = GroupsManagerXML.retrieveEntity(key,type);
         Utility.logMessage("DEBUG", "Utility::retrieveGroupMemberForKeyAndType(): Retrieved entity for Type: = "
               + type + " and key: " + key);
      }
      return  gm;
   }

   /**
    *
    * @param ownerID The user/group ID that created this IInitialGroupContext
    * @param ownerType The entity that created this IInitialGroupContext could be either a person (p) or a group (g).
    * @param groupID The Group ID referenced by this IInitialGroupContext.
    * @param ordinal Determined the order that the IInitialGroupContexts will be displayed.
    * @param expanded Determines whether the IInitialGroupContext will be expanded when first presented to the user.
    * @param dateCreated
    * @return IInitialGroupContext
    * @throws org.jasig.portal.channels.groupsmanager.ChainedException
    */
   public static IInitialGroupContext createInitialGroupContext (String ownerID, String ownerType,
         String groupID, int ordinal, boolean expanded, Timestamp dateCreated) throws ChainedException {
      return  RDBMInitialGroupContextStore.singleton().newInstance(ownerID, ownerType,
            groupID, ordinal, expanded, dateCreated);
   }

   /**
    * Extracts the value of a key sandwiched by delimiters from a string.
    * @param className
    * @param methodName
    * @param msgKey
    * @param msg
    * @return String
    */
   public static String formatMessage (String className, String methodName, String msgKey, String msg) {
      String formMsg = "";
      return  formMsg;
   }

}



