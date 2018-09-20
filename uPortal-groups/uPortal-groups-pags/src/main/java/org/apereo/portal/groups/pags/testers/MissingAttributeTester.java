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
import org.apereo.portal.security.IPerson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Class testing a missing attribute on IPerson. */
public class MissingAttributeTester implements IPersonTester {

    protected final String attributeName;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /** @since 5.2 */
    public MissingAttributeTester(IPersonAttributesGroupTestDefinition definition) {
        super();
        attributeName = definition.getAttributeName();
    }

    /** @return String */
    public String getAttributeName() {
        return attributeName;
    }

    @Override
    public final boolean test(IPerson person) {
        Object[] atts = person.getAttributeValues(getAttributeName());
        return atts == null || atts.length < 1;
    }
}
