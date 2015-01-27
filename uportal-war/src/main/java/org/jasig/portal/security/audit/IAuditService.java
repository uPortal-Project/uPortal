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

package org.jasig.portal.security.audit;

import org.joda.time.ReadableInstant;

/**
 * Service for auditing the exercise of portal functions.
 *
 * Service layer in a service-registry-dao-jpa architecture.
 *
 * @since uPortal 4.2
 */
public interface IAuditService {

    /**
     * Record that a particular user logged in at a particular time.
     * @param username a non-null username
     * @param momentOfLogin a non-null ReadableInstant at which that user logged in
     *
     * @throws java.lang.IllegalArgumentException if momentOfLogin is in the future
     */
    public void recordLogin(String username, ReadableInstant momentOfLogin);

}
