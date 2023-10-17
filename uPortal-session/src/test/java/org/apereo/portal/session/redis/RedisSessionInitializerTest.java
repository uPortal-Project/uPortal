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
package org.apereo.portal.session.redis;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class RedisSessionInitializerTest {

    private static final String EXPECTED_FILTER_NAME = "springSessionRepositoryFilter";

    private RedisSessionInitializer redisSessionInitializer;

    @Mock private ServletContext servletContext;
    @Mock private Dynamic FilterRegistration;

    @Rule public final EnvironmentVariables environmentVariables = new EnvironmentVariables();
    @Rule public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(this.servletContext.addFilter(anyString(), any(Filter.class)))
                .thenReturn(this.FilterRegistration);
        this.redisSessionInitializer = new RedisSessionInitializer();
    }

    @Test
    public void testOnStartupAddsFilterWhenStoreTypeSystemPropertySetToRedis() throws Exception {
        System.setProperty("org.apereo.portal.session.storetype", "redis");
        this.redisSessionInitializer.onStartup(this.servletContext);
        verify(this.servletContext, times(1))
                .addFilter(eq(EXPECTED_FILTER_NAME), any(Filter.class));
    }

    @Test
    public void
            testOnStartupDoesNotAddFilterWhenStoreTypeEnvironmentVariableAndSystemPropertyNotFound()
                    throws Exception {
        this.redisSessionInitializer.onStartup(this.servletContext);
        verify(this.servletContext, never()).addFilter(anyString(), any(Filter.class));
    }

    @Test
    public void testOnStartupDoesNotAddFilterWhenStoreTypeSystemPropertyValueIsNotRedis()
            throws Exception {
        System.setProperty("org.apereo.portal.session.storetype", "rdbms");
        this.redisSessionInitializer.onStartup(this.servletContext);
        verify(this.servletContext, never()).addFilter(anyString(), any(Filter.class));
    }

    @Test
    public void testOnStartupAddsFilterWhenStoreTypeEnvironmentVariableSetToRedis()
            throws Exception {
        this.environmentVariables.set("ORG_APEREO_PORTAL_SESSION_STORETYPE", "redis");
        this.redisSessionInitializer.onStartup(this.servletContext);
        verify(this.servletContext, times(1))
                .addFilter(eq(EXPECTED_FILTER_NAME), any(Filter.class));
    }

    @Test
    public void testOnStartupDoesNotAddFilterWhenStoreTypeEnvironmentVariableValueIsNotRedis()
            throws Exception {
        this.environmentVariables.set("ORG_APEREO_PORTAL_SESSION_STORETYPE", "foo");
        this.redisSessionInitializer.onStartup(this.servletContext);
        verify(this.servletContext, never()).addFilter(anyString(), any(Filter.class));
    }

    @Test
    public void testOnStartupHasSystemPropertyTakePresidenceOverEnvironmentVariable()
            throws Exception {
        this.environmentVariables.set("ORG_APEREO_PORTAL_SESSION_STORETYPE", "redis");
        System.setProperty("org.apereo.portal.session.storetype", "foo");
        this.redisSessionInitializer.onStartup(this.servletContext);
        verify(this.servletContext, never()).addFilter(anyString(), any(Filter.class));
    }
}
