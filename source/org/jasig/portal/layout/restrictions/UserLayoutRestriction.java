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


package org.jasig.portal.layout.restrictions;

import org.jasig.portal.PortalException;
import org.jasig.portal.layout.UserLayoutNode;

/**
 * <p>Title: UserLayoutRestriction class</p>
 * <p>Description: The base class for UserLayout restrictions</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Instructional Media & Magic</p>
 * @author Michael Ivanov
 * @version 1.0
 */

public abstract class UserLayoutRestriction implements IUserLayoutRestriction {


  private String restrictionName;
  private String restrictionExpression;
  private String nodePath;
  protected UserLayoutNode node;

  public UserLayoutRestriction( UserLayoutNode node ) {
     this.node = node;
  }

  /**
     * Parses the restriction expression of the current node
     * @exception PortalException
     */
  protected abstract void parseRestrictionExpression() throws PortalException;

  /**
     * Checks the restriction for the given node parsing the restriction expression
     * @param node a <code>UserLayoutNode</code> object
     * @exception PortalException
     */
  public abstract boolean checkRestriction(UserLayoutNode node) throws PortalException;


  /**
   * Returns the type of the current restriction
   * @return a restriction type respresented in the <code>RestrictionTypes</code> interface
   */
  public abstract int getRestrictionType();


  /**
     * Checks the restriction for the current node
     * @param needParsing a boolean value specified if the existent expression needs to be parsed
     * @exception PortalException
     */
  public boolean checkRestriction( boolean needParsing ) throws PortalException {
    if ( needParsing ) parseRestrictionExpression();
    if ( nodePath != null ) checkRestriction(nodePath);
    return checkRestriction(node);
  }


    /**
     * Gets all the nodes specified by the ptah expression
     * @return a <code>String</code> name of the restriction
     */
     protected UserLayoutNode[] findNodesByPath( String url) {
      // TO BE DONE!!!
      return new UserLayoutNode[] { node };
     }

  /**
     * Checks the restriction for the nodes on the given path parsing the restriction expression
     * @param url a <code>String</code> tree node path in the User Layout
     * @exception PortalException
     */
  public boolean checkRestriction(String url) throws PortalException {
    if ( url == null ) return checkRestriction();
    else {
       UserLayoutNode[] nodes = findNodesByPath(url);
       int i = 0;
       for ( ; i < nodes.length && checkRestriction(node); i++ );
       if ( i == nodes.length )
        return true;
        return false;
    }
  }

  /**
     * Checks the restriction for the current node without necessity to parse the restriction expression
     * @exception PortalException
     */
  public boolean checkRestriction() throws PortalException {
    return checkRestriction(false);
  }

  /**
     * Sets the name of the restriction
     * @param restrictionName a <code>String</code> name of the restriction
     */
  public void setRestrictionName ( String restrictionName ) {
    this.restrictionName = restrictionName;
  }


  /**
     * Gets the name of the restriction
     * @return a <code>String</code> name of the restriction
     */
  public String getRestrictionName() {
    return restrictionName;
  }

  /**
     * Sets the name of the restriction
     * @param nodePath a <code>String</code> path in the UserLayout tree to be checked
     */
  public void setUserLayoutNodePath ( String nodePath ) {
    this.nodePath = nodePath;
  }


  /**
     * Gets the name of the restriction
     * @return a <code>String</code> path in the UserLayout tree to be checked
     */
  public String getUserLayoutNodePath() {
    return nodePath;
  }

  /**
     * Sets the restriction expression
     * @param restrictionExpression a <code>String</code> expression
     */
  public void setRestrictionExpression ( String restrictionExpression ) {
    if ( !restrictionExpression.equals(this.restrictionExpression) ) {
     this.restrictionExpression = restrictionExpression;
     try { parseRestrictionExpression(); }
     catch ( PortalException pe ) { System.out.println("setRestrictionExpression: " + pe); }
    }
  }



  /**
     * Gets the restriction expression
     * @return a <code>String</code> expression
     */
  public String getRestrictionExpression() {
     return restrictionExpression;
  }

}