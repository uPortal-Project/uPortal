/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.al.common.restrictions;


/**
 * The restriction type class.
 *
 * @author Michael Ivanov
 * @version $Revision$
 */

public class RestrictionType {
	
  public static final RestrictionType PRIORITY_RESTRICTION = new RestrictionType("priority");
  public static final RestrictionType DEPTH_RESTRICTION = new RestrictionType("depth");
  public static final RestrictionType GROUP_RESTRICTION = new RestrictionType("group");
  public static final RestrictionType IMMUTABLE_RESTRICTION = new RestrictionType("immutable");
  public static final RestrictionType UNREMOVABLE_RESTRICTION = new RestrictionType("unremovable");
  public static final RestrictionType HIDDEN_RESTRICTION = new RestrictionType("hidden");
	
  private final String type;	

  protected RestrictionType( String type ) {
    this.type = type;
  }
  
  public String getType() {
  	return type;
  }
  
  public String toString() {
  	return type;
  }
  
  public boolean equals ( Object obj ) {
  	 return ( (obj instanceof RestrictionType) && obj.toString().equals(type) );
  }
  
}
