/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.restrictions;

import org.jasig.portal.PortalException;
import org.jasig.portal.utils.ResourceLoader;

import java.util.HashSet;
import java.util.Set;
import java.util.Properties;
import java.util.Enumeration;

/**
 * UserLayoutRestrictionFactory class.
 * The factory for layout restrictions 
 *
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version $Revision$
 */

public class UserLayoutRestrictionFactory {
	
 private static final String RESTRICTIONS_PATH = 	"/properties/al/restrictions.properties";
	
 private static Set activeRestrictions;
 
 private static void init() throws PortalException {
  try {	
 	activeRestrictions = new HashSet();
 	Properties props = ResourceLoader.getResourceAsProperties(UserLayoutRestrictionFactory.class,RESTRICTIONS_PATH);
 	for ( Enumeration elems = props.keys(); elems.hasMoreElements(); ) {
 	  	 String restrClassName = elems.nextElement().toString(); 
 	  	 if ( "Y".equalsIgnoreCase(props.getProperty(restrClassName)) )
 	  	   activeRestrictions.add(restrClassName);
 	}
  } catch ( Exception e ) {
  	 throw new PortalException ( "init: " + e.toString() ); 
  }
 }
 
	
 public static IUserLayoutRestriction createRestriction ( String className, String restrictionValue, String restrictionPath ) throws PortalException {
  if ( activeRestrictions == null )
  	init();
  if ( !activeRestrictions.contains(className) )
 	throw new PortalException ("The allowed set of restrictions does not contain the restriction '" + className +"'" );	
  try {	
 	IUserLayoutRestriction restriction = (IUserLayoutRestriction) Class.forName(className).newInstance();
 	restriction.setRestrictionExpression(restrictionValue);
 	restriction.setRestrictionPath(restrictionPath);
 	return restriction;
  } catch ( Exception e ) {
  	  throw new PortalException ( "createRestriction: " + e.toString() );
  }
 }

 public static IUserLayoutRestriction createRestriction( int restrictionType, String restrictionValue, String restrictionPath ) throws PortalException {
    IUserLayoutRestriction restriction = null;
    switch ( restrictionType ) {
      case RestrictionTypes.DEPTH_RESTRICTION:
        restriction = new DepthRestriction(restrictionPath);
        break;
      case RestrictionTypes.GROUP_RESTRICTION:
        restriction = new GroupRestriction(restrictionPath);
        break;
      case RestrictionTypes.HIDDEN_RESTRICTION:
        restriction = new HiddenRestriction(restrictionPath);
        break;
      case RestrictionTypes.IMMUTABLE_RESTRICTION:
        restriction = new ImmutableRestriction(restrictionPath);
        break;
      case RestrictionTypes.PRIORITY_RESTRICTION:
        restriction = new PriorityRestriction(restrictionPath);
        break;
      case RestrictionTypes.UNREMOVABLE_RESTRICTION:
        restriction = new UnremovableRestriction(restrictionPath);
        break;
    }
        if ( restriction == null )
          throw new PortalException ("Cannot create restriction for the given type = " + restrictionType );
    
        restriction.setRestrictionExpression(restrictionValue);
        return restriction;
 }
 
 public static IUserLayoutRestriction createRestriction( int restrictionType, String restrictionValue ) throws PortalException {
        return createRestriction(restrictionType,restrictionValue,IUserLayoutRestriction.LOCAL_RESTRICTION_PATH);     
 }

}