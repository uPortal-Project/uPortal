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
package org.apereo.portal.persondir;

import java.util.List;
import java.util.Set;

/**
 * ILocalAccountDao provides an interface for interacting with the uPortal internal account store.
 * This interface works with accounts in a local format rather than using the person directory API.
 *
 */
public interface ILocalAccountDao {

    /**
     * Update or create a local uPortal account.
     *
     * @param account
     * @return
     */
    public ILocalAccountPerson updateAccount(ILocalAccountPerson account);

    /**
     * Return an individual uPortal account matching the specified unique ID.
     *
     * @param id
     * @return
     */
    public ILocalAccountPerson getPerson(long id);

    /**
     * Return an individual uPortal account matching the specified unique username.
     *
     * @param username
     * @return
     */
    public ILocalAccountPerson getPerson(String username);

    /** Create a new local account for the specified username */
    public ILocalAccountPerson createPerson(String username);

    /**
     * Return a list of all local portal accounts.
     *
     * @return
     */
    public List<ILocalAccountPerson> getAllAccounts();

    /** @param account */
    public void deleteAccount(ILocalAccountPerson account);

    /**
     * Return a list of all local portal accounts matching the specified query.
     *
     * @param query
     * @return
     */
    public List<ILocalAccountPerson> getPeople(LocalAccountQuery query);

    /**
     * Return a set of the attribute names currently in-use in the local account store.
     *
     * @return
     */
    public Set<String> getCurrentAttributeNames();
}
