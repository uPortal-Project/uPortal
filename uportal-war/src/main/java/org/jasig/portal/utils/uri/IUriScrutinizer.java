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

package org.jasig.portal.utils.uri;

import java.net.URI;

/**
 * UriScrutinizers examine URIs to see if they should be blocked for reasons
 * of policy.
 * @since uPortal 2.5.1
 */
public interface IUriScrutinizer {

    /**
     * Scrutinize a URI to determine if access to it should be blocked for
     * reasons of policy. Throws BlockedUriException if access to the URI
     * should be blocked, conveying the reason for blockage.  
     * 
     * Blocking a URI is an exceptional and ideally rare circumstance
     * which will usually abort whatever operation was being undertaken, and so
     * this method throws on that exceptional circumstance.
     * @param uri non-null URI for examination
     * @throws BlockedUriException if access should be blocked
     */
    public void scrutinize(URI uri) throws BlockedUriException;
    
}
