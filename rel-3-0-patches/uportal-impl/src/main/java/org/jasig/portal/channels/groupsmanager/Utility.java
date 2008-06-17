/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.channels.groupsmanager;

import java.io.StringWriter;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;

/**
 * A class holding utility functions used by the Groups Manager channel.
 * @author Don Fracapane
 * @version $Revision$
 */
public class Utility
      implements GroupsManagerConstants {

    private static final Log log = LogFactory.getLog(Utility.class);
    
   /** Creates new Utility */
   public Utility () {
   }

   /**
    * Compares 2 strings to determine if they are functionally the same. For this
    * method null is the same as "".
    * @param one String
    * @param two String
    * @return an <code>boolean</code> object
    */
   public static boolean areEqual (String one, String two) {
      String str1 = (one==null ? "" : one).trim();
      String str2 = (two==null ? "" : two).trim();
      return str1.equals(str2);
   }

   /**
    * Answers if testString has a value other that null and "".
    * @param aString String
    * @return an <code>boolean</code> object
    */
   public static boolean notEmpty (String aString) {
      String testString = (aString==null ? "" : aString).trim();
      return !testString.equals("");
   }

   /**
    * This method will prints to the log when an exception is not passed.
    * @param msgTypeStr String
    * @param msg String
    */
   public static void logMessage (String msgTypeStr, String msg) {
      Utility.logMessage(msgTypeStr, msg, null);
      return;
   }

   /**
    * An attempt to extract all calls to logger to expedite assumed future upgrades.
    * @param msgTypeStr String
    * @param msg String
    * @param th Throwable
    */
   public static void logMessage (String msgTypeStr, String msg, Throwable th) {

      if (msgTypeStr != null && msgTypeStr.equals("ERROR"))
          log.error(msg, th);
      else if (msgTypeStr != null && msgTypeStr.equals("WARN"))
          log.warn(msg, th);
      else if (msgTypeStr != null && msgTypeStr.equals("INFO"))
          log.info(msg, th);
      else if (msgTypeStr != null && msgTypeStr.equals("FATAL"))
          log.fatal(msg, th);
      else 
          log.debug(msg, th);
 
      return;
   }

   /**
    * Extracts the value of a key sandwiched by delimiters from a string.
    * @param fromDelim
    * @param source
    * @param toDelim
    * @return an <code>String</code> object
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
    * Prints a Document. Used for debugging.
    * @param aDoc
    * @param aMessage
    */
   public static void printDoc (Document aDoc, String aMessage) {
      String aMsg = (aMessage != null ? aMessage : "");
      try {
         Utility.logMessage("DEBUG", "****************************************");
         Utility.logMessage("DEBUG", aMsg + "\n" + toString(aDoc));
         Utility.logMessage("DEBUG", "########################################");
      } catch (Exception e) {
         Utility.logMessage("ERROR", e.toString(), e);
      }
      return;
   }

   /**
    * Prints an Element. Used for debugging.
    * @param anElem
    * @param aMessage
    */
   public static void printElement (Element anElem, String aMessage) {
      Document prtDoc = GroupsManagerXML.getNewDocument();
      //org.w3c.dom.Node cpy = anElem.cloneNode(true);
      //prtDoc.adoptNode(cpy);
      Element cpy = (Element)prtDoc.importNode(anElem,true);
      prtDoc.appendChild(cpy);
      Utility.printDoc(prtDoc, aMessage);
      return;
   }

   /**
    * Retrieves a Group Member for the provided key and of the provided type.
    * @param key
    * @param type
    * @return an <code>IGroupMember</code> object
    */
   public static IGroupMember retrieveGroupMemberForKeyAndType (String key, String type) {
      IGroupMember gm = null;

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
    * Represents a document as a string.
    * @param aDoc
    * @return the document serialized out as a human-readable String
    */
   public static String toString (Document aDoc) {
      StringWriter sw = new StringWriter();
      try {
          /* TODO: This should be reviewed at some point to see if we can use the
           * DOM3 LS capability and hence a standard way of doing this rather
           * than using an internal implementation class.
           */
          OutputFormat format = new OutputFormat();
                      format.setOmitXMLDeclaration(true);
                      format.setIndenting(true);
          XMLSerializer serial = new XMLSerializer(sw, format);
         serial.serialize(aDoc);
      } catch (Exception e) {
         Utility.logMessage("ERROR", "Utility::asString(): " + e, e);
      }
      return  sw.toString();
   }

   /**
    * given a group key, return an xml group id that matches it in the provided document
    *
    * @param grpKey String
    * @param sd CGroupsManagerUnrestrictedSessionData
    * @return an <code>String</code> object
    */
   public static String translateKeytoID(String grpKey, CGroupsManagerUnrestrictedSessionData sd){
      Document viewDoc = sd.model;
      String id = null;
      Element grpViewKeyElem;
      Iterator grpItr = GroupsManagerXML.getNodesByTagNameAndKey(viewDoc, GROUP_TAGNAME,
            grpKey);
      IEntityGroup gm = GroupsManagerXML.retrieveGroup(grpKey);
      if (gm != null) {
         if (!grpItr.hasNext()) {
            grpViewKeyElem = GroupsManagerXML.getGroupMemberXml(gm, true, null, sd);
            Element rootElem = viewDoc.getDocumentElement();
            rootElem.appendChild(grpViewKeyElem);
         }
         else {
            grpViewKeyElem = (Element)grpItr.next();
            GroupsManagerXML.getGroupMemberXml(gm, true, grpViewKeyElem, sd);
         }
         id = grpViewKeyElem.getAttribute("id");
      }
      return id;
   }

   /**
    * Determines if an object has a value other than null or blank.
    * @param o Object
    * @return boolean
    */
   public static boolean hasValue (Object o) {
      boolean rval = false;
      if (o != null && !o.toString().trim().equals("")) {
         rval = true;
      }
      return  rval;
   }

   /**
    * Determines if an object has a value other than null or blank and is equal to
    * the test parameter.
    * @param o Object
    * @param test String
    * @return boolean
    */
   public static boolean hasValue (Object o, String test) {
      boolean rval = false;
      if (hasValue(o)) {
         if (String.valueOf(o).equals(test)) {
            rval = true;
         }
      }
      return  rval;
   }

}
