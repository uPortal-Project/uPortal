/*
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
package org.apereo.portal.security.provider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.PortalSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Manages the storage of an IPerson object in a user's session. */
public class SimplePersonManager extends AbstractPersonManager {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Retrieve an IPerson object for the incoming request
     *
     * @param request the servlet request object
     * @return the IPerson object for the incoming request
     */
    @Override
    public IPerson getPerson(HttpServletRequest request) throws PortalSecurityException {
        HttpSession session = request.getSession(false);
        IPerson person = null;

        // Return the person object if it exists in the user's session
        if (session != null) {
            person = (IPerson) session.getAttribute(PERSON_SESSION_KEY);
            logger.debug("getPerson -- person object retrieved from session is [{}]", person);
        }

        if (person == null) {
            try {
                // Create a guest person
                person = createGuestPerson(request);
                logger.debug("getPerson -- created a new guest person [{}]", person);
            } catch (Exception e) {
                // Log the exception
                logger.error("Exception creating guest person.", e);
            }
            // Add this person object to the user's session
            if (person != null && session != null) {
                session.setAttribute(PERSON_SESSION_KEY, person);
            }
        }

        return person;
    }
}
