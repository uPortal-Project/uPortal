/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

/**
 * This exception would inform uPortal that a
 * general rendering problem has caused a channel
 * to throw an exception.
 * @author Peter Kharchenko
 * @version $Revision$
 */

public class GeneralRenderingException extends PortalException {
    public GeneralRenderingException() {
    }

    public GeneralRenderingException(String msg) {
        super(msg);
    }

    public GeneralRenderingException(String msg, Exception e) {
        super(msg,e);
    }

    public GeneralRenderingException(String msg,boolean refresh,boolean reinstantiate) {
        super(msg,refresh,reinstantiate);
    }

    public GeneralRenderingException(String msg,Exception e,boolean refresh,boolean reinstantiate) {
        super(msg,e,refresh,reinstantiate);
    }

}
