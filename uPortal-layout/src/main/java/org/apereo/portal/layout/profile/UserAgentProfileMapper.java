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

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.Validate;
import org.apereo.portal.security.IPerson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maps User-Agents to profile names using regular expressions. A list of {@link Mapping}s is
 * evaluated in order and the first match is returned. If the user agent header is null or no match
 * is found the {@link #setDefaultProfileName(String)} value is used.
 *
 */
public class UserAgentProfileMapper implements IProfileMapper {
    protected Logger logger = LoggerFactory.getLogger(getClass());

    private List<Mapping> mappings = Collections.emptyList();
    private String defaultProfileName = null;
    private String userAgentHeader = "User-Agent";

    /** Regular expression to Profile name mappings. */
    public void setMappings(List<Mapping> mappings) {
        this.mappings = mappings;
    }

    /** Default profile name to return if no match is found, defaults to <code>null</code>. */
    public void setDefaultProfileName(String defaultProfileName) {
        this.defaultProfileName = defaultProfileName;
    }

    /**
     * {@link HttpServletRequest} header name to get the user agent string. Defaults to "User-Agent"
     */
    public void setUserAgentHeader(String userAgentHeader) {
        Validate.notNull(userAgentHeader);
        this.userAgentHeader = userAgentHeader;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.layout.profile.IProfileMapper#getProfileFname(org.apereo.portal.security.IPerson, javax.servlet.http.HttpServletRequest)
     */
    @Override
    public String getProfileFname(IPerson person, HttpServletRequest request) {
        final String userAgent = request.getHeader(this.userAgentHeader);
        if (userAgent == null) {
            this.logger.debug(
                    "No {} header for {} returning default profile",
                    this.userAgentHeader,
                    person.getUserName());
            return this.defaultProfileName;
        }

        for (final Mapping mapping : this.mappings) {
            final Matcher matcher = mapping.pattern.matcher(userAgent);
            if (matcher.matches()) {
                this.logger.debug(
                        "Matched {} header {} for {} returning profile {}",
                        new Object[] {
                            this.userAgentHeader, person.getUserName(), mapping.profileName
                        });
                return mapping.profileName;
            }
        }

        this.logger.debug(
                "No matching Mapping for {} header for {} returning default profile",
                this.userAgentHeader,
                person.getUserName());
        return defaultProfileName;
    }

    public static class Mapping {
        private Pattern pattern;
        private String profileName;

        public void setPattern(String pattern) {
            this.pattern = Pattern.compile(pattern);
        }

        public void setProfileName(String profileName) {
            this.profileName = profileName;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.pattern == null) ? 0 : this.pattern.hashCode());
            result =
                    prime * result + ((this.profileName == null) ? 0 : this.profileName.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Mapping other = (Mapping) obj;
            if (this.pattern == null) {
                if (other.pattern != null) {
                    return false;
                }
            } else if (!this.pattern.equals(other.pattern)) {
                return false;
            }
            if (this.profileName == null) {
                if (other.profileName != null) {
                    return false;
                }
            } else if (!this.profileName.equals(other.profileName)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "Mapping [pattern=" + this.pattern + ", profileName=" + this.profileName + "]";
        }
    }
}
