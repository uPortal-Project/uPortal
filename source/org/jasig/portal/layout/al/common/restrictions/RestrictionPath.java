/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.al.common.restrictions;


/**
 * The generic restriction path class.
 *
 * @author Michael Ivanov
 * @version $Revision$
 */

public class RestrictionPath {
	
      //	 The local restriction path defined for every restriction by default 	   
  public final static RestrictionPath LOCAL_RESTRICTION_PATH =  new RestrictionPath("local");	
	  
	  // The parent restriction path, could be setup to a parent node	   
  public final static RestrictionPath PARENT_RESTRICTION_PATH = new RestrictionPath("parent");	
	  
	  // The children restriction path, could be setup to all children 
  public final static RestrictionPath CHILDREN_RESTRICTION_PATH = new RestrictionPath("children");		
	
  private String path;	
	
  public RestrictionPath() {
	 super();
  }	

  public RestrictionPath( String path ) {
    this.path = path;
  }

  public String getPath() {
  	return path;
  }
  
  public void setPath( String path ) {
  	this.path = path;
  }
  
  public String toString() {
  	return path;
  }
  
  public boolean equals ( Object obj ) {
  	 return ( (obj instanceof RestrictionPath) && obj.toString().equals(path) );
  }
  
}
