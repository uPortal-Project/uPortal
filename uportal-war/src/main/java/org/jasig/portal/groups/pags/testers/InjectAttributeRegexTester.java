/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.groups.pags.testers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jasig.portal.groups.pags.dao.IPersonAttributesGroupTestDefinition;
import org.jasig.portal.security.IPerson;

/**
 * A tester for matching the possibly multiple values of an attribute
 * against a regular expression, and in replacing a pattern by an other user attribute (optional use).  If any of the values matches the pattern,
 * the tester returns true.
 * <p>
 *     <u>Optional: For a pattern replacement specify the user attribute name between @...@</u><br/>
 *
 *     As example:
 *     <pre>
 *       {@code <test>
 *             <attribute-name>isMemberOf</attribute-name>
 *             <tester-class>org.jasig.portal.groups.pags.testers.InjectAttributeRegexTester</tester-class>
 *             <test-value>((lycee)|(cfa)|(college)):Applications:Cahier_de_texte:@CurrentOrganization@</test-value>
 *         </test>
 *       }
 *     </pre>
 *
 *     Firstly the pattern <b>@CurrentOrganization@</b> will be replaced by the user attribute <b>CurrentOrganization</b> value,
 *     after if the value is "<b>"ORGANIZATION_NAME</b>" (like for tenant name) this pags will test if the user
 *     has in his attribute "<b>isMemberOf</b>" a value matching the pattern "<b>((lycee)|(cfa)|(college)):Applications:Cahier_de_texte:ORGANIZATION_NAME</b>"<br/>
 *     This is used mainly when a user is on several Organizations and to work on the Current Organization context app, and when you have one portlet app per context.<br/>
 *     It's used to have one access rule on an app at once in a same layout.
 * </p>
 * @author Julien Gribonvald
 * @version $Revision$
 */

public class InjectAttributeRegexTester extends StringTester {

    private final static Pattern p = Pattern.compile("@(.+)@");

    protected Pattern pattern;

    /**
     * @since 4.3
     */
    public InjectAttributeRegexTester(IPersonAttributesGroupTestDefinition definition) {
        super(definition);
    }

    /**
     * @deprecated use {@link EntityPersonAttributesGroupStore}, which leverages
     * the single-argument constructor.
     */
    @Deprecated
    public InjectAttributeRegexTester(String attribute, String test) {
        super(attribute, test);
    }

    @Override
    public boolean test(IPerson person) {
    final Matcher matcher = p.matcher(this.testValue);
    String finalPattern = this.testValue;
    if (matcher.find()) {
        for (int i=1; i <= matcher.groupCount(); i++) {
            // Assuming the attribute to replace has only one value
            String att = (String) person.getAttribute(matcher.group(i));
            if ( att != null ) {
                finalPattern = finalPattern.replaceAll("@" + matcher.group(i) + "@" , att);
            }
        }
    }
    this.pattern = Pattern.compile(finalPattern);

    return super.test(person);
    }

    @Override
    public boolean test(String att) {
        return pattern.matcher(att).matches();
    }

}
