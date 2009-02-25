/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
    
    /**
     * Instantiate a generic GeneralRenderingException.
     * Deprecated because it would be so much more helpful for you to instead
     * throw an exception with a message.
     */
    public GeneralRenderingException() {
        super();
    }
    
    /**
     * Instantiate a GeneralRenderingException,
     * with cause of the problem.
     * @param cause - cause of the problem
     */
    public GeneralRenderingException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiate a GeneralRenderingException,
     * with a message describing the nature of the problem.
     * @param msg - message explaining problem
     */
    public GeneralRenderingException(String msg) {
        super(msg);
    }

    /**
     * Instantiate a GeneralRenderingException with a message
     * and a Throwable representing the underlying cause of the problem.
     * @param msg - message explaining the problem
     * @param cause - underlying Throwable
     */
    public GeneralRenderingException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Instantiate a GeneralRenderingException with a message and
     * indicating whether channel refresh and channel reinstantiation are 
     * appropriate responses to the problem.
     * @param msg - message explaining the problem
     * @param refresh - true if refresh is an appropriate response
     * @param reinstantiate - true if reinstantiation is an appropriate response
     */
    public GeneralRenderingException(String msg, boolean refresh, boolean reinstantiate) {
        super(msg, refresh, reinstantiate);
    }

    /**
     * Instantiate a GeneralRenderingException with a message and underlying
     * cause, indicating whether channel refresh and channel reinstantiation are
     * appropriate responses to the problem.
     * @param msg - message explaining the problem
     * @param cause - underlying cause of problem
     * @param refresh - true if refresh is appropriate response
     * @param reinstantiate - true if reinstantiation is appropriate response
     */
    public GeneralRenderingException(String msg, Throwable cause,
            boolean refresh, boolean reinstantiate) {
        super(msg, cause, refresh, reinstantiate);
    }

}
