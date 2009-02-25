/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
