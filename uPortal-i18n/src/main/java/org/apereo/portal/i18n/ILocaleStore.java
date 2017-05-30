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
package org.apereo.portal.i18n;

import java.util.Locale;
import org.apereo.portal.security.IPerson;

/**
 * Interface defining how the portal reads and writes locale preferences.
 *
 */
public interface ILocaleStore {

    /**
     * Retrieves the locale preferences for a particular user.
     *
     * @param person the user
     * @return the user's locale preferences
     * @throws Exception
     */
    public Locale[] getUserLocales(IPerson person);

    /**
     * Persists the locale preferences for a particular user.
     *
     * @param person the user
     * @param locales the user's new locale preferences
     * @throws Exception
     */
    public void updateUserLocales(IPerson person, Locale[] locales);
}
