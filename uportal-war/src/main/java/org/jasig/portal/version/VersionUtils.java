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
    
    private static final class SimpleVersion implements Version {
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

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + major;
            result = prime * result + minor;
            result = prime * result + patch;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            SimpleVersion other = (SimpleVersion) obj;
            if (major != other.major)
                return false;
            if (minor != other.minor)
                return false;
            if (patch != other.patch)
                return false;
            return true;
        }
        
        @Override
        public String toString() {
            return major + "." + minor + "." + patch;
        }
    }
}
