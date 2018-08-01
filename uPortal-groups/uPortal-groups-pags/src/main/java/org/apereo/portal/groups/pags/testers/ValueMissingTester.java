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
 * Tests whether the attribute is null or none of the values of the attribute
 * equal the specified attribute value.
 */
public class ValueMissingTester extends BaseAttributeTester {

	/** @since 4.3 */
	public ValueMissingTester(IPersonAttributesGroupTestDefinition definition) {
		super(definition);
	}

	@Override
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
				Object obj = (Object) vals[i];

				if (obj != null) {
					String val = getStringTransformedValue(obj);

					if (val.equalsIgnoreCase(testValue)) {
						return false;
					}
				}
			}

			// None of the values equaled the test case, test passed
			return true;
		}
	}

	/**
	 * This method will transform all the basic primitive Data Type Wrapper
	 * class objects to String
	 * 
	 * @param obj
	 * @return
	 */
	private String getStringTransformedValue(Object obj) {
		if (obj instanceof String) {
			return (String) obj;
		} else if (obj instanceof Long || obj instanceof Double || obj instanceof Boolean || obj instanceof Byte
				|| obj instanceof Character || obj instanceof Integer || obj instanceof Float || obj instanceof Short) {
			return String.valueOf(obj);
		}
		return "";
	}
}
