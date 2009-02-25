/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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