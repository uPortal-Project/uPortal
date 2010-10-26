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

import org.jasig.portal.security.IPerson;

/**
 * Abstract class tests a possibly multi-valued attribute against
 * a test value.
 * <p>
 * @author Dan Ellentuck
 * @version $Revision$
 */

public abstract class StringTester extends BaseAttributeTester {

public StringTester(String attribute, String test) {
    super(attribute, test);
}

public boolean test(IPerson person) {
    boolean result = false;
    Object[] atts = person.getAttributeValues(getAttributeName());
    if ( atts != null )
    {
        for (int i=0; i<atts.length && result == false; i++)
        { 
            String att = (String)atts[i];
            result = test(att); 
            
            // Assume that we should perform OR matching on multi-valued 
            // attributes.  If the current attribute matches, return true
            // for the person.
            if (result) {
                return true;
            }
        }
    }
    return result;
}
public boolean test(String att) { return false; }

}
