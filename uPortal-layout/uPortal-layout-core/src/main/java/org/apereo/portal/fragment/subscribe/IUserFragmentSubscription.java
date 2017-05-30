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
package org.apereo.portal.fragment.subscribe;

import org.apereo.portal.IBasicEntity;

/**
 * IUserFragmentSubscription represents a fragment subscription for an individual user.
 * Subscriptions are used to track preferences for pulled DLM fragments.
 */
public interface IUserFragmentSubscription extends IBasicEntity {

    /**
     * Get the internal unique user ID for the end user with which this subscription is associated.
     *
     * @return
     */
    public int getUserId();

    /**
     * Get the unique string username of the owner of the subscribed-to fragment.
     *
     * @return
     */
    public String getFragmentOwner();

    /**
     * Return <code>true</code> if this fragment subscription is active, <code>false</code> if
     * inactive/deleted.
     *
     * @return
     */
    public boolean isActive();

    /** Mark this fragment as inactive/deleted. */
    public void setInactive();

    /**
     * Set a flag indicating whether this fragment is currently active.
     *
     * @param active
     */
    public void setActive(boolean active);

    /**
     * Get the unique ID associated with this subscription.
     *
     * @return
     */
    public long getId();
}
