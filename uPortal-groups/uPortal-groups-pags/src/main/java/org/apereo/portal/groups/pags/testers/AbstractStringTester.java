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

/** Abstract class tests a possibly multi-valued attribute against a test value. */
public abstract class AbstractStringTester extends BaseAttributeTester {

    /** @since 4.3 */
    public AbstractStringTester(IPersonAttributesGroupTestDefinition definition) {
        super(definition);
    }

    @Override
    public final boolean test(IPerson person) {
        boolean result = false;
        Object[] atts = person.getAttributeValues(getAttributeName());
        if (atts != null) {
            for (int i = 0; i < atts.length && result == false; i++) {
                String att = (String) atts[i];
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

    /** Subclasses provide a concrete implementation of this method to perform their testing. */
    public abstract boolean test(String att);
}
