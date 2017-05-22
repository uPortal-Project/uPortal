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

import org.apereo.portal.groups.pags.dao.IPersonAttributesGroupTestDefinition;
import org.apereo.portal.security.IPerson;

/**
 * Abstract base class for testers that test the value(s) of an <code>IPerson</code> integer
 * attribute.
 *
 */
public abstract class AbstractIntegerTester extends BaseAttributeTester {

    private final int testInteger;

    /** @since 4.3 */
    public AbstractIntegerTester(IPersonAttributesGroupTestDefinition definition) {
        super(definition);
        this.testInteger = Integer.parseInt(definition.getTestValue());
    }

    public int getTestInteger() {
        return testInteger;
    }

    public final boolean test(IPerson person) {

        boolean result = false; // default
        final Object[] atts = person.getAttributeValues(getAttributeName());

        if (atts != null) {
            for (int i = 0; i < atts.length && result == false; i++) {

                final Object objValue = atts[i];
                if (objValue == null) {
                    continue;
                }

                /*
                 * Currently we support:
                 *   - Integer
                 *   - Long
                 *   - String
                 */
                try {
                    Integer intValue = null;

                    if (objValue instanceof Integer) {
                        intValue = (Integer) objValue;
                    } else if (objValue instanceof Long) {
                        final Long lngValue = (Long) objValue;
                        if (lngValue >= Integer.MIN_VALUE && lngValue <= Integer.MAX_VALUE) {
                            // A value outside this range is not valid to test
                            intValue = lngValue.intValue();
                        }
                    } else if (objValue instanceof String) {
                        final String strValue = (String) objValue;
                        intValue = Integer.parseInt(strValue);
                    }

                    if (intValue != null) {
                        // A positive result will break the loop
                        result = test(intValue);
                    }
                } catch (NumberFormatException nfe) {
                    // result stays false
                    logger.debug("Value not parsable to an int:  {}", objValue, nfe);
                }
            }
        }

        return result;
    }

    public abstract boolean test(int attributeValue);

}
