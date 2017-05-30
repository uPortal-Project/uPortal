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
package org.apereo.portal.url.processing;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Provides APIs a class can implement if it wishes to be part of the request parameter processing
 * chain. Implementations can read and write request parameters before any other part of the
 * framework deals with the request.
 *
 */
public interface IRequestParameterProcessor {

    /**
     * Analyze current request, process necessary URL parameters, delivering information to the
     * appropriate components. This method can also add, modify and remove parameters on the
     * request. If the request is not yet in a state where it can be completely processed this
     * method may return <code>false</code> so that it can be called again after other processors
     * have been allowed to execute. Even if <code>false</code> is returned as much processing as
     * possible should happen for each pass.
     *
     * @param request - incoming request
     * @param response - outgoing response
     * @returns <code>true</code> if processing is complete, <code>false</code> if processing is not
     *     complete and this processor should be called again after all other processors have been
     *     executed.
     * @throws IllegalArgumentException If req or res are null.
     */
    boolean processParameters(HttpServletRequest request, HttpServletResponse response);
}
