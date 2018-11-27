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
package org.apereo.portal.rest;

import org.apereo.portal.utils.cache.CacheManagementHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Allows a portal admin to clear caches by REST API.
 *
 * @since 5.4
 */
@Controller
public class CacheManagementController {

    public static final String ENDPOINT_URI = "/v5-4/caches";

    @Autowired private CacheManagementHelper cacheManagementHelper;

    @PreAuthorize(
            "hasPermission('ALL', 'java.lang.String', new org.apereo.portal.spring.security.evaluator.AuthorizableActivity('UP_SYSTEM', 'ALL_PERMISSIONS'))")
    @RequestMapping(value = ENDPOINT_URI, method = RequestMethod.DELETE)
    public ResponseEntity clearCache() {
        cacheManagementHelper.clearAllCaches();
        return ResponseEntity.ok("Caches cleared");
    }
}
