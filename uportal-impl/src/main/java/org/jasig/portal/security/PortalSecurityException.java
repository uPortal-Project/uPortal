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

package org.jasig.portal.security;

import org.jasig.portal.PortalException;

/**
 * <p>An exception representing a Portal security problem.</p>
 *
 * @author Andrew Newman, newman-andy@yale.edu
 * @version $Revision$ $Date$
 */
public class PortalSecurityException extends PortalException {
    
    
    /**
     * Instantiate a PortalSecurityException with the given cause.
     * @param cause Throwable that caused the error condition
     */
    public PortalSecurityException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiate a PortalSecurityException with the given message.
     * @param message information about the error condition
     */
    public PortalSecurityException(String message) {
        super(message);
    }

    /**
     * Instantiate a PortalSecurityException with the given message and 
     * underlying cause
     * @param message information about the error condition
     * @param cause underlying cause of error condition
     */
    public PortalSecurityException(String message, Throwable cause) {
        super(message, cause);
    }
}