/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
 * @version $Revision$ $Date$ 
 */
public class GroupsException extends org.jasig.portal.PortalException {
    
    /**
     * Instantiate a GroupsException with the given cause.
     * @param cause Throwable that caused the problem
     */
    public GroupsException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiate a GroupsException with the given message.
     * @param msg message describing problem
     */
    public GroupsException(String msg) {
        super(msg);
    }
    
    /**
     * Instantiate a GroupsException with the given message and underlying cause.
     * @param msg message describing problem
     * @param cause underlying cause
     */
    public GroupsException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
