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

package org.jasig.portal.url;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Wrapper for all portal responses
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortalHttpServletResponseWrapper extends HttpServletResponseWrapper {
    private final Object urlEncodingMutex = new Object();

    public PortalHttpServletResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    /*
     * encoding URLs is not thread-safe in Tomcat, sync around url encoding
     */

    @Override
    public String encodeRedirectUrl(String url) {
        synchronized (this.urlEncodingMutex) {
            return super.encodeRedirectUrl(url);
        }
    }

    @Override
    public String encodeRedirectURL(String url) {
        synchronized (this.urlEncodingMutex) {
            return super.encodeRedirectURL(url);
        }
    }

    @Override
    public String encodeUrl(String url) {
        synchronized (this.urlEncodingMutex) {
            return super.encodeUrl(url);
        }
    }

    @Override
    public String encodeURL(String url) {
        synchronized (this.urlEncodingMutex) {
            return super.encodeURL(url);
        }
    }
}
