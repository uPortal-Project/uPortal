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
package org.jasig.portal.api.sso;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import org.jasig.portal.portlets.lookup.IPersonAttributesFilter;
import org.jasig.portal.portlets.lookup.IPersonLookupHelper;
import org.jasig.portal.security.SystemPerson;
import org.jasig.services.persondir.IPersonAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@DependsOn("ssoPersonSearchResultFilter")
public class ApiSsoPersonLookupService implements SsoPersonLookupService {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	// Use the "environment.build.sso" prefix rather than "org.jasig.portal.security.sso"
	// b/c the former is set in ssp-platform-config.properties and the latter in portal.properties.
	// Normally we'd just go ahead and add the necessary properties to portal.properties but in this
	// case we need to let people set aribitrary properties below the given prefix. So if we
	// used the portal.properties prefix we'd either force people to fork that file or break from
	// the established naming conventionin ssp-platform-config.properties.
	public static final String CONFIG_PREFIX = "environment.build.sso";

	@Autowired
	private IPersonLookupHelper personLookupHelper;

	@Autowired
	@Qualifier("ssoPersonSearchResultFilter")
	private IPersonAttributesFilter ssoPersonSearchResultFilter;

	private ObjectMapper jsonMapper = new ObjectMapper();

	private JavaType personAsMapType = jsonMapper.getTypeFactory().constructParametricType(Map.class, String.class, Object.class);

	private JavaType personsAsListOfMapsType = jsonMapper.getTypeFactory().constructParametricType(List.class, personAsMapType);

	@Override
	public Map<String, Object> findSsoPersonByUsername(String username) {
		final IPersonAttributes person = personLookupHelper.findPerson(
				SystemPerson.INSTANCE, username);
		final IPersonAttributes filteredPerson = filterPersonSearchResults(person);
		log.debug("Search by username [{}] found [{}] matches before filtering. After filtering: [{}].",
				new Object[] { username, (person == null ? 0 : 1), (filteredPerson == null ? 0 : 1)});
		return filteredPerson == null ? null : toCollection(filteredPerson);
	}

	@Override
	public List<Map<String, Object>> findSsoPerson(String attribute, String value) {
		final Map<String, Object> query = personSearchQuery(attribute, value);
		final List<IPersonAttributes> persons = personLookupHelper
				.searchForPeople(SystemPerson.INSTANCE, query);
		final List<IPersonAttributes> filteredPersons = filterPersonSearchResults(persons);
		log.debug("Search by query [{}] found [{}] matches before filtering. After filtering: [{}].",
				new Object[] { query, (persons == null ? 0 : persons.size()),
						(filteredPersons == null ? 0 : filteredPersons.size())});
		if ( filteredPersons == null || filteredPersons.isEmpty() ) {
			return new ArrayList<Map<String, Object>>();
		}
		return toCollection(filteredPersons);
	}

	private Map<String, Object> personSearchQuery(String attribute, String value) {
		final Map<String, Object> query = new HashMap<String, Object>();
		query.put(attribute, value);
		return query;
	}

	private IPersonAttributes filterPersonSearchResults(IPersonAttributes person) {
		if ( person == null || ssoPersonSearchResultFilter == null ) {
			return person;
		}
		final List<IPersonAttributes> filteredPersons = filterPersonSearchResults(Arrays.asList(person));
		return (filteredPersons == null || filteredPersons.isEmpty()) ? null : filteredPersons.get(0);
	}

	private List<IPersonAttributes> filterPersonSearchResults(List<IPersonAttributes> persons) {
		if ( persons == null || persons.isEmpty() || ssoPersonSearchResultFilter == null ) {
			return persons;
		}
		return ssoPersonSearchResultFilter.filter(persons,CONFIG_PREFIX);
	}

	private Map<String, Object> toCollection(IPersonAttributes filteredPerson) {
		return toCollection(filteredPerson, personAsMapType);
	}

	private List<Map<String, Object>> toCollection(List<IPersonAttributes> filteredPerson) {
		return toCollection(filteredPerson, personsAsListOfMapsType);
	}

	private <T> T toCollection(Object orig, JavaType targetType) {
		try {
			// Terrible, yes. But in theory the JSON de/serialization ensures we get the same Map-based
			// representation as you'd get from deserializing the result of PeopleRESTController GET
			// methods (which is exactly what SSP clients need)
			final String asString = jsonMapper.writeValueAsString(orig);
			return jsonMapper.readValue(asString, targetType);
		} catch ( IOException e ) {
			throw new RuntimeException("Failed to serialize a person record", e);
		}
	}

}
