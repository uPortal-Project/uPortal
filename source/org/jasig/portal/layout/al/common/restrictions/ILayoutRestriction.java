/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.al.common.restrictions;


import org.jasig.portal.PortalException;
import org.jasig.portal.layout.al.common.node.ILayoutNode;


/**
 * IUserLayoutRestriction is the base interface for UserLayout restrictions.
 * 
 * @author Michael Ivanov
 * @version $Revision$
 */

public interface ILayoutRestriction {

  /**
   * Returns the name of the current restriction
   * @return a <code>String</code> name
   */
  public String getName();
  
  /**
   * Sets the name of the current restriction
   * @param a <code>String</code> name
   */
  public void setName( String name );
  
  /**
   * Returns true if the current restriction is of the given type
   * @param a <code>RestrictionType</code> type
   * @return a boolean value
   */
  public boolean is( RestrictionType type);

  /**
     * Checks the restriction for the given property value
     * @param propertyValue a <code>String</code> property value to be checked
     * @return a boolean value
     * @exception PortalException
     */
  public boolean checkRestriction(String propertyValue) throws PortalException;


  /**
     * Checks the relative restriction on a given node
     * @param node a <code>ILayoutNode</code> node
     * @return a boolean value
     * @exception PortalException
     */
  public boolean checkRestriction ( ILayoutNode node ) throws PortalException;


  /**
     * Sets the restriction expression
     * @param restrictionExpression a <code>String</code> expression
     */
  public void setRestrictionExpression ( String restrictionExpression );


  /**
     * Gets the restriction expression
     * @return a <code>String</code> expression
     */
  public String getRestrictionExpression();


  /**
   * Sets the restriction path
   * @param restrictionPath a <code>RestrictionPath</code> path
   */
  public void setRestrictionPath ( RestrictionPath restrictionPath );
  
   /**
     * Gets the tree path for the current restriction
     * @return a <code>RestrictionPath</code> tree path
     */
  public RestrictionPath getRestrictionPath();

}

