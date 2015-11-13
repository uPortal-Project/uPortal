/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.rest.layout;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TabListOfNodes implements NodeList {

   private List<Node> tab = new ArrayList<Node>();

   @Override
   public int getLength() {
       return tab.size();
   }

   @Override
   public Node item(int index) {
       return tab.get(index);
   }
   
   public void addItem(Node node) {
       tab.add(node);
   }
   
   public void addAllChannels(NodeList root) {
       for(int i = 0; i < root.getLength(); i++) {
           Node cur = root.item(i);
           if("channel".equals(cur.getNodeName())) {
               this.addItem(cur);
           } else {
               if(cur.getChildNodes() != null && cur.getChildNodes().getLength() > 0) {
                   addAllChannels(cur.getChildNodes());
               }
           }
       }
    }

}
