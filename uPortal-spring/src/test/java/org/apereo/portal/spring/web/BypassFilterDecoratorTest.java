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
package org.apereo.portal.spring.web;

import static org.mockito.Mockito.*;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class BypassFilterDecoratorTest {

    @Mock private FilterChain chain;
    @Mock private FilterConfig filterConfig;
    @Mock private ServletRequest request;
    @Mock private ServletResponse response;
    @Mock private Filter filter;
    @Mock private Filter bypassedFilter;

    @Rule public final MockitoRule mockitoRule = MockitoJUnit.rule();
    @Rule public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testNoSettings() throws Exception {
        BypassFilterDecorator decorator = new BypassFilterDecorator();
        exception.expect(ServletException.class);
        decorator.init(filterConfig);
        exception.expect(ServletException.class);
        decorator.doFilter(request, response, chain);
        decorator.destroy();
    }

    @Test
    public void testDefaultBypass() throws Exception {
        BypassFilterDecorator decorator = new BypassFilterDecorator();
        decorator.setWrappedFilter(filter);
        runDecorator(decorator);
        verify(filter, times(1)).init(filterConfig);
        verify(filter, times(1)).doFilter(request, response, chain);
        verify(filter, times(1)).destroy();
    }

    @Test
    public void testFilterCalled() throws Exception {
        BypassFilterDecorator decorator = new BypassFilterDecorator();
        decorator.setBypass(false);
        decorator.setWrappedFilter(filter);
        runDecorator(decorator);
        verify(filter, times(1)).init(filterConfig);
        verify(filter, times(1)).doFilter(request, response, chain);
        verify(filter, times(1)).destroy();
    }

    @Test
    public void testFilterBypassed() throws Exception {
        BypassFilterDecorator decorator = new BypassFilterDecorator();
        decorator.setBypass(true);
        decorator.setWrappedFilter(bypassedFilter);
        runDecorator(decorator);
        verify(bypassedFilter, times(0)).init(filterConfig);
        verify(bypassedFilter, times(0)).doFilter(request, response, chain);
        verify(bypassedFilter, times(0)).destroy();
    }

    private void runDecorator(BypassFilterDecorator decorator) throws Exception {
        decorator.init(filterConfig);
        decorator.doFilter(request, response, chain);
        decorator.destroy();
    }
}
