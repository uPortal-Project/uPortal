package org.jasig.portal.layout;

import java.util.Hashtable;

/**
 * <p>Title: UserLayoutNode </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Instructional Media & Magic</p>
 * @author Michael Ivanov
 * @version 1.0
 */

public class UserLayoutNode {

     protected String parentNodeId;
     protected String nextNodeId;
     protected String previousNodeId;
     protected UserLayoutNodeDescription nodeDescription;

     protected int priority = 0;
     protected int depth = 1;
     protected String groupName = "";
     protected Hashtable restrictions;

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
     public void setRestrictions ( Hashtable restrictions ) {
       this.restrictions = restrictions;
     }

     /**
     * Gets the restriction expression value for this node by the restriction name.
     * @param restrictionName a <code>String</code> name of the restriction
     */
     public String getRestrictionExpression( String restrictionName ) {
       return (String)restrictions.get(restrictionName);
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
     public void setDepth ( int priority ) {
       this.priority = priority;
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
