/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
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
    
    private Long l_timeoutValue=null;

    public InternalTimeoutException() {
    }

    public InternalTimeoutException(String msg) {
        super(msg);
    }

    public InternalTimeoutException(String msg,long timeoutValue) {
        super(msg);
        l_timeoutValue=new Long(timeoutValue);
    }

    public InternalTimeoutException(String msg,long timeoutValue,boolean refresh,boolean reinstantiate) {
        super(msg,refresh, reinstantiate);
        l_timeoutValue=new Long(timeoutValue);
    }

    public InternalTimeoutException(String msg,boolean refresh, boolean reinstantiate) {
        super(msg,refresh,reinstantiate);
    }

    public Long getTimeoutValue() {
        return l_timeoutValue;
    }

}
