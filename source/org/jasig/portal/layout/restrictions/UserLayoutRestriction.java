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
import org.jasig.portal.layout.ALNode;

/**
 * <p>Title: UserLayoutRelativeRestriction class</p>
 * <p>Description: The base class for UserLayout restrictions</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Instructional Media & Magic</p>
 * @author Michael Ivanov
 * @version 1.0
 */

public abstract class UserLayoutRestriction implements IUserLayoutRestriction {


  private String restrictionExpression;
  protected String nodePath;

  public UserLayoutRestriction() {

  }

  public UserLayoutRestriction( String nodePath ) {
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


  /**  Checks the relative restriction on a given node
     * @param node a <code>ALNode</code> node
     * @return a boolean value
     * @exception PortalException
     */
  public boolean checkRestriction ( ALNode node ) throws PortalException {
     return true;
  }


  /**
   * Returns the type of the current restriction
   * @return a restriction type respresented in the <code>RestrictionTypes</code> interface
   */
  public int getRestrictionType() {
     //return (nodePath==null||nodePath.length()==0)?RestrictionTypes.REMOTE_RESTRICTION:0;
     return 0;
  }

  /**
     * Gets the restriction name
     * @return a <code>String</code> restriction name
     */
  public String getRestrictionName() {
     return getRestrictionName(getRestrictionType(),nodePath);
  }


  /**
     * Gets the restriction name based on a restriction type and a node path
     * @param restrictionType a restriction type
     * @param nodePath a <code>String</code> node path
     * @return a <code>String</code> restriction name
     */
  public static String getRestrictionName( int restrictionType, String nodePath ) {
     return (nodePath!=null && nodePath.length()>0)?restrictionType+":"+nodePath:restrictionType+"";
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
