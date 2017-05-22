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
package org.apereo.portal.events.tincan.providers;

import org.apereo.portal.events.tincan.om.LrsStatement;

public interface ITinCanAPIProvider {
    /** Do any initialization required for this provider. */
    void init();

    /**
     * Handle an LRS statement.
     *
     * @param statement the statement to handle
     * @return
     */
    boolean sendEvent(LrsStatement statement);

    /** Do any tear-down clean up. Probably unnecessary, but just included for parity w/ init. */
    void destroy();
}
