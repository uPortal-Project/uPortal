/*
 Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 distributed with this work for additional information regarding copyright ownership. Apereo
 licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 this file except in compliance with the License. You may obtain a copy of the License at the
 following location:

 <p>http://www.apache.org/licenses/LICENSE-2.0

 <p>Unless required by applicable law or agreed to in writing, software distributed under the
 License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied. See the License for the specific language governing permissions and
 limitations under the License.
*/
package org.apereo.portal.url;

import org.apereo.portal.security.IPerson;

/** Defines a strategy to calculated the user's session timeout. */
public interface IMaxInactiveStrategy {

    /**
     * Determines the max inactive duration in milliseconds based on values obtained through {@code
     * IPerson} for the current user, for example {@code IPermissions}.
     *
     * @param person object representing user that submitted request, used to obtain permissions or
     *     other metadata.
     * @return Integer calculated max inactive duration in milliseconds for this user, null if
     *     system default should be used.
     */
    Integer calcMaxInactive(IPerson person);
}
