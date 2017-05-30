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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.Validate;
import org.apereo.portal.security.IPerson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * Returns the specified profile FName for a given server specified in the server regular expression
 * or returns null if server doesn't match
 *
 * <p>This mapper is intended to be used within the profile mapping chain within
 * ChaniningProfileMapperImpl
 */
public class ServerProfileMapperImpl implements IProfileMapper {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private String profile = "default";

    private String serverRegex;

    private Pattern pattern;

    /**
     * Sets the regular expression for the servers you want to select a profile for
     *
     * @param serverRegex
     */
    @Required
    public void setServerRegex(String serverRegex) {
        Validate.notBlank(serverRegex);
        this.serverRegex = serverRegex;
        this.pattern = Pattern.compile(this.serverRegex);
    }

    /**
     * Sets the profile users will be sent to
     *
     * @param defaultProfile
     */
    public void setProfile(String profile) {
        Validate.notBlank(profile);
        this.profile = profile;
    }

    /**
     * Returns the profile name specified by {@link #setDefaultProfile(String defaultProfile)
     * setDefaultProfile} or "default" if {@link #setDefaultProfile(String defaultProfile)
     * setDefaultProfile} is not called if the user is using a server in the server regular
     * expression specified by {@link #setServerRegex(String serverRegex) setServerRegex} returns
     * null otherwise
     *
     * @param person cannot be null
     * @param request cannot be null
     */
    @Override
    public String getProfileFname(IPerson person, HttpServletRequest request) {
        Validate.notNull(person, "Cannot get profile fname for a null person.");
        Validate.notNull(request, "Cannot get profile fname for a null request.");

        String userName = person.getUserName();

        final String userServerName = request.getServerName();

        Matcher matcher = this.pattern.matcher(userServerName);

        if (matcher.matches()) {
            logger.debug(
                    "User {} will be given {} profile because server {} matches regular expression {}",
                    userName,
                    this.profile,
                    userServerName,
                    this.serverRegex);
            return this.profile;
        } else {
            logger.debug(
                    "User {} will not be given {} profile because server {} does not match regular expression {}",
                    userName,
                    this.profile,
                    userServerName,
                    this.serverRegex);
            return null;
        }
    }
}
