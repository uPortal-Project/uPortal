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
package org.apereo.portal.layout;

import java.util.Hashtable;
import javax.sql.DataSource;
import org.apereo.portal.IUserIdentityStore;
import org.apereo.portal.IUserProfile;
import org.apereo.portal.PortalException;
import org.apereo.portal.UserProfile;
import org.apereo.portal.jpa.BasePortalJpaDao;
import org.apereo.portal.layout.dao.IStylesheetDescriptorDao;
import org.apereo.portal.layout.dao.IStylesheetUserPreferencesDao;
import org.apereo.portal.layout.om.IStylesheetDescriptor;
import org.apereo.portal.layout.om.IStylesheetUserPreferences;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.PersonFactory;
import org.apereo.services.persondir.IPersonAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * Helper class for reset-layout based web flows.
 *
 * <p>Depends on uPortal's {@link DataSource}.
 */
public class UserLayoutHelperImpl extends JdbcDaoSupport implements IUserLayoutHelper {

    protected static final String DEFAULT_LAYOUT_FNAME = "default";

    private IUserIdentityStore userIdentityStore;

    private IUserLayoutStore userLayoutStore;

    private IStylesheetUserPreferencesDao stylesheetUserPreferencesDao;

    private IStylesheetDescriptorDao stylesheetDescriptorDao;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public void setUserLayoutStore(IUserLayoutStore userLayoutStore) {
        this.userLayoutStore = userLayoutStore;
    }

    /** @param userIdentityStore the userIdentityStore to set */
    @Autowired
    public void setUserIdentityStore(IUserIdentityStore userIdentityStore) {
        this.userIdentityStore = userIdentityStore;
    }

    @Autowired
    public void setStylesheetUserPreferencesDao(
            IStylesheetUserPreferencesDao stylesheetUserPreferencesDao) {
        this.stylesheetUserPreferencesDao = stylesheetUserPreferencesDao;
    }

    @Autowired
    public void setStylesheetDescriptorDao(IStylesheetDescriptorDao stylesheetDescriptorDao) {
        this.stylesheetDescriptorDao = stylesheetDescriptorDao;
    }

    /**
     * Resets a users layout for all the users profiles
     *
     * @param personAttributes
     */
    @BasePortalJpaDao.PortalTransactional
    public void resetUserLayoutAllProfiles(final IPersonAttributes personAttributes) {
        final IPerson person = PersonFactory.createRestrictedPerson();
        person.setAttributes(personAttributes.getAttributes());
        // get the integer uid into the person object without creating any new person data
        int uid = userIdentityStore.getPortalUID(person, false);
        person.setID(uid);

        final Hashtable<Integer, UserProfile> map = userLayoutStore.getUserProfileList(person);
        for (UserProfile profile : map.values()) {
            resetUserLayoutForProfileByName(person, profile);
            resetStylesheetUserPreferencesForProfile(person, profile);
        }
    }

    /**
     * @param personAttributes
     * @see
     *     org.apereo.portal.layout.IUserLayoutHelper#resetUserLayout(org.apereo.services.persondir.IPersonAttributes)
     */
    @BasePortalJpaDao.PortalTransactional
    @Override
    public void resetUserLayout(final IPersonAttributes personAttributes) {
        // Create an empty RestrictedPerson object
        final IPerson person = PersonFactory.createRestrictedPerson();

        // populate the person with the supplied attributes
        person.setAttributes(personAttributes.getAttributes());

        // get the integer uid into the person object without creating any new person data
        final int uid = userIdentityStore.getPortalUID(person, false);
        person.setID(uid);

        final IUserProfile profile =
                userLayoutStore.getUserProfileByFname(person, DEFAULT_LAYOUT_FNAME);
        resetUserLayoutForProfileByName(person, profile);

        resetStylesheetUserPreferencesForProfile(person, profile);
    }

    private void resetUserLayoutForProfileByName(IPerson person, IUserProfile profile) {

        try {
            // Finally set the layout id to 0.  This orphans the existing layout but it will be
            // replaced by the default
            // when the user logs in
            profile.setLayoutId(0);

            // persist the change
            userLayoutStore.updateUserProfile(person, profile);
            logger.info(
                    "resetUserLayoutForProfileByName complete for person [{}] and profile='{}'",
                    person,
                    profile.getProfileFname());
        } catch (Exception e) {
            logger.error(
                    "Error during resetUserLayoutForProfileByName for person [{}] and profile='{}'",
                    person,
                    profile.getProfileFname(),
                    e);
            throw new PortalException("Error during resetUserLayoutForProfileByName", e);
        }
    }

    private void resetStylesheetUserPreferencesForProfile(IPerson person, IUserProfile profile) {

        try {
            // Structure
            final IStylesheetDescriptor sDescriptor =
                    stylesheetDescriptorDao.getStylesheetDescriptor(
                            profile.getStructureStylesheetId());
            logger.debug(
                    "Obtained the following IStylesheetDescriptor for person [{}] and profile='{}':  [{}]",
                    person,
                    profile.getProfileFname(),
                    sDescriptor);
            final IStylesheetUserPreferences sPreferences =
                    stylesheetUserPreferencesDao.getStylesheetUserPreferences(
                            sDescriptor, person, profile);
            logger.debug(
                    "Obtained the following IStylesheetUserPreferences for descriptor [{}], person [{}], and profile='{}':  [{}]",
                    sDescriptor,
                    person,
                    profile.getProfileFname(),
                    sPreferences);
            if (sPreferences != null) {
                stylesheetUserPreferencesDao.deleteStylesheetUserPreferences(sPreferences);
            }

            // Theme
            final IStylesheetDescriptor tDescriptor =
                    stylesheetDescriptorDao.getStylesheetDescriptor(profile.getThemeStylesheetId());
            logger.debug(
                    "Obtained the following IStylesheetDescriptor for person [{}] and profile='{}':  [{}]",
                    person,
                    profile.getProfileFname(),
                    tDescriptor);
            final IStylesheetUserPreferences tPreferences =
                    stylesheetUserPreferencesDao.getStylesheetUserPreferences(
                            tDescriptor, person, profile);
            logger.debug(
                    "Obtained the following IStylesheetUserPreferences for descriptor [{}], person [{}], and profile='{}':  [{}]",
                    tDescriptor,
                    person,
                    profile.getProfileFname(),
                    tPreferences);
            if (tPreferences != null) {
                stylesheetUserPreferencesDao.deleteStylesheetUserPreferences(tPreferences);
            }

            logger.info(
                    "resetStylesheetUserPreferencesForProfile complete for person [{}] and profile='{}'",
                    person,
                    profile.getProfileFname());
        } catch (Exception e) {
            logger.error(
                    "Error during resetStylesheetUserPreferencesForProfile for person [{}] and profile='{}'",
                    person,
                    profile.getProfileFname(),
                    e);
            throw new PortalException("Error during resetStylesheetUserPreferencesForProfile", e);
        }
    }
}
