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

package org.jasig.portal.channels.cusermanager;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.security.IPerson;

import junit.framework.TestCase;

public class ChannelRuntimeDataToPersonConverterTest extends TestCase {

	private ChannelRuntimeDataToPersonConverter converter =  new ChannelRuntimeDataToPersonConverter();
	
	public void testMapsParametersToAttributes() {
		ChannelRuntimeData channelRuntimeData = new ChannelRuntimeData();
		channelRuntimeData.setParameter("param_one", "value_one");
		channelRuntimeData.setParameter("param_two", "value_two");
		
		IPerson person = converter.channelRuntimeDataToPerson(channelRuntimeData);
		assertEquals("value_one", person.getAttribute("param_one"));
		assertEquals("value_two", person.getAttribute("param_two"));
		
	}
	
	public void testDoesNotMapFormActionParameterToAttribute() {
		ChannelRuntimeData channelRuntimeData = new ChannelRuntimeData();
		channelRuntimeData.setParameter(Constants.FORMACTION, "some_value");
		
		IPerson person = converter.channelRuntimeDataToPerson(channelRuntimeData);
		
		assertNull(person.getAttribute(Constants.FORMACTION));
	}

}
