/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

/**
 * This exception would inform uPortal that an
 * authorization violation has occured within a channel.
 * @author Peter Kharchenko
 * @version $Revision$
 */

public class AuthorizationException extends PortalException {

    public AuthorizationException() {
    }

    public AuthorizationException(String msg) {
        super(msg);
    }

    public AuthorizationException(String msg, Exception exc) {
        super(msg,exc);
    }

    public AuthorizationException(String msg,boolean refresh, boolean reinstantiate) {
        super(msg,refresh, reinstantiate);
    }

}
