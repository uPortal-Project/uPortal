/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.layout;

import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.IUserProfile;
import org.jasig.portal.UserProfile;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.provider.RestrictedPerson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This concrete implementation of {@link IProfileMapper} decorates one or more enclosed profile mapper instances.  It
 * will invoke the enclosed mappers in the order specified.  The first mapper that returns a value is the "winner" --
 * subsequent mappers are not checked.  This implementation also supports a default profile fname;  If none of the
 * enclosed mappers returns a value, the specified default will be returned.  Lastly, this implementation supports a
 * stickySelection feature:  if enabled (it's off by default), a user's choice of a specific profile will be remembered
 * until s/he makes another specific choice.  In other words, choosing a non-default profile makes it your own, personal
 * default from there forward.
 *
 * @author Jen Bourey, jennifer.bourey@gmail.com
 */
public final class ChainingProfileMapperImpl implements IProfileMapper {

    private static final String STICKY_PROFILE_FNAME = "ChainingProfileMapperImpl.STICKY_PROFILE";
    private static final String STICKY_PROFILE_DESCRIPTION =
            "StickyProfile feature;  system-generated profile that points to another profile";
    private static final UserProfile STICKY_USER_PROFILE_TEMPLATE = new UserProfile(
            -1,                         // id;  assigned a generated value when the profile is persisted
            STICKY_PROFILE_FNAME,       // fname
            null,                       // name;  set before saving, points to the profile that should be "sticky"
            STICKY_PROFILE_DESCRIPTION, // description
            0,                          // layout_id;  not used
            0,                          // struct_ss;  not used
            0);                         // theme_ss;  not used

    protected Logger logger = LoggerFactory.getLogger(getClass());

    private String defaultProfileName = "default";
    private boolean stickySelection = false;  // default
    private List<IProfileMapper> subMappers = Collections.<IProfileMapper>emptyList();

    @Autowired
    private IUserLayoutStore layoutStore;

    public void setDefaultProfileName(String defaultProfileName) {
        this.defaultProfileName = defaultProfileName;
    }

    /**
     * If this bean property is set to TRUE, whenever a non-default profile is selected by the enclosed mappers that
     * profile will be remembered until a new choice is made.  The default is FALSE.
     */
    public void setStickySelection(boolean stickySelection) {
        this.stickySelection = stickySelection;
    }

    public void setSubMappers(List<IProfileMapper> subMappers) {
        // Defensive copy
        this.subMappers = Collections.unmodifiableList(subMappers);
    }

    @Override
    public String getProfileFname(IPerson person, HttpServletRequest request) {

        String rslt = null;  // indicates profile has not yet been set

        logger.trace("Choosing profile for user '{}';  stickySelection={}", person.getUserName(), stickySelection);

        for (IProfileMapper mapper : subMappers) {
            final String fname = mapper.getProfileFname(person, request);
            if (fname != null) {
                logger.debug("Profile mapper {} found profile fname={}", mapper, fname);
                rslt =  fname;
                break; // First mapper with an answer wins
            }
        }

        /*
         * This section implements th (optional) sticky behavior.  Three criteria are required to trigger it:
         *   - stickySelection must be enabled (bean property)
         *   - The user must not be a guest
         *   - The IPerson object must not be an instance of RestrictedPerson (we only want the AuthN-time IPerson)
         */
        if (stickySelection && !person.isGuest() && !(person instanceof RestrictedPerson)) {

            // We need the current sticky profile;  obtain it from getUserProfileList (rather than getUserProfileByFname)
            // because that method will attempt to fall back on a system profile in the absence of a user-owned profile;
            // we don't want that.
            final Hashtable<Integer, UserProfile> profiles = layoutStore.getUserProfileList(person);
            logger.debug("Profiles for user '{}' are:  {}", person.getUserName(), profiles);
            final UserProfile stickyProfile = findStickyProfile(profiles);
            logger.debug("stickyProfile for user '{}' is:  {}", person.getUserName(), stickyProfile);

            // Was a profile identified in the chain?
            if (rslt != null) {
                // Yes;  Do we need to do anything?  There are 3 possibilities:
                if (defaultProfileName.equals(rslt) && stickyProfile != null) {
                    // There is an existing stickyProfile we need to clear...
                    logger.debug("Clearing sticky profile '{}' for user '{}'", stickyProfile.getProfileName(), person.getUserName());
                    clearStickyProfile(person, stickyProfile);
                } else if (!defaultProfileName.equals(rslt) && stickyProfile == null) {
                    // Currently there is no stickyProfile and we need to create one...
                    logger.debug("Setting sticky profile '{}' for user '{}'", rslt, person.getUserName());
                    setStickyProfile(person, rslt);
                } else if (!defaultProfileName.equals(rslt) && !stickyProfile.getProfileName().equals(rslt)) {
                    // There is an existing stickyProfile, but it needs to be changed...
                    logger.debug("Updating sticky profile '{}' for user '{}'", rslt, person.getUserName());
                    updateStickyProfile(person, stickyProfile, rslt);
                }
                // Otherwise make no update;  nothing has changed
            }

            // Do we need to apply the stickyProfile?
            if (rslt == null && stickyProfile != null) {
                logger.debug("Applying sticky profile '{}' for user '{}'", stickyProfile.getProfileName(), person.getUserName());
                rslt = stickyProfile.getProfileName();
            }

        }

        // Sanity check to be certain we don't return a "broken" profile
        if (STICKY_PROFILE_FNAME.equals(rslt)) {
            logger.warn("Selected synthetic (pointer) profile for user '{}';  falling back to default of '{}'",
                                                                person.getUserName(), defaultProfileName);
            rslt = defaultProfileName;
        }

        // If we reach this point w/o selecting something else, we apply the default
        return rslt != null ? rslt : defaultProfileName;

    }

    /*
     * Private stuff
     */

    /**
     * Returns the current sticky profile, if present in the collection, or null.
     *
     * @param profiles
     * @return
     */
    private UserProfile findStickyProfile(Hashtable<Integer, UserProfile> profiles) {
        UserProfile rslt = null;  // default
        for (UserProfile p : profiles.values()) {
            if (STICKY_PROFILE_FNAME.equals(p.getProfileFname())) {
                rslt = p;
                break;
            }
        }
        return rslt;
    }

    private void clearStickyProfile(IPerson person, UserProfile stickyProfile) {
        layoutStore.deleteUserProfile(person, stickyProfile.getProfileId());
    }

    private void setStickyProfile(IPerson person, String profileName) {
        final IUserProfile stickyProfile = new UserProfile(STICKY_USER_PROFILE_TEMPLATE);
        stickyProfile.setProfileName(profileName);
        layoutStore.addUserProfile(person, stickyProfile);
    }

    private void updateStickyProfile(IPerson person, UserProfile stickyProfile, String profileName) {
        stickyProfile.setProfileName(profileName);
        layoutStore.updateUserProfile(person, stickyProfile);
    }

}
