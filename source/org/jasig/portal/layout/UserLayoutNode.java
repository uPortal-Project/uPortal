/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jasig.portal.layout.restrictions.IUserLayoutRestriction;

/**
 * Represents a node in the user layout tree.
 *
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version $Revision$
 */
public class UserLayoutNode {

     protected String parentNodeId;
     protected String nextNodeId;
     protected String previousNodeId;
     protected IUserLayoutNodeDescription nodeDescription;

     protected int priority = 0;
     protected int depth = 1;
     protected String groupName = "";
     // this object contains the restrictions for this node of ICachingRestrictions type
     protected Set restrictions = Collections.synchronizedSet(new HashSet());

     public UserLayoutNode() {}

     public UserLayoutNode ( IUserLayoutNodeDescription nd ) {
       nodeDescription = nd;
     }

     public String getId() {
        return nodeDescription.getId();
     }

     public void setNodeDescription ( IUserLayoutNodeDescription nd ) {
       nodeDescription = nd;
     }

     public IUserLayoutNodeDescription getNodeDescription() {
       return nodeDescription;
     }

     public void setParentNodeId ( String parentNodeId ) {
      this.parentNodeId = parentNodeId;
     }

     public String getParentNodeId() {
       return parentNodeId;
     }

     public void setNextNodeId ( String nextNodeId ) {
      this.nextNodeId = nextNodeId;
     }

     public String getNextNodeId() {
       return nextNodeId;
     }

     public void setPreviousNodeId ( String previousNodeId ) {
      this.previousNodeId = previousNodeId;
     }

     public String getPreviousNodeId() {
       return previousNodeId;
     }

     /**
      * Sets the hashtable of restrictions bound to this node
      * @param restrictions a <code>Hashtable</code> hashtable of restriction expressions
      */
     public void setRestrictions ( Set restrictions ) {
       this.restrictions = restrictions;
     }

     /**
      * Gets the hashtable of restrictions bound to this node
      * @return a hashtable of restriction expressions
      */
     public Set getRestrictions () {
       return restrictions;
     }

     /**
      * Adds the restriction for this node.
      * @param restriction a <code>IUserLayoutRestriction</code> a restriction
      */
     public void addRestriction( IUserLayoutRestriction restriction ) {
       restrictions.add(restriction);
     }

     /**
      * Sets the priority for this node.
      * @param priority a <code>int</code> priority value
      */
     public void setPriority ( int priority ) {
       this.priority = priority;
     }

     /**
      * Gets the priority value for this node.
      */
     public int getPriority() {
       return priority;
     }

     /**
      * Sets the tree depth for this node.
      * @param depth a <code>int</code> depth value
      */
     public void setDepth ( int depth ) {
       this.depth = depth;
     }

     /**
      * Gets the depth value for this node.
      * @return the depth of this node
      */
     public int getDepth() {
       return depth;
     }

     /**
      * Sets the group name for this node.
      * @param groupName a <code>String</code> group name value
      */
     public void setGroupName ( String groupName ) {
       this.groupName = groupName;
     }

     /**
      * Gets the group name for this node.
      * @return the group name
      */
     public String getGroupName() {
       return groupName;
     }

  }
