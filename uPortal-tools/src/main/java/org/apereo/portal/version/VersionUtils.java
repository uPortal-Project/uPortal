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
package org.apereo.portal.version;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apereo.portal.version.om.Version;
import org.apereo.portal.version.om.Version.Field;

/** Utilities for working with Version classes */
public class VersionUtils {
    private static final Pattern VERSION_PATTERN =
            Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)(?:\\.(\\d+))?(?:[\\.-].*)?$");

    /**
     * Parse a version string into a Version object, if the string doesn't match the pattern null is
     * returned.
     *
     * <p>The regular expression used in parsing is: ^(\d+)\.(\d+)\.(\d+)(?:\.(\d+))?(?:[\.-].*)?$
     *
     * <p>Examples that match correctly:
     *
     * <ul>
     *   <li>4.0.5
     *   <li>4.0.5.123123
     *   <li>4.0.5-SNAPSHOT
     *       <ul>
     *         Examples do NOT match correctly:
     *         <ul>
     *           <li>4.0
     *           <li>4.0.5_123123
     *               <ul>
     */
    public static Version parseVersion(String versionString) {
        final Matcher versionMatcher = VERSION_PATTERN.matcher(versionString);
        if (!versionMatcher.matches()) {
            return null;
        }

        final int major = Integer.parseInt(versionMatcher.group(1));
        final int minor = Integer.parseInt(versionMatcher.group(2));
        final int patch = Integer.parseInt(versionMatcher.group(3));
        final String local = versionMatcher.group(4);
        if (local != null) {
            return new SimpleVersion(major, minor, patch, Integer.valueOf(local));
        }

        return new SimpleVersion(major, minor, patch);
    }

    /**
     * Determine how much of two versions match. Returns null if the versions do not match at all.
     *
     * @return null for no match or the name of the most specific field that matches.
     */
    public static Version.Field getMostSpecificMatchingField(Version v1, Version v2) {
        if (v1.getMajor() != v2.getMajor()) {
            return null;
        }

        if (v1.getMinor() != v2.getMinor()) {
            return Version.Field.MAJOR;
        }

        if (v1.getPatch() != v2.getPatch()) {
            return Version.Field.MINOR;
        }

        final Integer l1 = v1.getLocal();
        final Integer l2 = v2.getLocal();
        if (l1 != l2 && (l1 == null || l2 == null || !l1.equals(l2))) {
            return Version.Field.PATCH;
        }

        return Version.Field.LOCAL;
    }

    /**
     * Determine if an "update" can be done between the from and to versions. The ability to update
     * is defined as from == to OR (from.isBefore(to) AND mostSpecificMatchingField in (PATCH,
     * MINOR))
     *
     * @param from Version updating from
     * @param to Version updating to
     * @return true if the major and minor versions match and the from.patch value is less than or
     *     equal to the to.patch value
     */
    public static boolean canUpdate(Version from, Version to) {
        final Field mostSpecificMatchingField = getMostSpecificMatchingField(from, to);
        switch (mostSpecificMatchingField) {
            case LOCAL:
                {
                    return true;
                }
            case PATCH:
            case MINOR:
                {
                    return from.isBefore(to);
                }
            default:
                {
                    return false;
                }
        }
    }

    private static final class SimpleVersion extends AbstractVersion {
        private static final long serialVersionUID = 1L;

        private final int major;
        private final int minor;
        private final int patch;
        private final Integer local;

        public SimpleVersion(int major, int minor, int patch) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
            this.local = null;
        }

        public SimpleVersion(int major, int minor, int patch, Integer local) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
            this.local = local;
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

        public Integer getLocal() {
            return local;
        }
    }
}
