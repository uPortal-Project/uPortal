/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.restrictions;

import org.jasig.portal.PortalException;
import org.jasig.portal.layout.ILayoutNode;

/**
 * UserLayoutRestriction summary sentence goes here.
 *
 * @author Michael Ivanov
 * @version $Revision$
 */

public abstract class UserLayoutRestriction implements IUserLayoutRestriction {

  private String name;	
  private String restrictionExpression;
  protected String nodePath;
  
  public UserLayoutRestriction() {
     this("",LOCAL_RESTRICTION_PATH);
  }

  public UserLayoutRestriction( String name ) {
     this(name,LOCAL_RESTRICTION_PATH);
  }

  public UserLayoutRestriction( String name, String nodePath ) {
  	 this.name = name;
     this.nodePath = nodePath;
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
   * Sets the restriction path
   * @param restrictionPath a <code>String</code> path
   */
  public void setRestrictionPath ( String nodePath ) {
  	this.nodePath = nodePath;
  }
  

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
  public boolean checkRestriction ( ILayoutNode node ) throws PortalException {
  	return true;
  }


  /**
     * Gets the restriction name
     * @return a <code>String</code> restriction name
     */
  public String getUniqueKey() {
     return getUniqueKey(getName(),nodePath);
  }


  /**
     * Gets the restriction name based on a restriction name and a node path
     * @param restrictionName a restriction name
     * @param nodePath a <code>String</code> node path
     * @return a <code>String</code> restriction name
     */
  public static String getUniqueKey( String restrictionName, String nodePath ) {
  	 if ( nodePath!=null && nodePath.length() > 0  ) 
  	 	nodePath = LOCAL_RESTRICTION_PATH;
     return restrictionName+":"+nodePath;
  }

  /**
   * Gets the local restriction name based on a restriction type
   * @param restrictionName a restriction name
   * @return a <code>String</code> restriction name
   */
  public static String getUniqueKey( String restrictionName) {
	 return getUniqueKey(restrictionName,LOCAL_RESTRICTION_PATH);
  }

  

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
     * @return a <code>String</code> tree path
     */
  public String getRestrictionPath() {
     return nodePath;
  }

}
