/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.al.common.restrictions;

import org.jasig.portal.PortalException;
import org.jasig.portal.layout.al.common.node.ILayoutNode;

/**
 * UserLayoutRestriction summary sentence goes here.
 *
 * @author Michael Ivanov
 * @version $Revision$
 */

public abstract class LayoutRestriction implements ILayoutRestriction {

  private String name;	
  private String restrictionExpression;
  protected RestrictionPath restrictionPath;
  
  public LayoutRestriction() {
     this("",RestrictionPath.LOCAL_RESTRICTION_PATH);
  }

  public LayoutRestriction( String name ) {
     this(name,RestrictionPath.LOCAL_RESTRICTION_PATH);
  }

  public LayoutRestriction( String name, RestrictionPath restrictionPath ) {
  	 this.name = name;
     this.restrictionPath = restrictionPath;
  }

  /**
   * Sets the name of the current restriction
   * @param a <code>String</code> name
   */
  public void setName( String name ) {
  	this.name = name;
  }
  
  /**
   * Returns the name of the current restriction
   * @return a <code>String</code> name
   */
  public String getName() {
  	return name;
  }
  
  /**
   * Returns true if the current restriction is of the given type
   * @param a <code>RestrictionType</code> type
   * @return a boolean value
   */
  public boolean is( RestrictionType type) {
  	return type.equals(getType());
  }
  
  /**
   * Sets the restriction path
   * @param restrictionPath a <code>RestrictionPath</code> path
   */
  public void setRestrictionPath ( RestrictionPath restrictionPath ) {
  	this.restrictionPath = restrictionPath;
  }
  
  /**
   * Returns the type of the current restriction.
   * @return a <code>RestrictionType</code> type
   */
  protected abstract RestrictionType getType();
  
  /**
     * Parses the restriction expression of the current node
     * @exception PortalException
     */
  protected abstract void parseRestrictionExpression() throws PortalException;

  /**
     * Checks the restriction for the given property value
     * @param propertyValue a <code>String</code> property value to be checked
     * @exception PortalException
     */
  public abstract boolean checkRestriction(String propertyValue) throws PortalException;
  
  /**
   * Checks the restriction on a given node
   * @param node a <code>ILayoutNode</code> node
   * @return a boolean value
   * @exception PortalException
   */
  public abstract boolean checkRestriction ( ILayoutNode node ) throws PortalException;

  /**
     * Sets the restriction expression
     * @param restrictionExpression a <code>String</code> expression
     */
  public void setRestrictionExpression ( String restrictionExpression ) {
    if ( !restrictionExpression.equals(this.restrictionExpression) ) {
     this.restrictionExpression = restrictionExpression;
     try { parseRestrictionExpression(); }
     catch ( PortalException pe ) { pe.printStackTrace();
                                    System.out.println( "restriction expression: " + restrictionExpression );
                                    System.out.println("setRestrictionExpression: " + pe);
                                  }
    }
  }



  /**
     * Gets the restriction expression
     * @return a <code>String</code> expression
     */
  public String getRestrictionExpression() {
     return restrictionExpression;
  }


  /**
     * Gets the tree path for the current restriction
     * @return a <code>RestrictionPath</code> tree path
     */
  public RestrictionPath getRestrictionPath() {
     return restrictionPath;
  }

}
