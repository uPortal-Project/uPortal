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
package org.apereo.portal.utils;

import java.util.Collection;

/**
 */
public final class IncludeExcludeUtils {
    private IncludeExcludeUtils() {}

    /**
     * Determines if the specified value is included based on the contents of the include and
     * exclude collections.
     *
     * <p>Returns true if one of the following is true: <br>
     * includes is empty && excludes is empty<br>
     * includes contains value<br>
     * includes is empty and excludes does not contain value<br>
     *
     * @param value Value to test
     * @param includes Included values
     * @param excludes Excluded values
     * @return true if it should be included
     */
    public static <T> boolean included(
            T value, Collection<? extends T> includes, Collection<? extends T> excludes) {
        return (includes.isEmpty() && excludes.isEmpty())
                || includes.contains(value)
                || (includes.isEmpty() && !excludes.contains(value));
    }
}
