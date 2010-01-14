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

import org.jasig.portal.groups.pags.testers.BaseAttributeTester;
import org.jasig.portal.security.IPerson;

/**
 * Tests whether the attribute is null or none of the
 * values of the attribute equal the specified attribute value.
 * @author Eric Dalquist, edalquist@unicon.net
 * @version $Revision$
 */
public class ValueMissingTester extends BaseAttributeTester {

    public ValueMissingTester(String attribute, String test) {
        super(attribute, test);
    }

    public boolean test(IPerson person) {
        // Get the list of values for the attribute
        Object[] vals = person.getAttributeValues(getAttributeName());

        // No values, test passed
        if (vals == null) {
            return true;
        } else {
            // Loop through the values of the attribute, if one is equal
            // to the test case the test fails and returns false
            for (int i = 0; i < vals.length; i++) {
                String val = (String)vals[i];

                if (val.equalsIgnoreCase(testValue)) {
                    return false;
                }
            }

            // None of the values equaled the test case, test passed
            return true;
        }
    }
}
