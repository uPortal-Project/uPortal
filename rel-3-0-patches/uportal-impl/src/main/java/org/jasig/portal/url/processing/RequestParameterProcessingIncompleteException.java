/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
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
