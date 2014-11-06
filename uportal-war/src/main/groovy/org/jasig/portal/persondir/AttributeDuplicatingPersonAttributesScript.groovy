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

package org.jasig.portal.persondir
import org.jasig.services.persondir.support.BaseGroovyScriptDaoImpl
/**
 * Script that takes in an attribute key name to look for and if present in the passed in map of user attributes, copies
 * the attribute's values to the provided list of attribute names if not already present.
 * E.g. if an attribute called 'username' is present, add attributes uid and user.login.id if not present with the
 * list of values of the username attribute.
 *
 * If the keyToDuplicate is not present, getPersonAttributesFromMultivaluedAttributes() returns the existing
 * userAttributes unchanged as a no-op.
 *
 * @author James Wennmacher, jwennmacher@unicon.net
 */

class AttributeDuplicatingPersonAttributesScript extends BaseGroovyScriptDaoImpl {

    String keyToDuplicate;
    Set<String> desiredNames;

    AttributeDuplicatingPersonAttributesScript() {
    }

    public AttributeDuplicatingPersonAttributesScript(String keyToDuplicate, Set<String> desiredNames) {
        this.keyToDuplicate = keyToDuplicate;
        this.desiredNames = desiredNames;
    }

    @Override
    public Map<String, List<Object>> getPersonAttributesFromMultivaluedAttributes(Map<String, List<Object>> userAttributes) {
        if (userAttributes?.get(keyToDuplicate)) {
            List<Object> attributeValues = userAttributes.get(keyToDuplicate);
            Map<String, List<Object>> newUserAttributes = new HashMap<> (userAttributes);

            for (desiredName in desiredNames) {
                newUserAttributes.put(desiredName, attributeValues);
            }
            return newUserAttributes
        }
        return userAttributes;
    }

}
