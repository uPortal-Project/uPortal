package org.apereo.portal.utils.validators;

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
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Pattern;

public class InputValidator {
    private static final String ALPHANUMERIC_AND_DASH_VALIDATOR_REGEX =
            "^[\\w\\-]{1,500}$"; // 1-500 characters
    private static final Pattern ALPHANUMERIC_AND_DASH_VALIDATOR_PATTERN =
            Pattern.compile(ALPHANUMERIC_AND_DASH_VALIDATOR_REGEX);

    /**
     * @param s value to validate
     * @param inputType not sanitized and will be returned in json.
     * @return The validated value
     */
    public static String validateAsWordCharacters(String s, String inputType) {
        if (!ALPHANUMERIC_AND_DASH_VALIDATOR_PATTERN.matcher(s).matches()) {
            throw new IllegalArgumentException(
                    "Specified "
                            + inputType
                            + " is not the correct length or has invalid characters.");
        }
        return s;
    }

    /**
     * @param s value to validate
     * @return
     */
    public static String validateAsURL(String s) {
        try {
            new URL(s).toURI();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("URL " + s + " is invalid");
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("URL " + s + " is invalid");
        }
        return s;
    }
}
