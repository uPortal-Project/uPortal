package org.apereo.portal.layout;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apereo.portal.security.IPerson;

/**
 * Class that helps personalize portlet definitions by replacing tokens with user attributes.
 *
 * <p>TODO: Includes a cache to facilitate reuse of same data.
 */
@Slf4j
public class LayoutPortletPersonalizer {
    // TODO: make a parameter, class attribute
    public static final String prefix = "prefix.";
    // TODO: make a parameter, class attribute
    public static final Pattern pattern = Pattern.compile("\\{\\{(.+)\\}\\}");

    private Map<String, String> replacements;

    LayoutPortletPersonalizer(IPerson person) {
        this.replacements = createTokenMappingForPerson(person);
    }

    public String personalize(String text) {
        return replaceTokens(pattern, text, replacements);
    }

    private Map<String, String> createTokenMappingForPerson(IPerson person) {
        HashMap<String, String> replacements = new HashMap<>();
        // populate the replacements map ...

        Set<String> attrNames = person.getAttributeMap().keySet();
        for (String attrName : attrNames) {
            final String key = prefix + attrName;
            final Object valObj = person.getAttribute(attrName);
            if (valObj instanceof String) {
                final String value = (String) valObj;
                log.debug("Person attribute pair added for key: " + key + ", " + value);
                replacements.put(key, value);
            } else if (valObj instanceof String[]) {
                final String value = ((String[]) valObj)[0];
                log.debug("Person attribute pair added for key: " + key + ", " + value);
                replacements.put(key, value);
            } else {
                log.warn("Person attribute value is not a string!! : " + key);
            }
        }
        return replacements;
    }

    public static String replaceTokens(
            Pattern pattern, String text, Map<String, String> replacements) {
        if (text == null) {
            return null;
        }
        Matcher matcher = pattern.matcher(text);
        StringBuilder builder = new StringBuilder();
        int i = 0;
        while (matcher.find()) {
            log.debug(matcher.group(0));
            log.debug(matcher.group(1));
            String replacement = replacements.get(matcher.group(1));
            log.debug(replacement);
            builder.append(text, i, matcher.start());
            if (replacement == null) builder.append(matcher.group(0));
            else builder.append(replacement);
            i = matcher.end();
        }
        if (i == 0) {
            return text;
        } else {
            log.debug("Token found in: " + text);
            builder.append(text.substring(i));
            return builder.toString();
        }
    }
}
