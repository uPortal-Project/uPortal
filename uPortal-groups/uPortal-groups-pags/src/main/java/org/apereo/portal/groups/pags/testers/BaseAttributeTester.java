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

import org.apereo.portal.groups.pags.IPersonTester;
import org.apereo.portal.groups.pags.dao.IPersonAttributesGroupTestDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A tester for examining <code>IPerson</code> attributes.
 *
 */
public abstract class BaseAttributeTester implements IPersonTester {

    protected final String attributeName;
    protected final String testValue;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /** @since 4.3 */
    public BaseAttributeTester(IPersonAttributesGroupTestDefinition definition) {
        super();
        attributeName = definition.getAttributeName();
        testValue = definition.getTestValue();
    }

    /** @return String */
    public String getAttributeName() {
        return attributeName;
    }

    /** @return String */
    public String getTestValue() {
        return testValue;
    }

    public String toString() {
        return "Tester for " + getAttributeName() + " : " + getTestValue();
    }

}
