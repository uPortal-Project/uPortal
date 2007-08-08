/* Copyright 2001, 2004 The JA-SIG Collaborative.  All rights reserved.
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
 * @version $Revision$ $Date$ 
 */
public class GroupsException extends org.jasig.portal.PortalException {
    
    /**
     *  Instantiate a bare GroupsException.
     * Deprecated because it would be so much more helpful to use a contructor
     * that includes a descriptive message.
     * @deprecated use a more informative constructor
     */
    public GroupsException() {
        super();
    }
    
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

    /**
     * Always returns zero.
     * @return 0
     * @deprecated
     */
    public int getExceptionCode() {
        return 0;
    }
}
