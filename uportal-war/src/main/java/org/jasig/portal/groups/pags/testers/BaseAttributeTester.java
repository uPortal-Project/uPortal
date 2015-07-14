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

import org.jasig.portal.groups.pags.IPersonTester;
import org.jasig.portal.groups.pags.dao.IPersonAttributesGroupTestDefinition;

/**
 * A tester for examining <code>IPerson</code> attributes.  
 * <p>
 * @author Dan Ellentuck
 * @version $Revision$
 */
public abstract class BaseAttributeTester implements IPersonTester {
    protected final String attributeName;
    protected final String testValue;

    /**
     * @since 4.3
     */
    public BaseAttributeTester(IPersonAttributesGroupTestDefinition definition) {
        super();
        attributeName = definition.getAttributeName();
        testValue = definition.getTestValue();
    }

    /**
     * @deprecated use {@link EntityPersonAttributesGroupStore}, which leverages
     * the single-argument constructor.
     */
    @Deprecated
    public BaseAttributeTester(String attribute, String test) {
        super();
        attributeName = attribute;
        testValue = test;
    }

    /**
     * @return String
     */
    public String getAttributeName() {
        return attributeName;
    }

    /**
     * @return String
     */
    public String getTestValue() {
        return testValue;
    }

    /**
     * return String
     */
    public String asString(Object o) {
        String result = null;
        if (o instanceof String) {
            result = (String) o;
        } else {
            if (o instanceof String[]) {
                String[] sa = (String[]) o;
                if (sa.length > 0) {
                    result = sa[0];
                }
            }
        }
        return result;
    }

    public String toString() {
        return "Tester for " + getAttributeName() + " : " + getTestValue();
    }

}
