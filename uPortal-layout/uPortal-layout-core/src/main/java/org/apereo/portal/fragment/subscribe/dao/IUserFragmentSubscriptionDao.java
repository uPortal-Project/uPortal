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
package org.apereo.portal.fragment.subscribe.dao;

import java.util.List;
import org.apereo.portal.fragment.subscribe.IUserFragmentSubscription;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.security.IPerson;

/**
 * Interface for retrieving information about fragments (pre-formatted tabs) to which a user has
 * subscribed.
 *
 */
public interface IUserFragmentSubscriptionDao {

    /**
     * Creates, initializes and persists a new {@link IUserFragmentSubscription} based on the
     * specified person, and fragment owner. The subscription will always be created as active.
     *
     * @param person the logged in person.
     * @param fragmentOwner the person object representing the fragment owner
     * @return A newly created, initialized and persisted {@link IUserFragmentSubscription}
     * @throws org.springframework.dao.DataIntegrityViolationException If a definition already
     *     exists for the specified channelPublishId
     */
    public IUserFragmentSubscription createUserFragmentInfo(IPerson person, IPerson fragmentOwner);

    /**
     * Persists changes to a {@link IPortletDefinition}.
     *
     * @param IUserFragmentSubscription The user fragment subscription to store the changes for
     * @throws IllegalArgumentException if portletDefinition is null.
     */
    public void updateUserFragmentInfo(IUserFragmentSubscription userFragmentSubscription);

    /**
     * Get a {@link IUserFragmentSubscription} for the specified {@link IPerson}.
     *
     * @param person the logged in person whose subscriptions will be returned.
     * @return an array of user fragment subscriptions.
     * @throws IllegalArgumentException if portletDefinitionId is null.
     */
    public List<IUserFragmentSubscription> getUserFragmentInfo(IPerson person);

    /**
     * Get a {@link IUserFragmentSubscription} for the specified person and fragment owner.
     *
     * @param person the logged in person.
     * @param fragmentOwner the person object representing the fragment owner
     * @return The user fragment subscription for the person and fragment owner, null if no
     *     definition exists for the id.
     */
    public IUserFragmentSubscription getUserFragmentInfo(IPerson person, IPerson fragmentOwner);

    /**
     * Get a {@link IUserFragmentSubscription} for the specified user fragment info id.
     *
     * @param userFragmentInfoId the user fragment info id.
     * @return The user fragment subscription for the userFragmentInfoId, null if no definition
     *     exists for the id.
     */
    public IUserFragmentSubscription getUserFragmentInfo(long userFragmentInfoId);

    /**
     * Removes the specified {@link IPortletDefinition} from the persistent store.
     *
     * @param userFragmentSubscription The definition to deactivate.
     * @throws IllegalArgumentException if portletDefinition is null.
     */
    public void deleteUserFragmentInfo(IUserFragmentSubscription userFragmentSubscription);

    /** @return A list of all of the usernames who have active tab subscriptions */
    public List<String> getAllUsersWithActiveSubscriptions();
}
