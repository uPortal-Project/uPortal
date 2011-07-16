/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.groups.pags.testers;

import java.util.regex.Pattern;

/**
 * A tester for matching the possibly multiple values of an attribute 
 * against a regular expression.  If any of the values matches the pattern, 
 * the tester returns true.
 * <p>
 * @author Dan Ellentuck
 * @version $Revision$
 */

public class RegexTester extends StringTester {
    protected final Pattern pattern;

    public RegexTester(String attribute, String test) {
        super(attribute, test);
        this.pattern = Pattern.compile(test);
    }

    @Override
    public boolean test(String att) {
        return pattern.matcher(att).matches();
    }

}
