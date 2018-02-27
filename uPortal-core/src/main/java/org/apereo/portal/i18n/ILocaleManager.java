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

import java.util.List;
import java.util.Locale;

/**
 * An instance of this interface manages locales on behalf of a user. Locale managers currently keep
 * track of locales at the following levels:<br>
 *
 * <ol>
 *   <li>User's locale preferences (associated with a user ID)
 *   <li>Browser's locale preferences (from the Accept-Language request header)
 *   <li>Session's locale preferences (set via the portal request parameter uP_locales)
 *   <li>Portal's locale preferences (set in portal.properties)
 * </ol>
 *
 * Eventually, they will also keep track of locale preferences at the following levels:<br>
 *
 * <ol>
 *   <li>Layout node's locale preferences
 *   <li>User profile's locale preferences
 * </ol>
 *
 * @since 5.0
 */
public interface ILocaleManager {

    List<Locale> getUserLocales();

    void setUserLocales(List<Locale> userLocales);

    List<Locale> getSessionLocales();

    void setSessionLocales(List<Locale> sessionLocales);
}
