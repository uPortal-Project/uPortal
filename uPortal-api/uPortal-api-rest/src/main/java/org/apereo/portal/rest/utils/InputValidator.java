package org.apereo.portal.rest.utils;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apereo.portal.dao.portletlist.jpa.PortletListItem;

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
     * @param items list of portlet list items
     * @param item a given portlet list item (assumed to be in the list at least once)
     */
    public static void validateUniqueEntityId(List<PortletListItem> items, PortletListItem item) {
        if (items.stream()
                        .filter(o -> item.getEntityId().equals(o.getEntityId()))
                        .collect(Collectors.toList())
                        .size()
                > 1) {
            throw new IllegalArgumentException("entity IDs must be unique in the items list");
        }
    }
}
