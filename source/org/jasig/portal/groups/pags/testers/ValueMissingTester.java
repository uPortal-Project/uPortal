/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
