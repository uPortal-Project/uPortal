/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
/**
 * 
 */
package org.jasig.portal.url;

import org.jasig.portal.ErrorID;
import org.jasig.portal.PortalException;

/**
 * May be thrown if the request URL does not adhere to the expected syntax.
 * 
 * @author Nicholas Blair, nblair@doit.wisc.edu
 *
 */
public class InvalidPortalRequestException extends PortalException {

    /**
     * 
     */
    private static final long serialVersionUID = 53706L;

    /**
     * 
     */
    public InvalidPortalRequestException() {
    }

    /**
     * @param cause
     */
    public InvalidPortalRequestException(Throwable cause) {
        super(cause);
    }

    /**
     * @param msg
     */
    public InvalidPortalRequestException(String msg) {
        super(msg);
    }

    /**
     * @param errorid
     */
    public InvalidPortalRequestException(ErrorID errorid) {
        super(errorid);
    }

    /**
     * @param msg
     * @param cause
     */
    public InvalidPortalRequestException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * @param errorid
     * @param cause
     */
    public InvalidPortalRequestException(ErrorID errorid, Throwable cause) {
        super(errorid, cause);
    }

    /**
     * @param msg
     * @param refresh
     * @param reinstantiate
     */
    public InvalidPortalRequestException(String msg, boolean refresh,
            boolean reinstantiate) {
        super(msg, refresh, reinstantiate);
    }

    /**
     * @param msg
     * @param cause
     * @param refresh
     * @param reinstantiate
     */
    public InvalidPortalRequestException(String msg, Throwable cause,
            boolean refresh, boolean reinstantiate) {
        super(msg, cause, refresh, reinstantiate);
    }

}
