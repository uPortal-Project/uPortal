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
package org.apereo.portal.security;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import org.apereo.portal.security.provider.PersonImpl;
import org.apereo.portal.security.provider.RestrictedPerson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Responsible for creating {@link IPerson} instances. Historically, the capabilities of this class
 * were accessed through static methods and constants, but with uP5 (and beyond) we need to
 * configure this class through the <code>PropertySourcesPlaceholderConfigurer</code>. At present
 * this class brideges both worlds, but in the future it would be good to move away from static
 * methods.
 *
 * <p>Can create representations of a <i>system</i> as well as <i>guest</i> users.
 *
 * <p>The <i>system</i> user has an ID of 0
 *
 * <p><i>guest</i> users exhibit both of the following characteristics<br>
 *
 * <ol>
 *   <li>User is not (successfully) authenticated with the portal.
 *   <li>Username is included in the list specified by the property <code>
 *       org.apereo.portal.security.PersonFactory.guest_user_names</code>.
 * </ol>
 */
@Component
@Lazy(false) // Force this bean to load in Import/Export via the CLI
public class PersonFactory {

    private static final String SYSTEM_USERNAME = "system";

    private String guestUsernamesProperty = "guest"; // default;  for unit tests

    private static List<String> guestUsernames = null;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init() {
        guestUsernames =
                Collections.unmodifiableList(Arrays.asList(guestUsernamesProperty.split(",")));
        logger.info("Found the following guest usernames:  {}", guestUsernames);
    }

    /**
     * Collection of guest user names specified in portal.properties as <code>
     * org.apereo.portal.security.PersonFactory.guest_user_names</code>. The value of this property
     * is a comma-delimited list.
     *
     * @since 5.0
     */
    public static List<String> getGuestUsernames() {
        if (guestUsernames == null) {
            throw new IllegalStateException(
                    "The guestUsernames collection has not been initialized");
        }
        return guestUsernames;
    }

    /**
     * Creates an empty <code>IPerson</code> implementation.
     *
     * @return an empty <code>IPerson</code> implementation
     */
    public static IPerson createPerson() {
        return new PersonImpl();
    }

    /**
     * Creates a <i>system</i> user.
     *
     * @return a <i>system</i> user
     */
    public static IPerson createSystemPerson() {
        IPerson person = createPerson();
        person.setAttribute(IPerson.USERNAME, SYSTEM_USERNAME);
        person.setID(0);
        return person;
    }

    /**
     * Creates a <i>restricted</i> user.
     *
     * @return <i>restricted</i> user
     */
    public static RestrictedPerson createRestrictedPerson() {
        IPerson person = createPerson();
        return new RestrictedPerson(person);
    }

    /**
     * In addition to supporting the Spring context, this setter allows unit tests to bootstrap this
     * class so that downstream features won't break.
     *
     * @since 5.0
     */
    @Value("${org.apereo.portal.security.PersonFactory.guest_user_names:guest}")
    public void setGuestUsernamesProperty(String guestUsernamesProperty) {
        this.guestUsernamesProperty = guestUsernamesProperty;
    }
}
