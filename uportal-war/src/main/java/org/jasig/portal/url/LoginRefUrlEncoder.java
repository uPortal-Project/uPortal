/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.url;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

/**
 * Encode the originally requested URL for use as a RefUrl parameter in various login use cases (internal, external CAS, etc.).
 * 
 * @author James Wennmacher, jwennmacher@unicon.net
 */
public interface LoginRefUrlEncoder {

    /**
     * Encodes an URL that includes a login URL and a reference to the originally-requested URL for use in a browser
     * redirect.
     * @param request HTTP Request
     * @param loginUrl Login URL
     * @return encoded redirect string
     * @throws UnsupportedEncodingException
     */
    String encodeLoginAndRefUrl(HttpServletRequest request) throws UnsupportedEncodingException;

}
