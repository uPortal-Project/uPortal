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
/**
 * 
 */
package org.jasig.portal.layout.profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.provider.PersonImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author GIP RECIA 2013 - Maxime BOSSARD.
 *
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class ServerNameGuestChainingProfileMapperTest {

	private Map<String, String> configMap = new HashMap<String, String>();
	{
		configMap.put("test-lycee.portail.ent", "lycees");
		configMap.put("test-cfa.portail.ent", "cfa");
		configMap.put("test.college.ent", "clg37");
	}
	
	@Test
	public void testGuestAuthorizedDomain() throws Exception {
		ServerNameGuestChainingProfileMapper profileMapper = new ServerNameGuestChainingProfileMapper();
		profileMapper.setAuthorizedServerNames(configMap);
		profileMapper.afterPropertiesSet();
		
		final IPerson person = createGuestPerson();
		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.setServerName("test-cfa.portail.ent");
		
		final String fname = profileMapper.getProfileFname(person, request);
		
		Assert.assertEquals("guest-cfa-default", fname);
	}
	
	@Test
	public void testGuestNonAuthorizedDomain() throws Exception {
		ServerNameGuestChainingProfileMapper profileMapper = new ServerNameGuestChainingProfileMapper();
		profileMapper.setAuthorizedServerNames(configMap);
		profileMapper.afterPropertiesSet();
		
		final IPerson person = createGuestPerson();
		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.setServerName("domain.test");
		
		final String fname = profileMapper.getProfileFname(person, request);
		
		Assert.assertEquals("guest-default", fname);
	}
	
	@Test
	public void testNonGuestAuthorizedDomain() throws Exception {
		ServerNameGuestChainingProfileMapper profileMapper = new ServerNameGuestChainingProfileMapper();
		profileMapper.setAuthorizedServerNames(configMap);
		profileMapper.afterPropertiesSet();
		
		final IPerson person = createPerson();
		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.setServerName("test-cfa.portail.ent");
		
		final String fname = profileMapper.getProfileFname(person, request);
		
		Assert.assertEquals("default", fname);
	}
	
	@Test
	public void testNonGuestNonAuthorizedDomain() throws Exception {
		ServerNameGuestChainingProfileMapper profileMapper = new ServerNameGuestChainingProfileMapper();
		profileMapper.setAuthorizedServerNames(configMap);
		profileMapper.afterPropertiesSet();
		
		final IPerson person = createPerson();
		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.setServerName("domain.test");
		
		final String fname = profileMapper.getProfileFname(person, request);
		
		Assert.assertEquals("default", fname);
	}
	
	@Test
	public void testNonGuestWithSubMapperMatching() throws Exception {
		ServerNameGuestChainingProfileMapper profileMapper = new ServerNameGuestChainingProfileMapper();
		profileMapper.setAuthorizedServerNames(configMap);
		List<IProfileMapper> subMappers = new ArrayList<IProfileMapper>();
		SessionAttributeProfileMapperImpl subMapper = new SessionAttributeProfileMapperImpl();
		subMappers.add(subMapper);
		subMapper.setDefaultProfileName("submapper_default");
		subMapper.setAttributeName("session_attr");
		Map<String, String> mapping = new HashMap<String, String>();
		mapping.put("session_attr", "sub_fname");
		subMapper.setMappings(mapping);
		profileMapper.setSubMappers(subMappers);
		profileMapper.afterPropertiesSet();
		
		final IPerson person = createPerson();
		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.setServerName("domain.test");
		request.getSession().setAttribute("session_attr", "session_attr");
		
		final String fname = profileMapper.getProfileFname(person, request);
		
		Assert.assertEquals("sub_fname", fname);
	}
	
	@Test
	public void testNonGuestWithSubMapperNotMatching() throws Exception {
		ServerNameGuestChainingProfileMapper profileMapper = new ServerNameGuestChainingProfileMapper();
		profileMapper.setAuthorizedServerNames(configMap);
		List<IProfileMapper> subMappers = new ArrayList<IProfileMapper>();
		SessionAttributeProfileMapperImpl subMapper = new SessionAttributeProfileMapperImpl();
		subMappers.add(subMapper);
		subMapper.setDefaultProfileName("submapper_default");
		subMapper.setAttributeName("session_attr");
		Map<String, String> mapping = new HashMap<String, String>();
		mapping.put("session_attr", "sub_fname");
		subMapper.setMappings(mapping);
		profileMapper.setSubMappers(subMappers);
		profileMapper.afterPropertiesSet();
		
		final IPerson person = createPerson();
		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.setServerName("domain.test");
		request.getSession().setAttribute("other_session_attr", "session_attr");
		
		final String fname = profileMapper.getProfileFname(person, request);
		
		Assert.assertEquals("submapper_default", fname);
	}
	
	@Test
	public void testGuestNonAuthorizedDomainWithSubMapperMatching() throws Exception {
		ServerNameGuestChainingProfileMapper profileMapper = new ServerNameGuestChainingProfileMapper();
		profileMapper.setAuthorizedServerNames(configMap);
		List<IProfileMapper> subMappers = new ArrayList<IProfileMapper>();
		SessionAttributeProfileMapperImpl subMapper = new SessionAttributeProfileMapperImpl();
		subMappers.add(subMapper);
		subMapper.setDefaultProfileName("submapper_default");
		subMapper.setAttributeName("session_attr");
		Map<String, String> mapping = new HashMap<String, String>();
		mapping.put("session_attr", "sub_fname");
		subMapper.setMappings(mapping);
		profileMapper.setSubMappers(subMappers);
		profileMapper.afterPropertiesSet();
		
		final IPerson person = createGuestPerson();
		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.setServerName("domain.test");
		request.getSession().setAttribute("session_attr", "session_attr");
		
		final String fname = profileMapper.getProfileFname(person, request);
		
		Assert.assertEquals("guest-sub_fname", fname);
	}
	
	@Test
	public void testGuestAuthorizedDomainWithSubMapperMatching() throws Exception {
		ServerNameGuestChainingProfileMapper profileMapper = new ServerNameGuestChainingProfileMapper();
		profileMapper.setAuthorizedServerNames(configMap);
		List<IProfileMapper> subMappers = new ArrayList<IProfileMapper>();
		SessionAttributeProfileMapperImpl subMapper = new SessionAttributeProfileMapperImpl();
		subMappers.add(subMapper);
		subMapper.setDefaultProfileName("submapper_default");
		subMapper.setAttributeName("session_attr");
		Map<String, String> mapping = new HashMap<String, String>();
		mapping.put("session_attr", "sub_fname");
		subMapper.setMappings(mapping);
		profileMapper.setSubMappers(subMappers);
		profileMapper.afterPropertiesSet();
		
		final IPerson person = createGuestPerson();
		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.setServerName("test.college.ent");
		request.getSession().setAttribute("session_attr", "session_attr");
		
		final String fname = profileMapper.getProfileFname(person, request);
		
		Assert.assertEquals("guest-clg37-sub_fname", fname);
	}
	
	@Test
	public void testGuestAuthorizedDomainWithSubMapperNotMatchingWithSubDefault() throws Exception {
		ServerNameGuestChainingProfileMapper profileMapper = new ServerNameGuestChainingProfileMapper();
		profileMapper.setAuthorizedServerNames(configMap);
		List<IProfileMapper> subMappers = new ArrayList<IProfileMapper>();
		SessionAttributeProfileMapperImpl subMapper = new SessionAttributeProfileMapperImpl();
		subMappers.add(subMapper);
		subMapper.setDefaultProfileName("submapper_default");
		subMapper.setAttributeName("session_attr");
		Map<String, String> mapping = new HashMap<String, String>();
		mapping.put("session_attr", "sub_fname");
		subMapper.setMappings(mapping);
		profileMapper.setSubMappers(subMappers);
		profileMapper.afterPropertiesSet();
		
		final IPerson person = createGuestPerson();
		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.setServerName("test.college.ent");
		request.getSession().setAttribute("other_session_attr", "session_attr");
		
		final String fname = profileMapper.getProfileFname(person, request);
		
		Assert.assertEquals("guest-clg37-submapper_default", fname);
	}
	
	@Test
	public void testGuestAuthorizedDomainWithSubMapperNotMatchingWithoutSubDefault() throws Exception {
		ServerNameGuestChainingProfileMapper profileMapper = new ServerNameGuestChainingProfileMapper();
		profileMapper.setAuthorizedServerNames(configMap);
		List<IProfileMapper> subMappers = new ArrayList<IProfileMapper>();
		SessionAttributeProfileMapperImpl subMapper = new SessionAttributeProfileMapperImpl();
		subMappers.add(subMapper);
		subMapper.setDefaultProfileName("submapper_default");
		subMapper.setAttributeName("session_attr");
		subMapper.setDefaultProfileName("");
		Map<String, String> mapping = new HashMap<String, String>();
		mapping.put("session_attr", "sub_fname");
		subMapper.setMappings(mapping);
		profileMapper.setSubMappers(subMappers);
		profileMapper.afterPropertiesSet();
		
		final IPerson person = createGuestPerson();
		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.setServerName("test.college.ent");
		request.getSession().setAttribute("other_session_attr", "session_attr");
		
		final String fname = profileMapper.getProfileFname(person, request);
		
		Assert.assertEquals("guest-clg37-default", fname);
	}
	
	protected static IPerson createGuestPerson() throws Exception {
        IPerson person = new PersonImpl();
        person.setAttribute(IPerson.USERNAME, "guest");
        
        return person;
	}
	
	protected static IPerson createPerson() throws Exception {
        IPerson person = new PersonImpl();
        person.setAttribute(IPerson.USERNAME, "non_guest");
        
        return person;
	}
}
