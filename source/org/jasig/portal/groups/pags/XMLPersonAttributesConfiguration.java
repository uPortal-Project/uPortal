/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.groups.pags;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.jasig.portal.groups.pags.PersonAttributesGroupStore.GroupDefinition;
import org.jasig.portal.groups.pags.PersonAttributesGroupStore.TestGroup;
import org.jasig.portal.utils.ResourceLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Read in the configuration for the Person Attributes group store using
 * an XML file.  See the example file in /properties/groups/PAGSGroupStoreConfig.xml
 * and the DTD in /webpages/dtd/PAGSGroupStore.dtd for information on the
 * file format.
 *
 * @author Al Wold
 * @version $Revision$
 */
public class XMLPersonAttributesConfiguration
   implements IPersonAttributesConfiguration {

   public Map getConfig() {
      Map groupDefinitions;
      Document config = null;
      try {
         config =
            ResourceLoader.getResourceAsDocument(
               this.getClass(),
               "/properties/groups/PAGSGroupStoreConfig.xml");
      } catch (Exception rme) {
         throw new RuntimeException("PersonAttributesGroupStore: Unable to find configuration document");
      }
      groupDefinitions = new HashMap();
      config.normalize();
      Element groupStoreElement = config.getDocumentElement();
      NodeList groupElements = groupStoreElement.getChildNodes();
      for (int i = 0; i < groupElements.getLength(); i++) {
         if (groupElements.item(i) instanceof Element) {
            GroupDefinition groupDef = initGroupDef((Element) groupElements.item(i));
            groupDefinitions.put(groupDef.getKey(), groupDef);
         }
      }
      return groupDefinitions;
   }
   private GroupDefinition initGroupDef(Element groupElement) {
      GroupDefinition groupDef = new GroupDefinition();
      NodeList children = groupElement.getChildNodes();
      for (int i = 0; i < children.getLength(); i++) {
         if (children.item(i) instanceof Element) {
            Element element = (Element)children.item(i);
            String tagName = element.getTagName();
            element.normalize();
            String text = null;
            if (element.getFirstChild() instanceof Text) {
               text = ((Text)element.getFirstChild()).getData().trim();
            }
            if (tagName.equals("group-key")) {
               groupDef.setKey(text);
            } else if (tagName.equals("group-name")) {
               groupDef.setName(text);
            } else if (tagName.equals("group-description")) {
               groupDef.setDescription(text);
            } else if (tagName.equals("selection-test")) {
               NodeList testGroups = element.getChildNodes();
               for (int j = 0; j < testGroups.getLength(); j++) {
                  Node testGroup = testGroups.item(j);
                  if (testGroup instanceof Element && ((Element)testGroup).getTagName().equals("test-group")) {
                     TestGroup tg = new TestGroup();
                     NodeList tests = testGroup.getChildNodes();
                     for (int k = 0; k < tests.getLength(); k++) {
                        Node test = tests.item(k);
                        if (test instanceof Element && ((Element)test).getTagName().equals("test")) {
                           String attribute = null;
                           String tester = null;
                           String value = null;
                           NodeList parameters = test.getChildNodes();
                           for (int l = 0; l < parameters.getLength(); l++) {
                              Node parameter = parameters.item(l);
                              text = null;
                              String nodeName = parameter.getNodeName();
                              if (parameter.getFirstChild() != null &&
                                  parameter.getFirstChild() instanceof Text) {
                                     text = ((Text)parameter.getFirstChild()).getData();
                              }
                              if (nodeName.equals("attribute-name")) {
                                 attribute = text;
                              } else if (nodeName.equals("tester-class")) {
                                 tester = text;
                              } else if (nodeName.equals("test-value")) {
                                 value = text;
                              }
                           }
                           IPersonTester testerInst = initializeTester(tester, attribute, value);
                           tg.addTest(testerInst);
                        }
                        groupDef.addTestGroup(tg);
                    }
                  }
               }
            } else if (tagName.equals("members")) {
               addMemberKeys(groupDef, element);
            }
         }
      }
      return groupDef;
   }
   private void addMemberKeys(GroupDefinition groupDef, Element members) {
      NodeList children = members.getChildNodes();
      for (int i = 0; i < children.getLength(); i++) {
         Node node = children.item(i);
         if (node instanceof Element && node.getNodeName().equals("member-key")) {
            Element member = (Element)node;
            member.normalize();
            if (member.getFirstChild() instanceof Text) {
               groupDef.addMember(((Text)member.getFirstChild()).getData());
            }
         }
      }
   }
   private IPersonTester initializeTester(String tester, String attribute, String value) {
         try {
            Class testerClass = Class.forName(tester);
            Constructor c = testerClass.getConstructor(new Class[]{String.class, String.class});
            Object o = c.newInstance(new Object[]{attribute, value});
            return (IPersonTester)o;
         } catch (Exception e) {
            e.printStackTrace();
            return null;
         }
      }
}
