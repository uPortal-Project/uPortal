/* Copyright 2001, 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

/**
 * A serious internal portal exception.
 * @author Peter Kharchenko
 * @version $Revision$ $Date$
 */
public class InternalPortalException extends Throwable {
    private static final long serialVersionUID = 1L;

    /**
     * Instantiate an InternalPortalException wrapper around the given
     * Throwable.
     * @param cause - a Throwable to be wrapped
     */
    public InternalPortalException(Throwable cause) {
        super(cause);
    }

}
