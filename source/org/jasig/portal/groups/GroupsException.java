/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.groups;
 
/**
 * A GroupsException describes a problem in the groups structure or in
 * the groups store.  An example of a structural problem is an attempt 
 * to create a circular reference.  If the problem arises retrieving or 
 * updating the groups store, the GroupsException should wrap an Exception 
 * specific to the store, probably a java.sql.SQLException or a 
 * javax.naming.NamingException.
 *
 * @author Dan Ellentuck
 * @version 1.0, 11/29/01  
 */
public class GroupsException extends org.jasig.portal.PortalException {
/**
 * 
 */
public GroupsException() {
	super();
}
/**
 * @param msg java.lang.String
 */
public GroupsException(String msg) {
	super(msg);
}
/**
 * Add GROUPS_EXCEPTION to PortalExceptions.
 */
public int getExceptionCode() {
	return 0;
}
}
