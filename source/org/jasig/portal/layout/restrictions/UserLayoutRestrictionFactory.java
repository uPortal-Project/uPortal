/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.restrictions;

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
 	 	for ( Enumeration classNames = activeRestrictions.elements(); classNames.hasMoreElements(); ) {
 	 	  String name = (String) classNames.nextElement();
 	 	  String cn = (String) classNames.nextElement();
 	 	  if ( className.equals(cn) ) {
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
 
 
 public static IUserLayoutRestriction createRestriction( String restrictionName ) throws PortalException {
 	if ( !activeRestrictions.containsKey(restrictionName) )
 	 	throw new PortalException ("The allowed set of restrictions does not contain the restriction '" + restrictionName +"'" );	
 	return getRestriction(activeRestrictions.getProperty(restrictionName));
 }
 
 public static IUserLayoutRestriction createRestriction( String restrictionName, String restrictionValue, String restrictionPath ) throws PortalException {
 	IUserLayoutRestriction restriction = createRestriction ( restrictionName );
 	restriction.setRestrictionExpression(restrictionValue);
 	restriction.setRestrictionPath(restrictionPath);
 	return restriction;
 }
 
 public static IUserLayoutRestriction createRestriction( String restrictionName, String restrictionValue ) throws PortalException {
        return createRestriction(restrictionName,restrictionValue,IUserLayoutRestriction.LOCAL_RESTRICTION_PATH);     
 }
 

}