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

import org.jasig.portal.groups.pags.dao.EntityPersonAttributesGroupStore;
import org.jasig.portal.groups.pags.dao.IPersonAttributesGroupTestDefinition;
import org.jasig.portal.security.IPerson;

/**
 * A tester for examining if an <code>IPerson</code> attribute has less than nth values.
 *
 * @author GIP RECIA - Julien Gribonvald
 * @version $Revision$
 */
public class NbValuesLTTester extends AbstractIntegerTester {

	/**
	 * @since 4.3
	 */
	public NbValuesLTTester(IPersonAttributesGroupTestDefinition definition) {
		super(definition);
	}

	/**
	 * @deprecated use {@link EntityPersonAttributesGroupStore}, which leverages
	 * the single-argument constructor.
	 */
	@Deprecated
	public NbValuesLTTester(String attribute, String test) {
		super(attribute, test);
	}

	@Override
	public boolean test(IPerson person) {
		boolean result = false;
		Object[] atts = person.getAttributeValues(getAttributeName());
		if (atts != null) {
			result = atts.length < getTestInteger();
		}
		return result;
	}
	public boolean test(int attributeValue) {return false;}

}
