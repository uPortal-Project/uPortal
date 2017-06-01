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

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.Validate;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IdentitySwapperManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationListener;

/**
 * Profile mapper implementing sticky profile selection.
 *
 * <p>Stores selected profile fname (as looked up in injected Map keyed by profile key) into an
 * injected IProfileSelectionRegistry, but does not store under impersonation.
 *
 * <p>Translates a configured profile key (default: "default") to mean apathy, which is to say
 * clearing any stored profile selection. This allows users selecting the default profile key (as
 * "default") to have no stored selection at all and thereby get the default profile selection
 * behavior, where just what that default selection behavior in the rest of the profile mapping
 * chain might change over time. This apathy translation takes priority over key->fname mapping --
 * if a key is both a key in the map and is the key configured to mean apathy, it is treated as
 * meaning apathy.
 *
 * <p>Reflects stored profile selection from the IProfileSelectionRegistry.
 *
 * <p>DOES NOT HAVE A DEFAULT PROFILE FNAME TO RETURN. If this mapper doesn't have anything
 * interesting to say about what user profile a user ought to have, it simply returns null, which is
 * the API defined way to say nothing.
 *
 * <p>Subsequent or upstream profile mappers can fall back on a default, see also
 * ChainingProfileMapperImpl.
 *
 * <p>Fails gracefully. If persisted profile selection if any cannot be determined, logs and returns
 * null indicating no available opinion about desired profile mapping.
 *
 * <p>Typically this mapper will be in the profile mapping chain within a ChainingProfileMapperImpl.
 *
 * @since 4.2
 */
public class StickyProfileMapperImpl
        implements IProfileMapper, ApplicationListener<ProfileSelectionEvent> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    // autowired
    private IProfileSelectionRegistry profileSelectionRegistry;

    // dependency injected as "mappings", required.
    private Map<String, String> immutableMappings;

    // autowired
    private IdentitySwapperManager identitySwapperManager;

    // dependency injected, optional, may be null.
    private String profileKeyForNoSelection = "default";

    /**
     * Log a warning when configured such that a profile key both means apathy and means a
     * particular profile fname.
     */
    @PostConstruct
    public void warnOnApathyKeyInMappings() {
        if (null != profileKeyForNoSelection
                && immutableMappings.containsKey(profileKeyForNoSelection)) {
            logger.warn(
                    "Configured to treat profile key {} as apathy, "
                            + "yet also configured to map that key to profile  fname {}.  Apathy wins.  "
                            + "This is likely just fine, but it might be a misconfiguration.",
                    profileKeyForNoSelection,
                    immutableMappings.get(profileKeyForNoSelection));
        }
    }

    @Override
    public void onApplicationEvent(ProfileSelectionEvent event) {

        try {

            final String userName = event.getPerson().getUserName();

            if (event.getPerson().isGuest()) {
                logger.warn(
                        "Ignoring a profile selection event fired by guest user. "
                                + "Should the Guest user be firing profile selections in your uPortal "
                                + "implementation?  Likely not.");
                return;
            }

            if (identitySwapperManager.isImpersonating(event.getRequest())) {
                logger.debug(
                        "Ignoring selection of profile by key {} in the context of user {} because impersonated.",
                        event.getRequestedProfileKey(),
                        userName);
                return;
            }

            if (profileKeyForNoSelection != null
                    && profileKeyForNoSelection.equals(event.getRequestedProfileKey())) {

                logger.trace(
                        "Translating {} selection of profile key {} to apathy about profile selection.",
                        userName,
                        event.getRequestedProfileKey());
                profileSelectionRegistry.registerUserProfileSelection(userName, null);

                return;
            }

            if (!immutableMappings.containsKey(event.getRequestedProfileKey())) {
                logger.warn(
                        "User desired a profile by a key {} that does not map to any profile fname.  Ignoring.",
                        event.getRequestedProfileKey());
                return;
            }

            final String profileFName = immutableMappings.get(event.getRequestedProfileKey());

            logger.trace(
                    "Storing {} selection of profile fname {} (keyed by profile key {})",
                    userName,
                    profileFName,
                    event.getRequestedProfileKey());
            profileSelectionRegistry.registerUserProfileSelection(userName, profileFName);

        } catch (final Exception e) {
            logger.error("Something went wrong handling profile selection event " + event);
        }
    }

    @Override
    public String getProfileFname(IPerson person, HttpServletRequest request) {

        Validate.notNull(person, "Cannot get profile fname for a null person.");
        Validate.notNull(request, "Cannot get profile fname for a null request.");

        final String userName = person.getUserName();
        Validate.notNull(userName, "Cannot get profile fname for a null username.");

        try {
            return this.profileSelectionRegistry.profileSelectionForUser(userName);
        } catch (Exception e) {
            logger.error(
                    "Failed to read persisted profile selection for user "
                            + userName
                            + " if any.  Ignoring.",
                    e);
        }

        return null; // default to no opinion if registry lookup failed.
    }

    @Autowired
    public void setProfileSelectionRegistry(
            final IProfileSelectionRegistry profileSelectionRegistry) {
        this.profileSelectionRegistry = profileSelectionRegistry;
    }

    @Required
    public void setMappings(Map<String, String> mappings) {

        Validate.notNull(mappings);

        this.immutableMappings = ImmutableMap.copyOf(mappings);
    }

    @Autowired
    public void setIdentitySwapperManager(final IdentitySwapperManager manager) {
        this.identitySwapperManager = manager;
    }

    /**
     * Set the profile key that should be translated to "no selection".
     *
     * <p>Optional. Setting to null means no key will have this behavior. Defaults to "default".
     *
     * <p>Typically, this will be the value "default". "default" will typically both map to whatever
     * profile fname is your default (so, "resopndr") AND a user selecting "default" (rather than
     * "respondr") is saying "I have no selection and want to accept the default behavior", NOT "I
     * prefer whatever default maps to right now forver even if default changes in the future."
     *
     * @param profileKeyForNoSelection potentially null key that you want to mean apathy about
     *     profile selection
     */
    public void setProfileKeyForNoSelection(final String profileKeyForNoSelection) {
        this.profileKeyForNoSelection = profileKeyForNoSelection;
    }
}
