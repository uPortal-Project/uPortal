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
package org.jasig.portal.portlets.account;


import java.net.URL;
import java.util.Locale;

import org.jasig.portal.persondir.ILocalAccountPerson;


/**
 * Notification interface.  Implementations should notify users that their local
 * uportal user account has had a password reset token assigned to it and pass
 * along the URL that can be used to reset their password.
 */
public interface IPasswordResetNotification {
    /**
     * Notify the user of the password reset request.
     *
     * @param resetUrl  URL to use to reset the users password
     * @param account The account associated with the URL
     * @param locale the locale of the user making the reset request
     */
    void sendNotification(URL resetUrl, ILocalAccountPerson account, Locale locale);
}
