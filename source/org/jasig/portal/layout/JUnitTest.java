
package org.jasig.portal.layout;

import java.util.*;

import org.jasig.portal.security.*;
import org.jasig.portal.IUserLayoutStore;
import org.jasig.portal.UserLayoutStoreFactory;
import org.jasig.portal.UserProfile;
import org.jasig.portal.layout.restrictions.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import org.w3c.dom.Document;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import java.io.*;

import org.jasig.portal.utils.XML;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * <p>Title: JUnit test cases</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class JUnitTest extends TestCase {

  private static int LAYOUT_ID = 1;
  private static int USER_ID = 2;

  private static IPerson person;
  private static IUserLayoutStore layoutStore;
  private static UserProfile userProfile;
  private AggregatedUserLayoutImpl layoutManager;

  static {
    try {
      layoutStore =  UserLayoutStoreFactory.getUserLayoutStoreImpl();
      person = (org.jasig.portal.security.provider.PersonImpl) Class.forName("org.jasig.portal.security.provider.PersonImpl").newInstance();
      person.setID(USER_ID);
      userProfile = layoutStore.getUserProfileById(person,LAYOUT_ID);
    } catch ( Exception e ) {
        e.printStackTrace();
        System.exit(0);
      }
  }

  public JUnitTest(String name ) {
    super(name);
  }

  protected void setUp() {
    try {
      layoutManager = new AggregatedUserLayoutImpl(person, userProfile, (IAggregatedUserLayoutStore)layoutStore );
      layoutManager.loadUserLayout();
      layoutManager.setAutoCommit(false);
      layoutManager.markMoveTargets("17");
      layoutManager.markAddTargets(new UserLayoutChannelDescription());
    } catch ( Exception e ) {
       e.printStackTrace();
       System.exit(0);
      }
  }

  protected void tearDown() {
      layoutManager = null;
  }

  public static Test suite() {
    TestSuite suite = new TestSuite(JUnitTest.class);
    return suite;
  }

  public void testSaveLayout() {
   try {
    layoutManager.saveUserLayout();
   } catch ( Exception e ) {
       e.printStackTrace();
       fail(e.getMessage());
     }
  }

  public void testDeleteNode() {
   try {
     assertTrue(layoutManager.deleteNode("39"));
   } catch ( Exception e ) {
       e.printStackTrace();
       fail(e.getMessage());
     }
  }

  public void testMoveNode() {
   try {
     assertTrue(layoutManager.moveNode("39","10","13"));
   } catch ( Exception e ) {
       e.printStackTrace();
       fail(e.getMessage());
     }
  }

  public void testAddNode() {
   try {
     IALNodeDescription nodeDesc = new ALChannelDescription();
     nodeDesc = (IALNodeDescription)layoutManager.addNode(nodeDesc,"10","13");
     assertNotNull(nodeDesc.getId());
   } catch ( Exception e ) {
       e.printStackTrace();
       fail(e.getMessage());
     }
  }

  public static void main(String args[] ) {
     junit.textui.TestRunner.run(suite());
  }
}
