/* Copyright 2001, 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

/**
 * This exception would inform uPortal that a
 * a channel has encountered an internal timeout
 * exception.
 * @author Peter Kharchenko
 * @version $Revision$
 */
public class InternalTimeoutException extends PortalException {
    
    /**
     * Timeout value, in milliseconds, that was exceeded.
     */
    private Long timeoutValue = null;

    /**
     * Instantiate a bare InternalTimeoutException.
     * Deprecated because it would be so much more helpful if you
     * instead use a constructor that takes a message, etc.
     */
    public InternalTimeoutException() {
        super();
    }

    /**
     * Instantiate an InternalTimeoutException, conveying the given message.
     * @param msg message explaining the nature of the timeout
     */
    public InternalTimeoutException(String msg) {
        super(msg);
    }

    /**
     * Instantiate an InternalTimeoutException conveying a message and
     * specifying the timeout that was exceeded.
     * @param msg describes nature of timeout
     * @param timeoutValue the timeout value in milliseconds that was exceeded
     */
    public InternalTimeoutException(String msg,long timeoutValue) {
        super(msg);
        this.timeoutValue = new Long(timeoutValue);
    }

    /**
     * Instantiate an InternalTimeoutException conveying a message and
     * specifying the timeout value that was exceeded as well as whether
     * refresh and reinstantiation are appropriate responses to this problem.
     * @param msg describes nature of timeout
     * @param timeoutValue timeout value in milliseconds that was exceeded
     * @param refresh true if refresh is an appropriate response
     * @param reinstantiate true if reinstantiation is an appropriate response
     */
    public InternalTimeoutException(String msg, long timeoutValue,
            boolean refresh, boolean reinstantiate) {
        super(msg, refresh, reinstantiate);
        this.timeoutValue = new Long(timeoutValue);
    }
    
    /**
     * Instantiate an InternalTimeoutException conveying a message and underlying 
     * cause and specifying the timeout value that was exceeded as well as whether
     * refresh and reinstantiation are appropriate responses to this problem.
     * @param msg describes nature of timeout
     * @param cause underlying cause
     * @param timeoutValue timeout value in milliseconds that was exceeded
     * @param refresh true if refresh is an appropriate response
     * @param reinstantiate true if reinstantiation is an appropriate response
     */
    public InternalTimeoutException(String msg, Throwable cause, long timeoutValue,
            boolean refresh, boolean reinstantiate) {
        super(msg, cause, refresh, reinstantiate);
        this.timeoutValue = new Long(timeoutValue);
    }

    /**
     * Instantiate an InternalTimeoutException conveying a message and specifying
     * whether refresh and reinstantiation are appropriate responses.
     * @param msg describes nature of timeout problem
     * @param refresh true if refresh is an appropriate response
     * @param reinstantiate true if reinstantiation is an appropriate response
     */
    public InternalTimeoutException(String msg,boolean refresh, boolean reinstantiate) {
        super(msg,refresh,reinstantiate);
    }

    /**
     * Get the timeout value, in milliseconds, that was exceeded.
     * @return the timeout value, or null if none was recorded.
     */
    public Long getTimeoutValue() {
        return this.timeoutValue;
    }

}
