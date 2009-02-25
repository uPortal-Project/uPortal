/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.url.processing;

/**
 * Thrown by APIs that need request parameter processing to be complete before calling.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class RequestParameterProcessingIncompleteException extends IllegalStateException {
    private static final long serialVersionUID = 1L;

    /**
     * @see IllegalStateException#IllegalStateException(String, Throwable)
     */
    public RequestParameterProcessingIncompleteException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @see IllegalStateException#IllegalStateException(String)
     */
    public RequestParameterProcessingIncompleteException(String s) {
        super(s);
    }

    /**
     * @see IllegalStateException#IllegalStateException(Throwable)
     */
    public RequestParameterProcessingIncompleteException(Throwable cause) {
        super(cause);
    }
}
