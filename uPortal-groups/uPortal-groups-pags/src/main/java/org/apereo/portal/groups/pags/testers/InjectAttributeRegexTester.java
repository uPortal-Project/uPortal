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
package org.apereo.portal.groups.pags.testers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apereo.portal.groups.pags.dao.IPersonAttributesGroupTestDefinition;
import org.apereo.portal.security.IPerson;

/**
 * A tester for matching the possibly multiple values of an attribute against a regular expression,
 * and in replacing a pattern by an other user attribute (optional use). If any of the values
 * matches the pattern, the tester returns true.
 *
 * <p><u>Optional: For a pattern replacement specify the user attribute name between @...@</u><br>
 * As example:
 *
 * <pre>
 *       {@code <test>
 *             <attribute-name>isMemberOf</attribute-name>
 *             <tester-class>org.apereo.portal.groups.pags.testers.InjectAttributeRegexTester</tester-class>
 *             <test-value>((lycee)|(cfa)|(college)):Applications:Cahier_de_texte:@CurrentOrganization@</test-value>
 *         </test>
 *       }
 *     </pre>
 *
 * Firstly the pattern <b>@CurrentOrganization@</b> will be replaced by the user attribute
 * <b>CurrentOrganization</b> value, after if the value is "<b>"ORGANIZATION_NAME</b>" (like for
 * tenant name) this pags will test if the user has in his attribute "<b>isMemberOf</b>" a value
 * matching the pattern
 * "<b>((lycee)|(cfa)|(college)):Applications:Cahier_de_texte:ORGANIZATION_NAME</b>"<br>
 * This is used mainly when a user is on several Organizations and to work on the Current
 * Organization context app, and when you have one portlet app per context.<br>
 * It's used to have one access rule on an app at once in a same layout.
 *
 * @since 5.0
 */
public final class InjectAttributeRegexTester extends BaseAttributeTester {

    private static final Pattern p = Pattern.compile("@(.+)@");

    public InjectAttributeRegexTester(IPersonAttributesGroupTestDefinition definition) {
        super(definition);
    }

    @Override
    public boolean test(IPerson person) {

        boolean result = false; // default

        /*
         * First replace all instances of @{attrName}@ with
         * the user's first value for the named attribute
         */
        final Matcher matcher = p.matcher(this.testValue);
        String secondPatternString = this.testValue;
        if (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                // Using the first value only
                final String firstValue = (String) person.getAttribute(matcher.group(i));
                if (firstValue != null) { // Should we return false if it is?
                    secondPatternString =
                            secondPatternString.replaceAll(
                                    "@" + matcher.group(i) + "@", firstValue);
                }
            }
        }

        /*
         * Now use the calculated pattern to test against this.attributeName
         */
        final Pattern secondPattern = Pattern.compile(secondPatternString);
        final Object[] values = person.getAttributeValues(getAttributeName());
        if (values != null && values.length != 0) {
            for (Object secondValue : values) {
                if (secondValue instanceof String
                        && secondPattern.matcher((String) secondValue).matches()) {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }
}
