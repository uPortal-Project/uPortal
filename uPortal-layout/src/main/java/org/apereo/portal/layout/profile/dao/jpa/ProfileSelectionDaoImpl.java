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
package org.apereo.portal.layout.profile.dao.jpa;

import org.apache.commons.lang3.Validate;
import org.apereo.portal.jpa.BasePortalJpaDao;
import org.apereo.portal.jpa.OpenEntityManager;
import org.apereo.portal.layout.profile.IProfileSelection;
import org.apereo.portal.layout.profile.dao.IProfileSelectionDao;

/**
 * JPA implementation of IProfilePreferenceDao.
 *
 * @since 4.2
 */
// deliberately not annotated as Repository to avoid annotation-driven instantiation,
// instead adopters wishing to store profile selections via JPA can explicitly configure.
public class ProfileSelectionDaoImpl extends BasePortalJpaDao implements IProfileSelectionDao {

    @Override
    @PortalTransactional
    public IProfileSelection createOrUpdateProfileSelection(
            final String userName, final String profileFName) {
        Validate.notEmpty(userName, "Cannot create a profile selection for an empty userName");
        Validate.notEmpty(
                profileFName,
                "Cannot create profile selection with empty profile fname "
                        + "(instead delete any selection for this user.)");

        final ProfileSelection jpaProfileSelection = new ProfileSelection(userName, profileFName);
        return this.createOrUpdateProfileSelection(jpaProfileSelection);
    }

    @Override
    @PortalTransactional
    public IProfileSelection createOrUpdateProfileSelection(
            final IProfileSelection profileSelection) {

        Validate.notNull(profileSelection);

        if (!getEntityManager().contains(profileSelection)) {
            //Entity is not managed
            return getEntityManager().merge(profileSelection);
        } else {
            getEntityManager().persist(profileSelection);
            return profileSelection;
        }
    }

    @Override
    @PortalTransactional
    public void deleteProfileSelection(final IProfileSelection profileSelection) {

        Validate.notNull(profileSelection, "Cannot delete a null profileSelection.");

        final IProfileSelection persistentProfileSelection;

        if (getEntityManager().contains(profileSelection)) {
            persistentProfileSelection = profileSelection;
        } else {
            persistentProfileSelection = getEntityManager().merge(profileSelection);
        }

        getEntityManager().remove(persistentProfileSelection);
    }

    @Override
    @OpenEntityManager(unitName = PERSISTENCE_UNIT_NAME)
    public IProfileSelection readProfileSelectionForUser(final String userName) {

        final NaturalIdQuery<ProfileSelection> query = createNaturalIdQuery(ProfileSelection.class);
        query.using(ProfileSelection_.userName, userName);

        return query.load();
    }
}
