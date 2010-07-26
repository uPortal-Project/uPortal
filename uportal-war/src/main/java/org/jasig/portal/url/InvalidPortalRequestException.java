/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
