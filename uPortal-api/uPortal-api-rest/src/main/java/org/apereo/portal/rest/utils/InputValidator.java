package org.apereo.portal.rest.utils;

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
}
