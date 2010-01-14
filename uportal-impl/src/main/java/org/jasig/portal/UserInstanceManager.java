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

package org.jasig.portal;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.spring.locator.UserInstanceManagerLocator;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;

/**
 * @deprecated Use {@link org.jasig.portal.user.IUserInstanceManager} from the Spring Application Context instead.
 */
public class UserInstanceManager {

    /**
     * @deprecated Use {@link org.jasig.portal.user.IUserInstanceManager#getUserInstance(HttpServletRequest)} instead.
     */
    public static IUserInstance getUserInstance(HttpServletRequest request) throws PortalException {
        final IUserInstanceManager userInstanceManager = UserInstanceManagerLocator.getUserInstanceManager();
        return userInstanceManager.getUserInstance(request);
    }
}
