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

import java.lang.String;

/**
 * @author  Julien Gribonvald
 * @version $Revision$
 */
public interface IAuthUrlCustomizer {

    /**
     * Does this customizer supports the current HTTP request.
     *
     * @param request
     * @return
     */
    boolean supports(HttpServletRequest request, String url);

    /**
     * Customize the supplied external login URL.
     *
     * @param url
     * @return
     */
    String customizeUrl(HttpServletRequest request, String url);
}
