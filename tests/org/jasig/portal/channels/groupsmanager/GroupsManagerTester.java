package  org.jasig.portal.channels.groupsmanager;

import  java.util.*;
import  java.io.*;
import  org.jasig.portal.services.*;
import  java.sql.*;
import  java.util.Date;
import  junit.framework.*;
import  org.jasig.portal.*;
import  org.jasig.portal.channels.groupsmanager.commands.*;
import  org.jasig.portal.channels.groupsmanager.wrappers.*;
import  org.jasig.portal.groups.*;
import  org.jasig.portal.security.*;
import  org.jasig.portal.services.*;
import  org.jasig.portal.*;
import  org.jasig.portal.services.*;
import  org.apache.xml.serialize.XMLSerializer;
import  org.apache.xml.serialize.OutputFormat;
import  org.w3c.dom.Node;
import  org.w3c.dom.NodeList;
import  org.w3c.dom.Element;
import  org.w3c.dom.Text;
import  org.w3c.dom.Document;
import  org.apache.xerces.parsers.DOMParser;
import  org.apache.xerces.parsers.SAXParser;

/**
 * put your documentation comment here
 */
public class GroupsManagerTester extends TestCase implements GroupsManagerConstants {
   private static Class GROUP_CLASS;
   private static Class IPERSON_CLASS;
   private static String newGrpNameTemplate = "TempGroup*";
   //private String[] testGroupKeys;
   private int numTestGroups = 0;
   private static Document xmlDoc;
   // cache defaults:
   //private int cacheSize = 1000;
   //private int cacheIdleTimeSecs = 5*60;
   //private int cacheSweepIntervalSecs = 30;
   /**
    * EntityLockTester constructor comment.
    * @param name String
    */
   public GroupsManagerTester (String name) {
      super(name);
   }

   /**
    */
   protected void addTestGroups () {
      try {
         numTestGroups = 50;
         //testGroupKeys = new String[numTestGroups];
         String parentKey = "0";                //everyone
         IEntityGroup parentGroup = GroupService.findGroup(parentKey);
         String userID = "7";                   // admin
         for (int i = 0; i < numTestGroups; i++) {
            IEntityGroup childEntGrp = GroupService.newGroup(IPERSON_CLASS);
            //testGroupKeys[i] = childEntGrp.getKey();
            childEntGrp.setName(newGrpNameTemplate + i);
            childEntGrp.setCreatorID(userID);
            childEntGrp.update();
            parentGroup.addMember((IGroupMember)childEntGrp);
         }
         parentGroup.updateMembers();
      } catch (Exception ex) {
         print("GroupsManagerTester::addTestGroups(): [ERROR] " + ex.getMessage());
      }
   }

   /**
    */
   protected static void cleanUp () {
      print("***** GroupsManagerTester::cleanUp(): Cleaning up the playroom  *****");
      String entName;
      /*
      if(testGroupKeys != null){
         print(testGroupKeys);
         try {
            String parentKey = "0";                //everyone
            IEntityGroup parentGroup = GroupService.findGroup(parentKey);
            Iterator delGrpsItr = parentGroup.getMembers();
            while (delGrpsItr.hasNext()) {
               IGroupMember childGm = (IGroupMember)delGrpsItr.next();
               entName = ((IEntityGroup)childGm).getName();
               boolean keepGoing = true;
               int sub = 0;
               while (sub < testGroupKeys.length && keepGoing) {
                  if (testGroupKeys[sub].equals(childGm.getKey())) {
                     print("delete candidate key: " + childGm.getKey() + " name: " + entName);
                     keepGoing = false;
                     ((IEntityGroup)parentGroup).removeMember(childGm);
                     ((IEntityGroup)parentGroup).updateMembers();
                     ((IEntityGroup) childGm).delete();
                  }
               }
            }
            parentGroup.update();
         } catch (Exception ex) {
            print("GroupsManagerTester::deleteTestGroups(): [ERROR] " + ex.getMessage());
         }
      }
      else{
      */
         try {
            String parentKey = "0";                //everyone
            IEntityGroup parentGroup = GroupService.findGroup(parentKey);
            Iterator delGrpsItr = parentGroup.getMembers();
            while (delGrpsItr.hasNext()) {
               IGroupMember childGm = (IGroupMember)delGrpsItr.next();
               entName = ((IEntityGroup)childGm).getName();
               if (entName.startsWith(newGrpNameTemplate)) {
                  print("delete candidate key: " + childGm.getKey() + " name: " + entName);
                  ((IEntityGroup)parentGroup).removeMember(childGm);
                  ((IEntityGroup)parentGroup).updateMembers();
                  ((IEntityGroup) childGm).delete();
               }
            }
            parentGroup.update();
         } catch (Exception ex) {
            print("GroupsManagerTester::deleteTestGroups(): [ERROR] " + ex.getMessage());
         }
      //}
   }

   /**
    * Starts the application.
    * @param args an array of command-line arguments
    * @throws Exception
    */
   public static void main (java.lang.String[] args) throws Exception {
      String[] mainArgs =  {
         "org.jasig.portal.channels.groupsmanager.GroupsManagerTester"
      };
      print("###############  START TESTING Groups Manager  ###############");
      print("*");
      // bloat the groups by creating some under everyone
      //addTestGroups();
      junit.swingui.TestRunner.main(mainArgs);
      print("*");
      print("###############  END TESTING Groups Manager  ###############");
      // A group is cached when it is created. So this class should be run once to
      // create the groups and without cleanup. The second run is the real test. When
      // you want to clean up the date, uncomment the next line and run again.
      //cleanUp();
   }

   /**
    * @param keys
    */
   private static void print (Object[] keys) {
      if(keys != null){
         for (int i = 0; i < keys.length; i++) {
            print("(" + (i + 1) + ") " + keys[i]);
         }
         print("  Total: " + keys.length);
      }
   }

   /**
    * @param msg java.lang.String
    */
   private static void print (String msg) {
      java.sql.Timestamp ts = new java.sql.Timestamp(System.currentTimeMillis());
      System.out.println(ts + " : " + msg);
   }

   /**
    * @param xDoc Document
    * @param msg String
    */
   private static void printDoc(Document xDoc, String msg) {
      try{
         print(msg);
         StringWriter sw = new StringWriter();
         XMLSerializer serial = new XMLSerializer(sw, new org.apache.xml.serialize.OutputFormat(xDoc,"UTF-8", true));
         serial.serialize(xDoc);
         print(sw.toString());
      }
      catch(Throwable th){
         print("Bad thing happened in printDoc: " + th);
      }
   }

   /**
    * @param anElem
    */
   private static void printElement(Element anElem) {
      print("printElement():: About to print element");
      Collection nodes = new java.util.ArrayList();
      Element elem = null;

      org.w3c.dom.NodeList nList = anElem.getElementsByTagName("rdf:RDF");

      for (int i = 0; i < nList.getLength(); i++) {
         elem = (Element)nList.item(i);
         print("printElement(): rdf:RDF::title = " + elem.getAttribute("dc:title"));
         print("printElement(): rdf:RDF::description = " + elem.getAttribute("description"));
         print("printElement(): rdf:RDF::creator = " + elem.getAttribute("creator"));
      }
      nList = anElem.getElementsByTagName("rdf:Description");

      for (int i = 0; i < nList.getLength(); i++) {
         elem = (Element)nList.item(i);
         print("printElement(): rdf:Description::title = " + elem.getAttribute("dc:title"));
         print("printElement(): rdf:Description::description = " + elem.getAttribute("description"));
         print("printElement(): rdf:Description::creator = " + elem.getAttribute("creator"));
      }
   }

   /**
    */
   protected void setUp () {
      try {
         if (GROUP_CLASS == null) {
            GROUP_CLASS = Class.forName("org.jasig.portal.groups.IEntityGroup");
         }
         if (IPERSON_CLASS == null) {
            IPERSON_CLASS = Class.forName("org.jasig.portal.security.IPerson");
         }
      } catch (Exception ex) {
         print("GroupsManagerTester::setUp():  [ERROR]" + ex.getMessage());
      }
   }

   /**
    * @return junit.framework.Test
    */
   public static junit.framework.Test suite () {
      TestSuite suite = new TestSuite();
      suite.addTest(new GroupsManagerTester("testCaching"));
      suite.addTest(new GroupsManagerTester("testLocking"));
      return  suite;
   }

   /**
    * put your documentation comment here
    * @return ExtendedDocument
    */
   public static Document getXml () {
      String rkey = null;
      IEntityGroup entGrp = null;
      IGroupMember aGroupMember = null;
      Element rootGroupElement;
      Document viewDoc = GroupsManagerXML.getNewDocument();
      Element viewRoot = viewDoc.createElement("CGroupsManager");
      viewDoc.appendChild(viewRoot);
      //Element apRoot = GroupsManagerXML.getAuthorizationXml(sd, null, viewDoc);
      //viewRoot.appendChild(apRoot);
      Element etRoot = GroupsManagerXML.getEntityTypesXml(viewDoc);
      viewRoot.appendChild(etRoot);
      Element rootGroupsElem = GroupsManagerXML.createElement(GROUP_TAGNAME, viewDoc, true);
      rootGroupsElem.setAttribute("expanded", "true");
      /** @todo next 2 lines were from original igc code, I don't think they are needed */
      //rootGroupsElem.setAttribute("expanded", "false");
      //rootGroupsElem.setAttribute("hasMembers", "false");
      Element rdfElem = GroupsManagerXML.createRdfElement(null, viewDoc);
      rootGroupsElem.appendChild(rdfElem);
      viewRoot.appendChild(rootGroupsElem);
      try {
         // name and class for entity types with root group
         HashMap entTypes = GroupsManagerXML.getEntityTypes();
         Iterator entTypeKeys = entTypes.keySet().iterator();
         while (entTypeKeys.hasNext()) {
            Object key = entTypeKeys.next();
            Class entType = (Class)entTypes.get(key);
            IEntityGroup rootGrp = GroupService.getRootGroup(entType);
            rootGroupElement = GroupsManagerXML.getGroupMemberXml(rootGrp, true, null, viewDoc);
            /** @todo IEntityGroup.isEditable() will return the value we want */
            //rootGroupElement.setAttribute("readOnly", !rootGrp.isEditable());
            rootGroupElement.setAttribute("readOnly", "true");
            rootGroupsElem.appendChild(rootGroupElement);
         }
      } catch (Exception e) {
         //Utility.logMessage("ERROR", "GroupsManagerXML::getGroupsManagerXML(): ERROR" + e.toString());
         print("GroupsManagerTester::getXML(): [ERROR] " + e.toString());
      }
      return  viewDoc;
   }

   /**
    * @throws Exception
    */
   public void testCaching () throws Exception {
      print("***** GroupsManagerTester::testCaching(): ENTERING *****");
      long time0 = Calendar.getInstance().getTime().getTime();

      //xmlDoc = getXml();  // now saved as instance variable to be used in later tests.
      if (xmlDoc==null || xmlDoc.equals("")){
         xmlDoc=getXml();
      }

      long time1 = Calendar.getInstance().getTime().getTime();

      print(String.valueOf((time0 - time1)) + " ms total, to generate initial document");
      //printDoc(xmlDoc, "Document after FIRST construction");

      time1 = Calendar.getInstance().getTime().getTime();

      Document viewDoc2 = getXml();

      long time2 = Calendar.getInstance().getTime().getTime();
      print(String.valueOf((time1 - time2)) + " ms total, to generate second document using cached elements");
      //printDoc(viewDoc2, "Document after SECOND construction");

      print("***** GroupsManagerTester::testCaching(): LEAVING *****");
   }

   /**
    * @throws Exception
    */
   public void testLocking () throws Exception {
      print("GroupsManagerTester::testLocking(): Entering");
      String parentElemId = "4"; //getCommandIds(runtimeData);
      if (xmlDoc==null || xmlDoc.equals("")){
         xmlDoc=getXml();
      }
      Document viewDoc=xmlDoc;
      //printDoc(viewDoc, "GroupsManagerTester::testLocking(): Document:");
      // if not IPerson group, then set view root to root for requested type
      try{
         String userID = "7"; //runtimeData.getParameter("username");
         String userName = GroupsManagerXML.getEntityName(ENTITY_CLASSNAME, userID);
         String lockKey = userID + "::" + userName;
         //Element parentElem = Utility.getElementById(this.getXmlDoc(staticData),parentElemId);
         Element parentElem = viewDoc.getElementById(parentElemId);
         String parentKey = parentElem.getAttribute("key");
         print("GroupsManagerTester::testLocking(): About to lock entity for grp: " + parentKey + " with lockKey: " + lockKey);
         ILockableEntityGroup lockedGroup = GroupService.findLockableGroup(parentKey, lockKey);
         if (lockedGroup != null){
            // refresh element
            // store in staticData
            //staticData.setParameter("lockedGroup",lockedGroup);
         }
         else{
            // need to display group name
            String msg = "Unable to aquire lock for group: " + parentKey;
            print(msg);
            //runtimeData.setParameter("commandResponse", cmdResponse);
         }
      }
      catch(Exception e){
        print("Bad things happen" + e);
        //LogService.instance().log(LogService.ERROR,e);
      }
      //Utility.logMessage("DEBUG", "EditGroup::execute(): Uid of parent element = " + parentElemId);
      //staticData.setParameter("groupParentId", parentAddElemId);
   }
}



