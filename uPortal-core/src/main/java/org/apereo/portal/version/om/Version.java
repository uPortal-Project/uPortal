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
package org.apereo.portal.version.om;

/**
 * Describes a version number, based on http://apr.apache.org/versioning.html <br>
 * Versions MUST implement equality as checking if the Major, Minor, Patch, and Local values ALL
 * match
 *
 */
public interface Version extends Comparable<Version> {
    /** Describes the fields of the version number */
    public enum Field {
        /*
         * The order of the enum fields is of critical importance. The order MUST NOT
         * be changed without a complete understanding of how it affects field importance
         * and upgrade logic.
         *
         * primarily importance is defined as 0 being least important and increasing
         * as the ordinal increases
         */

        /** Local part */
        LOCAL(),

        /** Patch part */
        PATCH,

        /** Minor part */
        MINOR,

        /** Major part */
        MAJOR;

        /**
         * Determine if this field is less important than the specified field. Importance is defined
         * from left to right on the version number with the major value (left most) being the most
         * important.
         *
         * @throws IllegalArgumentException if Field is null
         */
        public boolean isLessImportantThan(Field f) {
            if (f == null) {
                throw new IllegalArgumentException("Cannot compare to null");
            }

            return f.ordinal() - this.ordinal() > 0;
        }

        /**
         * @return The previous field in importance order, null if this is the least important field
         */
        public Field getLessImportant() {
            final int o = this.ordinal();
            if (o == 0) {
                return null;
            }

            return values()[o - 1];
        }

        /** @return The next field in importance order, null if this is the most important field */
        public Field getMoreImportant() {
            final int o = this.ordinal();
            final Field[] v = values();
            if (o == v.length - 1) {
                return null;
            }

            return v[o + 1];
        }
    }

    /** @return The major part */
    int getMajor();

    /** @return The minor part */
    int getMinor();

    /** @return The patch part */
    int getPatch();

    /** @return The optional local part */
    Integer getLocal();

    /** @return true if this version comes before the other version */
    boolean isBefore(Version other);

    /** @return true if this version comes after the other version */
    boolean isAfter(Version other);
}
