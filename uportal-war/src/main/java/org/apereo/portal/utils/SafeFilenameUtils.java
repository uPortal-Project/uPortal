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

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 */
public final class SafeFilenameUtils {
    // Reserved names on Windows (see http://en.wikipedia.org/wiki/Filename)
    private static final List<Pattern> WINDOWS_INVALID_PATTERNS =
            Arrays.asList(
                    new Pattern[] {
                        Pattern.compile("AUX", Pattern.CASE_INSENSITIVE),
                        Pattern.compile("CLOCK\\$", Pattern.CASE_INSENSITIVE),
                        Pattern.compile("COM\\d*", Pattern.CASE_INSENSITIVE),
                        Pattern.compile("CON", Pattern.CASE_INSENSITIVE),
                        Pattern.compile("LPT\\d*", Pattern.CASE_INSENSITIVE),
                        Pattern.compile("NUL", Pattern.CASE_INSENSITIVE),
                        Pattern.compile("PRN", Pattern.CASE_INSENSITIVE)
                    });

    private static Map<Pattern, String> REPLACEMENT_PAIRS =
            ImmutableMap.<Pattern, String>builder()
                    .put(Pattern.compile("/|\\\\"), ".")
                    .put(Pattern.compile("[~`@\\|\\s#$\\*]"), "_")
                    .build();

    private SafeFilenameUtils() {}

    /** Makes 'safe' filename */
    public static String makeSafeFilename(String filename) {
        //Replace invalid characters
        for (final Map.Entry<Pattern, String> pair : REPLACEMENT_PAIRS.entrySet()) {
            final Pattern pattern = pair.getKey();
            final Matcher matcher = pattern.matcher(filename);
            filename = matcher.replaceAll(pair.getValue());
        }

        // Make sure the name doesn't violate a Windows reserved word...
        for (Pattern pattern : WINDOWS_INVALID_PATTERNS) {
            if (pattern.matcher(filename).matches()) {
                filename = "uP-" + filename;
                break;
            }
        }

        return filename;
    }
}
