package org.apereo.portal.utils.validators;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Pattern;

public class InputValidator {
	private static final String ALPHANUMERIC_AND_DASH_VALIDATOR_REGEX = "^[\\w\\-]{1,500}$"; // 1-500 characters
	private static final Pattern ALPHANUMERIC_AND_DASH_VALIDATOR_PATTERN = Pattern
			.compile(ALPHANUMERIC_AND_DASH_VALIDATOR_REGEX);

	/**
	 * @param s         value to validate
	 * @param inputType not sanitized and will be returned in json.
	 * @return The validated value
	 */
	public static String validateAsWordCharacters(String s, String inputType) {
		if (!ALPHANUMERIC_AND_DASH_VALIDATOR_PATTERN.matcher(s).matches()) {
			throw new IllegalArgumentException(
					"Specified " + inputType + " is not the correct length or has invalid characters.");
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
