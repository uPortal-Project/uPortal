package org.jasig.portal.layout;


import org.jasig.portal.PortalException;

/**
 * <p>Title: Priority Restriction class</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Instructional Media & Magic </p>
 * @author Michael Ivanov
 * @version 1.1
 */

public class PriorityRestriction implements IUserLayoutRestriction {


 public boolean checkRestriction( UserLayoutNode node ) throws PortalException {
   int priority = node.getPriority();
   String restrictionExp = node.getRestrictionExpression("priority");
   // Parsing restrictionExp and return the appropriate result
   return restrictionExp != null;
 }

}
