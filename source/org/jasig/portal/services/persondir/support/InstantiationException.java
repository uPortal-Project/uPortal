/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support;

import org.jasig.portal.PortalException;

/**
 * Exception which may be thrown by PersonDir package implementations
 * when they experience fatal failures in instantiation.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class InstantiationException extends PortalException {

    public InstantiationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public InstantiationException(String message) {
        super(message);
    }
}
