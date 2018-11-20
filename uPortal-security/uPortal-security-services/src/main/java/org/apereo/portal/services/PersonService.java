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
package org.apereo.portal.services;

import javax.annotation.Resource;
import org.apereo.portal.IUserIdentityStore;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.provider.PersonImpl;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Provides subsystems outside of authentication and user attributes a simplified API for
 * interacting with {@link IPerson} objects.
 *
 * @since 5.4
 */
@Service
public class PersonService {

    @Autowired private IUserIdentityStore userIdentityStore;

    @Resource(name = "personAttributeDao")
    private IPersonAttributeDao personAttributeDao;

    /** Obtain the fully-constructed {@link IPerson} associated witth the specified username. */
    public IPerson getPerson(String username) {
        final IPerson rslt = new PersonImpl();
        rslt.setAttribute(IPerson.USERNAME, username);
        rslt.setID(userIdentityStore.getPortalUserId(username));
        rslt.setAttributes(personAttributeDao.getPerson(username).getAttributes());
        return rslt;
    }
}
