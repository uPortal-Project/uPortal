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
package org.apereo.portal.layout.profile.dao;

import org.apereo.portal.layout.profile.IProfileSelection;

/**
 * DAO API modeling user selection of a profile.
 *
 * <p>Note that this is *not* an API about storing actual Profiles in all their complexity. Rather,
 * this is an API about storing the user preference for a profile referenced by its fname.
 *
 * @since 4.2
 */
public interface IProfileSelectionDao {

    /**
     * Updates a {@link IProfileSelection} or creates a new if one doesn't exist.
     *
     * @param userName non-null username of user who has made the selection
     * @param profileFName fname of the selected profile, or null indicating no selection
     * @return a managed entity {@link IProfileSelection} representing the params
     * @throws IllegalArgumentException if userName is null
     * @throws org.springframework.dao.DataIntegrityViolationException if the user already has a
     *     profile selection.
     */
    public IProfileSelection createOrUpdateProfileSelection(String userName, String profileFName);

    /**
     * Get the {@link IProfileSelection} for the given user, or null if that user has no persisted
     * selection.
     *
     * @param userName non-null username of user who may have made a profile selection
     * @return the profile selection for the given user, or null if no selection
     * @throws IllegalArgumentException if userName is null
     */
    public IProfileSelection readProfileSelectionForUser(String userName);

    /**
     * Updates a {@link IProfileSelection} or creates a new if one doesn't exist.
     *
     * @param profileSelection non-null potentially changed profileSelection to be persisted.
     * @return a managed entity
     * @throws java.lang.IllegalArgumentException if profileSelection is null
     */
    public IProfileSelection createOrUpdateProfileSelection(IProfileSelection profileSelection);

    /**
     * Removes the specified {@link IProfileSelection} from the persistent store.
     *
     * @param profileSelection non-null profile selection to be forgotten
     * @throws java.lang.IllegalArgumentException if profileSelection is null
     */
    public void deleteProfileSelection(IProfileSelection profileSelection);
}
