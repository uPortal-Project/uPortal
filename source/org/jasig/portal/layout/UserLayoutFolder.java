/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout;


/**
 * UserLayoutFolder summary decription sentence goes here.
 *
 * @author Michael Ivanov
 * @version 1.1
 */


  public class UserLayoutFolder extends UserLayoutNode {

     //protected List childNodes = Collections.synchronizedList(new LinkedList());
     protected String firstChildNodeId;


     public UserLayoutFolder() {
       super();
     }

     public UserLayoutFolder ( IUserLayoutNodeDescription nd ) {
       super (nd);
     }


     /**
     * Sets the first child node ID
     * @param nodeId a <code>String</code> first child node ID
     */
     public void setFirstChildNodeId ( String nodeId ) {
       this.firstChildNodeId = nodeId;
     }

     /**
     * Gets the first child node ID
     * @return a first child node ID
     */
     public String getFirstChildNodeId() {
       return firstChildNodeId;
     }

  }
