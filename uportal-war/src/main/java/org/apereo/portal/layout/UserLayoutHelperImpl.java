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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.IUserIdentityStore;
import org.apereo.portal.IUserProfile;
import org.apereo.portal.PortalException;
import org.apereo.portal.UserProfile;
import org.apereo.portal.security.PersonFactory;
import org.apereo.portal.security.provider.RestrictedPerson;
import org.jasig.services.persondir.IPersonAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * Helper class for reset-layout based web flows.
 *
 * <p>Depends on uPortal's {@link DataSource}.
 */
public class UserLayoutHelperImpl extends JdbcDaoSupport implements IUserLayoutHelper {

    protected static final String DEFAULT_LAYOUT_FNAME = "default";

    protected final Log logger = LogFactory.getLog(this.getClass());
    private IUserIdentityStore userIdentityStore;

    private IUserLayoutStore userLayoutStore;

    @Autowired(required = true)
    public void setUserLayoutStore(IUserLayoutStore userLayoutStore) {
        this.userLayoutStore = userLayoutStore;
    }

    /** @param userIdentityStore the userIdentityStore to set */
    @Autowired
    public void setUserIdentityStore(IUserIdentityStore userIdentityStore) {
        this.userIdentityStore = userIdentityStore;
    }

    /**
     * Resets a users layout for all the users profiles
     *
     * @param personAttributes
     */
    public void resetUserLayoutAllProfiles(final IPersonAttributes personAttributes) {

        RestrictedPerson person = PersonFactory.createRestrictedPerson();
        person.setAttributes(personAttributes.getAttributes());
        // get the integer uid into the person object without creating any new person data
        int uid = userIdentityStore.getPortalUID(person, false);
        person.setID(uid);

        try {
            Hashtable<Integer, UserProfile> userProfileList =
                    userLayoutStore.getUserProfileList(person);
            for (Integer key : userProfileList.keySet()) {
                UserProfile userProfile = userProfileList.get(key);
                userProfile.setLayoutId(0);
                userLayoutStore.updateUserProfile(person, userProfile);
                logger.info(
                        "resetUserLayout complete for " + person + "for profile " + userProfile);
            }
        } catch (Exception e) {
            final String msg = "Exception caught during resetUserLayout for " + person;
            logger.error(msg, e);
            throw new PortalException(msg, e);
        }
        return;
    }

    /**
     * @param personAttributes
     * @see
     *     org.apereo.portal.layout.IUserLayoutHelper#resetUserLayout(org.jasig.services.persondir.IPersonAttributes)
     */
    public void resetUserLayout(final IPersonAttributes personAttributes) {
        // Create an empty RestrictedPerson object
        RestrictedPerson person = PersonFactory.createRestrictedPerson();

        // populate the person with the supplied attributes
        person.setAttributes(personAttributes.getAttributes());

        // get the integer uid into the person object without creating any new person data
        int uid = userIdentityStore.getPortalUID(person, false);
        person.setID(uid);

        try {
            // determine user profile
            IUserProfile userProfile =
                    userLayoutStore.getUserProfileByFname(person, DEFAULT_LAYOUT_FNAME);

            // Finally set the layout id to 0.  This orphans the existing layout but it will be replaced by the default
            // when the user logs in
            userProfile.setLayoutId(0);

            // persist the change
            userLayoutStore.updateUserProfile(person, userProfile);
            logger.info("resetUserLayout complete for " + person);
        } catch (Exception e) {
            final String msg = "Exception caught during resetUserLayout for " + person;
            logger.error(msg, e);
            throw new PortalException(msg, e);
        }
    }
}
