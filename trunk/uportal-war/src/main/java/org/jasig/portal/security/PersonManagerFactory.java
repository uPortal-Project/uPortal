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

package org.jasig.portal.security;

import org.jasig.portal.spring.locator.PersonManagerLocator;

/**
 * @author Bernie Durfee (bdurfee@interactivebusiness.com)
 * @deprecated The 'personManager' bean in the spring context should be used instead
 */
@Deprecated
public class PersonManagerFactory {

    /**
     * Returns an instance of the IPersonManager specified in portal.properties
     * @return instance of the IPersonManager
     */
    public static IPersonManager getPersonManagerInstance() {
        return PersonManagerLocator.getPersonManager();
    }
}
