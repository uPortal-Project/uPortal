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
package org.apereo.portal.rendering.predicates;

import com.google.common.base.Predicate;
import javax.servlet.http.HttpServletRequest;
import org.apereo.portal.user.IUserInstance;
import org.apereo.portal.user.IUserInstanceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

/**
 * Predicate determining whether the given request represents a request in the context of a profile
 * matching the configured profile fname.
 *
 * @since 4.2
 */
public class ProfileFNamePredicate implements Predicate<HttpServletRequest> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    // auto-wired
    private IUserInstanceManager userInstanceManager;

    // dependency-injected
    private String profileFNameToMatch;

    @Override
    public boolean apply(final HttpServletRequest request) {

        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);

        final String profileFName =
                userInstance.getPreferencesManager().getUserProfile().getProfileFname();

        // used for logging
        final String username = userInstance.getPerson().getUserName();

        if (profileFNameToMatch.equals(profileFName)) {

            logger.debug(
                    "User {} does have profile with matching fname {}.", username, profileFName);

            return true;
        }

        logger.debug(
                "Request for user {} presents profile fname {} which does not match configured profile fname {}.",
                username,
                profileFName,
                profileFNameToMatch);

        return false;
    }

    @Autowired
    public void setUserInstanceManager(final IUserInstanceManager userInstanceManager) {
        Assert.notNull(userInstanceManager);
        this.userInstanceManager = userInstanceManager;
    }

    @Required
    public void setProfileFNameToMatch(final String profileFNameToMatch) {
        this.profileFNameToMatch = profileFNameToMatch;
    }

    @Override
    public String toString() {
        return "Predicate: true where profile fname is " + this.profileFNameToMatch + ".";
    }
}
