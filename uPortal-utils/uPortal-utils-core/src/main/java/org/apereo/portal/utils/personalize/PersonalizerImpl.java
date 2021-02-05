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
package org.apereo.portal.utils.personalize;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apereo.portal.security.IPerson;
import org.springframework.beans.factory.annotation.Value;

/** Class that helps personalize text by replacing tokens with user attributes. */
@Slf4j
public class PersonalizerImpl implements IPersonalizer {
    @Value("${org.apereo.portal.utils.personalize.PersonalizerImpl.prefix:apereo.}")
    private String prefix;

    @Value("${org.apereo.portal.utils.personalize.PersonalizerImpl.pattern:@up@(.*?)@up@}")
    private String patternStr;

    // One pattern for all personalization requests
    private Pattern pattern;

    @PostConstruct
    private void postConstruct() {
        log.debug("Using the replacement pattern: [{}] and prefix [{}]", patternStr, prefix);
        pattern = Pattern.compile(patternStr);
    }

    public String personalize(IPerson person, String text) {
        return personalize(person, text, null);
    }

    public String personalize(IPerson person, String text, HttpSession session) {
        if (person == null) {
            log.debug("person is null - skipping personalization of text [{}]", text);
            return text;
        }
        if (session == null) {
            // No caching available.
            Map<String, String> replacements = createTokenMappingForPerson(person);

            return replaceTokens(pattern, text, replacements);
        }

        Map<String, String> replacements;
        // Caching is available
        final Object personalizationCacheObj =
                session.getAttribute(PersonalizationConstants.USER_PERSONALIZATION_TOKENS);
        if (personalizationCacheObj == null) {
            // First time for the session.  Create and add to the cache
            log.debug(
                    "No [{}] cache found in the session.  Building a new one and saving in the session",
                    PersonalizationConstants.USER_PERSONALIZATION_TOKENS);
            replacements = createTokenMappingForPerson(person);
            session.setAttribute(
                    PersonalizationConstants.USER_PERSONALIZATION_TOKENS, replacements);
        } else if (personalizationCacheObj instanceof Map<?, ?>) {
            replacements = (Map<String, String>) personalizationCacheObj;
            log.debug(
                    "Using the [{}] cache found in the session.",
                    PersonalizationConstants.USER_PERSONALIZATION_TOKENS);
        } else {
            // Something corrupted the cache.  Log, rebuild, and clobber
            log.warn(
                    "Unexpected object type of [{}] for the [{}].  Rebuilding and replacing.",
                    personalizationCacheObj.getClass().getCanonicalName(),
                    PersonalizationConstants.USER_PERSONALIZATION_TOKENS);
            replacements = createTokenMappingForPerson(person);
            session.setAttribute(
                    PersonalizationConstants.USER_PERSONALIZATION_TOKENS, replacements);
        }

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
                log.debug("Person attribute pair added for key: [{}], [{}]", key, value);
                replacements.put(key, value);
            } else if (valObj instanceof String[]) {
                final String value = ((String[]) valObj)[0];
                log.debug("Person attribute pair added for key: [{}], [{}]", key, value);
                replacements.put(key, value);
            } else {
                log.warn("Person attribute value is not a string!! : [{}]", key);
            }
        }
        return replacements;
    }

    private static String replaceTokens(
            Pattern pattern, String text, Map<String, String> replacements) {
        if (text == null) {
            return null;
        }

        if (replacements == null) {
            log.warn("Replacement data map not set.  Ignoring personalization of text.");
            return text;
        }

        if (pattern == null) {
            log.warn("Pattern regex not set.  Ignoring personalization of text.");
            return text;
        }
        log.debug("Text to personalize: [{}]", text);
        Matcher matcher = pattern.matcher(text);
        StringBuilder builder = new StringBuilder();
        int i = 0;
        while (matcher.find()) {
            log.debug("Found group(0) in the text: [{}]", matcher.group(0));
            log.debug("Found group(1) in the text: [{}]", matcher.group(1));
            final String replacement = replacements.get(matcher.group(1));
            log.debug("Personalization to use for key[{}]: [{}]", matcher.group(1), replacement);
            builder.append(text, i, matcher.start());
            if (replacement == null) builder.append(matcher.group(0));
            else builder.append(replacement);
            i = matcher.end();
        }
        if (i == 0) {
            return text;
        } else {
            log.debug("Token found in: [{}]", text);
            builder.append(text.substring(i));
            final String result = builder.toString();
            if (text.equals(result)) {
                log.warn(
                        "Something unexpected happened - the original text is equal to the personalized text");
            }
            return result;
        }
    }
}
