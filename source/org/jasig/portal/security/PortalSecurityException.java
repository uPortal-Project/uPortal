/* Copyright 2001, 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security;

import org.jasig.portal.PortalException;

/**
 * <p>An exception representing a Portal security problem.</p>
 *
 * @author Andrew Newman, newman-andy@yale.edu
 * @version $Revision$ $Date$
 */
public class PortalSecurityException extends PortalException {

    /**
     * Instantiate a bare PortalSecurityException.
     * @deprecated instead use a more informative constructor
     */
    public PortalSecurityException() {
        super();
    }
    
    
    /**
     * Instantiate a PortalSecurityException with the given cause.
     * @param cause Throwable that caused the error condition
     */
    public PortalSecurityException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiate a PortalSecurityException with the given message.
     * @param message information about the error condition
     */
    public PortalSecurityException(String message) {
        super(message);
    }

    /**
     * Instantiate a PortalSecurityException with the given message and 
     * underlying cause
     * @param message information about the error condition
     * @param cause underlying cause of error condition
     */
    public PortalSecurityException(String message, Throwable cause) {
        super(message, cause);
    }
}