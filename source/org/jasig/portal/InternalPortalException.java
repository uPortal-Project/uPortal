/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

/**
 * A serious internal portal exception.
 * @author Peter Kharchenko
 * @version $Revision$
 */

public class InternalPortalException extends Throwable {
    protected Throwable exc;

    public InternalPortalException(Throwable e) {
        this.exc=e;
    }

    public Throwable getException() {
        return this.exc;
    }

}
