/**
 * Copyright � 2004 The JA-SIG Collaborative.  All rights reserved.
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
               text = ((Text)element.getFirstChild()).getData();
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
