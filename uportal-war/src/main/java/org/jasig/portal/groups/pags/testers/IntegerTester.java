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

import org.jasig.portal.groups.pags.dao.IPersonAttributesGroupTestDefinition;
import org.jasig.portal.security.IPerson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class tests the possibly multiple values of an 
 * <code>IPerson</code> integer attribute. 
 * <p>
 * @author Dan Ellentuck
 * @version $Revision$
 */

public abstract class IntegerTester extends BaseAttributeTester {
    protected int testInteger = Integer.MIN_VALUE;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * @since 4.3
     */
    public IntegerTester(IPersonAttributesGroupTestDefinition definition) {
        super(definition);
        this.testInteger = Integer.parseInt(definition.getTestValue());
    }

    /**
     * @deprecated use {@link EntityPersonAttributesGroupStore}, which leverages
     * the single-argument constructor.
     */
    @Deprecated
    public IntegerTester(String attribute, String test) {
        super(attribute, test); 
        testInteger = Integer.parseInt(test);
    }
    public int getTestInteger() {
        return testInteger;
    }

    public boolean test(IPerson person) {

        boolean result = false;
        Object[] atts = person.getAttributeValues(getAttributeName());

        if (atts != null) {
            for (int i=0; i<atts.length && result == false; i++) {
                Object objValue = atts[i];
                try {
                    Integer intValue = null;

                    if (objValue == null) {
                        continue;
                    }

                    if (objValue instanceof Integer) {
                        intValue = (Integer) objValue;
                    } else if (objValue instanceof Long) {
                        Long lngValue = (Long) objValue;
                        if (lngValue <= Integer.MAX_VALUE && lngValue >= Integer.MIN_VALUE) {
                            // A value outside this range is not valid to test
                            intValue = lngValue.intValue();
                        }
                    } else if (objValue instanceof String) {
                        String strValue = (String) objValue;
                        intValue = Integer.parseInt(strValue);
                    }

                    if (intValue != null) {
                        // A positive result breaks the loop
                        result = test(intValue);
                    }
                }
                catch (NumberFormatException nfe) {
                    // result stays false
                    logger.debug("Value not parsable to an int:  {}", objValue, nfe);
                }
            }
        }

        return result;

    }

    public abstract boolean test(int attributeValue);

}