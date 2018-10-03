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
package org.apereo.portal;

import org.apereo.portal.security.IPerson;

/** Interface for managing creation and removal of User Portal Data */
public interface IUserIdentityStore {

    /**
     * Returns a unique uPortal key for a user.
     *
     * @param person the person object
     * @return uPortalUID number
     */
    int getPortalUID(IPerson person) throws AuthorizationException;

    /**
     * Returns a unique uPortal key for a user. A boolean flag determines whether or not to
     * auto-create data for a new user.
     *
     * @param person Person whose portalUID will be returned
     * @param createPortalData Indicates whether to try to create all uPortal data for a new user
     * @return uPortalUID Number or -1 if no user found and unable to create user.
     * @throws AuthorizationException If createPortalData is false and no user is found or if a sql
     *     error is encountered
     */
    int getPortalUID(IPerson person, boolean createPortalData) throws AuthorizationException;

    /**
     * Returns the portal {@link IPerson} corresponding with the specified userName. The <code>
     * createPortalData</code> flag indicates whether the user should be created in the case that
     * s/he doesn't already exist.
     *
     * @param userName The identity of the requested user
     * @param createPortalData Indicates whether to try to create all uPortal data for a new user
     * @return The specified user, or null if the user is not found and createPortalData is false
     */
    IPerson getPerson(String userName, boolean createPortalData) throws AuthorizationException;

    void removePortalUID(String userName) throws Exception;

    void removePortalUID(int uPortalUID) throws Exception;

    /**
     * Gets a portal user name that is associated with the specified portal ID.
     *
     * @param uPortalUID The portal ID to find a user name for.
     * @return The user name associated with the specified portal id, null if one isn't found.
     */
    String getPortalUserName(int uPortalUID);

    /** Gets a portal user id that is associated with the specified portal user name */
    Integer getPortalUserId(String userName);

    /** Returns true if the specified String satisfies all criteria for a valid username. */
    boolean validateUsername(String username);
}
