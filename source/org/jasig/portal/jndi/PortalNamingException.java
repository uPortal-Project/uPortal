/* Copyright 2001, 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.jndi;

import javax.naming.NamingException;

/**
 * PortalNamingException
 * @author Bernie Durfee, bdurfee@interactivebusiness.com
 * @version $Revision$ $Date$
 */
public class PortalNamingException extends NamingException {
    
  /**
   * Instantiate a PortalNamingException with the given explanation of the problem.
   * @param explanation explanation of the problem
   */
  public PortalNamingException(String explanation) {
        super(explanation);
    }

    /**
     * Instantiate a PortalNameingException with the given explanation and cause.
     * @param message message describing problem
     * @param cause underlying cause
     */
    public PortalNamingException(String message, Throwable cause) {
        super(message);
        setRootCause(cause);
    }
  
}