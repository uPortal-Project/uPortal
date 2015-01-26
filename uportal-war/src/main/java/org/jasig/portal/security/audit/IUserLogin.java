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
 * IUserLogin represents a moment in time at which a specific user logged in.
 * IUserLogin instances are immutable.
 * @since uPortal 4.2
 */
public interface IUserLogin {

    /**
     * Get the user identifier.  A username.  Something like a NetID.
     * @return a non-null username.
     */
    public String getUserIdentifier();

    /**
     * Get the instant at which the login happened.
     * @return a non-null Instant.
     */
    public ReadableInstant getInstant();

}
