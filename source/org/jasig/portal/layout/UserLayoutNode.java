/**
 * Copyright © 2002 The JA-SIG Collaborative.  All rights reserved.
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


package org.jasig.portal.layout;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jasig.portal.layout.restrictions.IUserLayoutRestriction;


/**
 * <p>Title: UserLayoutNode </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Instructional Media & Magic</p>
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version 1.1
 */

public class UserLayoutNode {

     protected String parentNodeId;
     protected String nextNodeId;
     protected String previousNodeId;
     protected UserLayoutNodeDescription nodeDescription;

     protected int priority = 0;
     protected int depth = 1;
     protected String groupName = "";
     // this object contains the restrictions for this node of ICachingRestrictions type
     protected Set restrictions = Collections.synchronizedSet(new HashSet());


     public UserLayoutNode() {}

     public UserLayoutNode ( UserLayoutNodeDescription nd ) {
       nodeDescription = nd;
     }


     public String getId() {
        return nodeDescription.getId();
     }

     public String getNodeType() {
       return "channel";
     }

     public void setNodeDescription ( UserLayoutNodeDescription nd ) {
       nodeDescription = nd;
     }

     public UserLayoutNodeDescription getNodeDescription() {
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
     * @param restrictionName a <code>String</code> name of the restriction
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
     * Gets the priority value for this node.
     */
     public String getGroupName() {
       return groupName;
     }

  }
