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

import org.jasig.portal.groups.pags.testers.StringTester;

/**
 * Tests whether or not the user has <em>some</em>
 * value for a particular attribute.
 * This tester ignores the test-value field.
 * If the attribute has any value, then it returns true.
 * @author Nick Blair, nblair@wisc.edu
 * @version $Revision$
 */
public class ValueExistsTester extends StringTester {

    public ValueExistsTester(String attribute, String test) {
        super(attribute, test);
    }

    public boolean test(String att) {
        boolean result = false;
        if (att != null && !att.equals("")) {
            result = true;
        }
        return result;
    }
}
