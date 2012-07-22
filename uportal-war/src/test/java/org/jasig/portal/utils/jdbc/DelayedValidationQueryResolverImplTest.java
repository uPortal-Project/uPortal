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
package org.jasig.portal.utils.jdbc;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.dialect.MySQL5InnoDBDialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.Oracle10gDialect;
import org.hibernate.dialect.Oracle8iDialect;
import org.hibernate.dialect.Oracle9iDialect;
import org.jasig.portal.utils.PostgreSQL81Dialect;
import org.jasig.portal.utils.jdbc.DelayedValidationQueryResolverImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableMap;

@RunWith(MockitoJUnitRunner.class)
public class DelayedValidationQueryResolverImplTest {
	@InjectMocks private DelayedValidationQueryResolverImpl validationQueryResolver = new DelayedValidationQueryResolverImpl();
	
	@Test
	public void testQueryResolution() {
		validationQueryResolver.setValidationQueryMap(ImmutableMap.<Class<? extends Dialect>, String>of(
				Oracle8iDialect.class, "SELECT 1 FROM DUAL",
				HSQLDialect.class, "select 1 from INFORMATION_SCHEMA.SYSTEM_USERS",
				MySQLDialect.class, "select 1"));
		
		String vq = validationQueryResolver.resolveValidationQuery(Oracle8iDialect.class);
		assertEquals("SELECT 1 FROM DUAL", vq);
		vq = validationQueryResolver.resolveValidationQuery(Oracle9iDialect.class);
		assertEquals("SELECT 1 FROM DUAL", vq);
		vq = validationQueryResolver.resolveValidationQuery(Oracle10gDialect.class);
		assertEquals("SELECT 1 FROM DUAL", vq);
		
		vq = validationQueryResolver.resolveValidationQuery(PostgreSQL81Dialect.class);
		assertNull(vq);
		
		vq = validationQueryResolver.resolveValidationQuery(MySQL5InnoDBDialect.class);
		assertEquals("select 1", vq);
	}
}
