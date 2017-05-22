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
package org.apereo.portal.layout.profile;

import org.apereo.portal.jpa.BasePortalJpaDao;
import org.apereo.portal.layout.profile.dao.IProfileSelectionDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

/**
 * Trivial implementation of profile selection registry API that relies upon an
 * IProfileSelectionDao.
 *
 * <p>Translates between the very simple selections-are-just-plain-old-Strings Registry API and the
 * selections-are-objects DAO API.
 *
 * @since 4.2
 */
public class ProfileSelectionRegistry implements IProfileSelectionRegistry {

    // autowired
    private IProfileSelectionDao profileSelectionDao;

    @Override
    public String profileSelectionForUser(final String username) {

        Assert.notNull(username, "Cannot look up the profile selection for a null username.");

        final IProfileSelection profileSelection =
                this.profileSelectionDao.readProfileSelectionForUser(username);

        if (null == profileSelection) {
            return null;
        }

        return profileSelection.getProfileFName();
    }

    @Override
    @BasePortalJpaDao.PortalTransactional
    public void registerUserProfileSelection(final String userName, final String profileFName) {

        Assert.notNull(userName, "Cannot register a profile selection for a null username.");

        final IProfileSelection existingSelection =
                this.profileSelectionDao.readProfileSelectionForUser(userName);

        if (null == profileFName) {
            // null profileFName translates to deleting existing Selection if any

            if (null != existingSelection) {
                this.profileSelectionDao.deleteProfileSelection(existingSelection);
            }

        } else {
            // non-null profileFName translates to creating or updating a Selection.

            if (null == existingSelection) {
                this.profileSelectionDao.createOrUpdateProfileSelection(userName, profileFName);
            } else {
                existingSelection.setProfileFName(profileFName);
                this.profileSelectionDao.createOrUpdateProfileSelection(existingSelection);
            }
        }
    }

    @Autowired
    public void setProfileSelectionDao(final IProfileSelectionDao profileSelectionDao) {
        this.profileSelectionDao = profileSelectionDao;
    }
}
