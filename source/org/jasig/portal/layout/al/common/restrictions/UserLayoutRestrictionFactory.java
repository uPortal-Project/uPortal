/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.al.common.restrictions;

import org.jasig.portal.PortalException;
import org.jasig.portal.utils.ResourceLoader;

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
	
 private static Properties activeRestrictions;
 
 static {
  try {	
 	 activeRestrictions = ResourceLoader.getResourceAsProperties(UserLayoutRestrictionFactory.class,RESTRICTIONS_PATH);
  } catch ( Exception e ) {
  	 throw new RuntimeException ( "init: " + e.toString() ); 
  }
 }

 private static IUserLayoutRestriction getRestriction ( String className ) throws PortalException {
 	  if ( !activeRestrictions.contains(className) )
 	 	throw new PortalException ("The allowed set of restrictions does not contain the restriction '" + className +"'" );	
 	  try {	
 	 	IUserLayoutRestriction restriction = (IUserLayoutRestriction) Class.forName(className).newInstance();
 	 	for ( Enumeration names = activeRestrictions.keys(); names.hasMoreElements(); ) {
 	 	  String name = (String) names.nextElement();
 	 	  if ( className.equals(activeRestrictions.getProperty(name)) ) {
 	 	  	 restriction.setName(name);
 	 	  	 break;
 	 	  }
 	 	}  
 	 	return restriction;
 	  } catch ( Exception e ) {
 	  	  throw new PortalException ( "createRestriction: " + e.toString() );
 	  }
 }
 
 public static Properties getAvailableRestrictions() {
 	return activeRestrictions;
 }
 
 
 public static IUserLayoutRestriction createRestriction( String restrictionType ) throws PortalException {
 	if ( !activeRestrictions.containsKey(restrictionType) )
 	 	throw new PortalException ("The allowed set of restrictions does not contain the restriction '" + restrictionType +"'" );	
 	return getRestriction(activeRestrictions.getProperty(restrictionType));
 }
 
 public static IUserLayoutRestriction createRestriction( RestrictionType restrictionType ) throws PortalException {
 	return createRestriction(restrictionType.getType());
 }
 
 public static IUserLayoutRestriction createRestriction( RestrictionType restrictionType, String restrictionValue, RestrictionPath restrictionPath ) throws PortalException {
 	IUserLayoutRestriction restriction = createRestriction ( restrictionType );
 	restriction.setRestrictionExpression(restrictionValue);
 	restriction.setRestrictionPath(restrictionPath);
 	return restriction;
 }
 
 public static IUserLayoutRestriction createRestriction( RestrictionType restrictionType, String restrictionValue ) throws PortalException {
        return createRestriction(restrictionType,restrictionValue,RestrictionPath.LOCAL_RESTRICTION_PATH);     
 }
 

}