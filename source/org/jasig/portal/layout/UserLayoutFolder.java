package org.jasig.portal.layout;

import java.util.*;

/**
 * <p>Title: UserLayoutFolder </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Instructional Media & Magic</p>
 * @author Michael Ivanov
 * @version 1.0
 */


  public class UserLayoutFolder extends UserLayoutNode {

     protected List childNodes = Collections.synchronizedList(new LinkedList());


     public UserLayoutFolder() {
       super();
     }

     public UserLayoutFolder ( UserLayoutNodeDescription nd ) {
       super (nd);
     }


     public String getNodeType() {
       return "folder";
     }

     public void setChildNodes ( List childNodes ) {
      this.childNodes = childNodes;
     }


     public List getChildNodes() {
       return childNodes;
     }

     public void addChildNode ( String nodeId ) {
       childNodes.add ( nodeId );
     }

     public void deleteChildNode ( String nodeId ) {
       childNodes.remove( nodeId );
     }

  }
