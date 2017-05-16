/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal;


/**
 * This exception informs uPortal that an authorization violation has occured within a channel.
 *
 */
public class AuthorizationException extends PortalException {
    private static final long serialVersionUID = 1L;

    /**
     * Instantiate an AuthorizationException with the given cause.
     *
     * @param cause defines the causing Throwable of the authorization violation.
     */
    public AuthorizationException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiate an AuthorizationException with the given explanatory message.
     *
     * @param msg explains the nature of the attempted authorization violation.
     */
    public AuthorizationException(String msg) {
        super(msg);
    }

    /**
     * Instantiate an AuthorizationException with an explanatory message and an underlying Throwable
     * cause.
     *
     * @param msg explains the nature of the attempted authorization violation.
     * @param cause - an underlying cause of this Exception.
     */
    public AuthorizationException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Instantiate an AuthorizationException with an explanatory message and indicating whether
     * channel refresh and channel reinstantiation are appropriate responses.
     *
     * @param msg - explanatory message
     * @param refresh - true if refresh is appropriate response
     * @param reinstantiate - true if reinstantiate is appropriate response
     */
    public AuthorizationException(String msg, boolean refresh, boolean reinstantiate) {
        super(msg, refresh, reinstantiate);
    }
}
