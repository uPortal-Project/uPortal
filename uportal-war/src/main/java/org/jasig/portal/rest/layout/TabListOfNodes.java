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
