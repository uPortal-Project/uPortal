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
package org.jasig.portal.version;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jasig.portal.version.om.Version;

/**
 * Utilities for working with Version classes
 */
public class VersionUtils {
    private static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)(?:[\\.-].*)?$");
    
    /**
     * Parse a version string into a Version object, if the string doesn't match the pattern null is returned.
     * <p>
     * The regular expression used in parsing is: ^(\d+)\.(\d+)\.(\d+)(?:[\.-].*)?$
     * <p>
     * Examples that match correctly:
     * <ul>
     * <li>4.0.5</li>
     * <li>4.0.5.123123</li>
     * <li>4.0.5-SNAPSHOT</li>
     * <ul>
     * 
     * Examples do NOT match correctly:
     * <ul>
     * <li>4.0</li>
     * <li>4.0.5_123123</li>
     * <ul>
     */
    public static Version parseVersion(String versionString) {
        final Matcher versionMatcher = VERSION_PATTERN.matcher(versionString);
        if (!versionMatcher.matches()) {
            return null;
        }
        
        final int major = Integer.parseInt(versionMatcher.group(1));
        final int minor = Integer.parseInt(versionMatcher.group(2));
        final int patch = Integer.parseInt(versionMatcher.group(3));
        return new SimpleVersion(major, minor, patch);
    }
    
    /**
     * Determine if an "update" can be done between the from and to versions.
     * 
     * @param from Version updating from
     * @param to Version updating to
     * @return true if the major and minor versions match and the from.patch value is less than or equal to the to.patch value
     */
    public static boolean canUpdate(Version from, Version to) {
        return from.getMajor() == to.getMajor() &&
                from.getMinor() == to.getMinor() &&
                from.getPatch() <= to.getPatch();
    }
    
    private static final class SimpleVersion extends AbstractVersion {
		private static final long serialVersionUID = 1L;

		private final int major;
        private final int minor;
        private final int patch;
        
        public SimpleVersion(int major, int minor, int patch) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
        }

        @Override
        public int getMajor() {
            return major;
        }

        @Override
        public int getMinor() {
            return minor;
        }

        @Override
        public int getPatch() {
            return patch;
        }
    }
}
