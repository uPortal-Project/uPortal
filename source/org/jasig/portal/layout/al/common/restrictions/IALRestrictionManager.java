/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.al.common.restrictions;

import org.jasig.portal.PortalException;
import org.jasig.portal.layout.al.IALNode;


/**
 * The AL Restriction Manager Interface
 *
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version $Revision$
 */
public interface IALRestrictionManager extends IRestrictionManager {


  /**
     * Checks the restriction specified by the parameters below.
     * @param node a <code>ALNode</code> node to be checked
     * @param restrictionType a <code>RestrictionType</code> restriction type
     * @param restrictionPath a <code>RestrictionPath</code> restriction path
     * @param propertyValue a <code>String</code> property value to be checked
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  public boolean checkRestriction(IALNode node, RestrictionType restrictionType, RestrictionPath restrictionPath, String propertyValue) throws PortalException;

  /**
     * Checks the local restriction specified by the parameters below.
     * @param node a <code>ALNode</code> node to be checked
     * @param restrictionType a <code>RestrictionType</code> restriction type
     * @param propertyValue a <code>String</code> property value to be checked
     * @return a boolean value
     * @exception PortalException if an error occurs
     */
  public boolean checkRestriction(IALNode node, RestrictionType restrictionType, String propertyValue ) throws PortalException;
  
}
