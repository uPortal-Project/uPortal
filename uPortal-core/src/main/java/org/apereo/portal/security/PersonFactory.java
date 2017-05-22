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
import org.apereo.portal.properties.PropertiesManager;
import org.apereo.portal.security.provider.PersonImpl;
import org.apereo.portal.security.provider.RestrictedPerson;

/**
 * Creates a person.
 *
 * <p>Can create representations of a <i>system</i> user and a <i>guest</i> user.
 *
 * <p><i>system</i> users have an ID of 0
 *
 * <p><i>guest</i> users have both of the following characteristics<br>
 *
 * <ol>
 *   <li>User is not successfully authenticated with the portal.
 *   <li>User name matches the value of the property <code>
 *       org.apereo.portal.security.PersonFactory.guest_user_name</code> in <code>portal.properties
 *       </code>.
 * </ol>
 *
 */
public class PersonFactory {

    private static final String GUEST_USERNAMES_PROPERTY =
            PropertiesManager.getProperty(
                    "org.apereo.portal.security.PersonFactory.guest_user_names", "guest");

    /**
     * Collection of guest user names specified in portal.properties as <code>
     * org.apereo.portal.security.PersonFactory.guest_user_names</code>. The value of this property
     * is a comma-delimited list.
     *
     * @since 5.0
     */
    public static final List<String> GUEST_USERNAMES =
            Collections.unmodifiableList(Arrays.asList(GUEST_USERNAMES_PROPERTY.split(",")));

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
        person.setAttribute(IPerson.USERNAME, "SYSTEM_USER");
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
}
