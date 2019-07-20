/*
 Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 distributed with this work for additional information regarding copyright ownership. Apereo
 licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 this file except in compliance with the License. You may obtain a copy of the License at the
 following location:

 <p>http://www.apache.org/licenses/LICENSE-2.0

 <p>Unless required by applicable law or agreed to in writing, software distributed under the
 License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied. See the License for the specific language governing permissions and
 limitations under the License.
*/
package org.apereo.portal.url;

import static org.apereo.portal.url.MaxInactiveFilter.REFRESH_MINUTES;
import static org.apereo.portal.url.MaxInactiveFilter.SESSION_MAX_INACTIVE_SET_ATTR;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.security.ISecurityContext;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class MaxInactiveFilterTest {

    private static final ZoneId tz = ZoneId.systemDefault();

    @Test
    public void timeSetOutsideRefreshDurationWorkflow() throws IOException, ServletException {
        final HttpSession session = mock(HttpSession.class);
        final HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getSession(false)).thenReturn(session);
        final ServletResponse resp = mock(ServletResponse.class); // no calls, used in doFilter()
        final FilterChain chain = mock(FilterChain.class);

        final ISecurityContext securityContext = mock(ISecurityContext.class);
        when(securityContext.isAuthenticated()).thenReturn(true);
        final IPerson person = mock(IPerson.class);
        when(person.getSecurityContext()).thenReturn(securityContext);
        final LocalDateTime lastTime = LocalDateTime.now(tz).minusMinutes(REFRESH_MINUTES + 2);
        when(person.getAttribute(SESSION_MAX_INACTIVE_SET_ATTR)).thenReturn(lastTime);
        when(person.getAttribute(IPerson.USERNAME)).thenReturn("jsmith");
        final IPersonManager personManager = mock(IPersonManager.class);
        when(personManager.getPerson(req)).thenReturn(person);

        final IMaxInactiveStrategy maxInactiveStrategy = mock(IMaxInactiveStrategy.class);
        when(maxInactiveStrategy.calcMaxInactive(person)).thenReturn(null);

        final MaxInactiveFilter filter = new MaxInactiveFilter();
        ReflectionTestUtils.setField(filter, "personManager", personManager);
        ReflectionTestUtils.setField(filter, "maxInactiveStrategy", maxInactiveStrategy);

        filter.doFilter(req, resp, chain);

        verify(person, times(1))
                .setAttribute(eq(SESSION_MAX_INACTIVE_SET_ATTR), any(LocalDateTime.class));
        verify(maxInactiveStrategy, times(1)).calcMaxInactive(person);
        verify(securityContext, times(1)).isAuthenticated();
        verify(person, times(1)).getSecurityContext();
        verify(person, times(1)).getAttribute(SESSION_MAX_INACTIVE_SET_ATTR);
        verify(person, times(1)).getAttribute(IPerson.USERNAME);
        verify(personManager, times(1)).getPerson(req);
        verifyZeroInteractions(resp);
        verifyZeroInteractions(session);
        verify(chain, only()).doFilter(req, resp);
    }

    @Test
    public void noTimeSetWorkflow() throws IOException, ServletException {
        final HttpSession session = mock(HttpSession.class);
        final HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getSession(false)).thenReturn(session);
        final ServletResponse resp = mock(ServletResponse.class); // no calls, used in doFilter()
        final FilterChain chain = mock(FilterChain.class);

        final ISecurityContext securityContext = mock(ISecurityContext.class);
        when(securityContext.isAuthenticated()).thenReturn(true);
        final IPerson person = mock(IPerson.class);
        when(person.getSecurityContext()).thenReturn(securityContext);
        when(person.getAttribute(SESSION_MAX_INACTIVE_SET_ATTR)).thenReturn(null);
        when(person.getAttribute(IPerson.USERNAME)).thenReturn("jsmith");
        final IPersonManager personManager = mock(IPersonManager.class);
        when(personManager.getPerson(req)).thenReturn(person);

        final IMaxInactiveStrategy maxInactiveStrategy = mock(IMaxInactiveStrategy.class);
        final Integer interval = 5;
        when(maxInactiveStrategy.calcMaxInactive(person)).thenReturn(interval);

        final MaxInactiveFilter filter = new MaxInactiveFilter();
        ReflectionTestUtils.setField(filter, "personManager", personManager);
        ReflectionTestUtils.setField(filter, "maxInactiveStrategy", maxInactiveStrategy);

        filter.doFilter(req, resp, chain);

        verify(person, times(1))
                .setAttribute(eq(SESSION_MAX_INACTIVE_SET_ATTR), any(LocalDateTime.class));
        verify(session, times(1)).setMaxInactiveInterval(interval);
        verify(maxInactiveStrategy, times(1)).calcMaxInactive(person);
        verify(securityContext, times(1)).isAuthenticated();
        verify(person, times(1)).getSecurityContext();
        verify(person, times(1)).getAttribute(SESSION_MAX_INACTIVE_SET_ATTR);
        verify(person, times(2)).getAttribute(IPerson.USERNAME);
        verify(personManager, times(1)).getPerson(req);
        verifyZeroInteractions(resp);
        verify(chain, only()).doFilter(req, resp);
    }

    @Test
    public void timeSetInsideRefreshDurationWorkflow() throws IOException, ServletException {
        final HttpSession session = mock(HttpSession.class);
        final HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getSession(false)).thenReturn(session);
        final ServletResponse resp = mock(ServletResponse.class); // no calls, used in doFilter()
        final FilterChain chain = mock(FilterChain.class);

        final ISecurityContext securityContext = mock(ISecurityContext.class);
        when(securityContext.isAuthenticated()).thenReturn(true);
        final IPerson person = mock(IPerson.class);
        when(person.getSecurityContext()).thenReturn(securityContext);
        final LocalDateTime lastTime = LocalDateTime.now(tz).minusMinutes(1);
        when(person.getAttribute(SESSION_MAX_INACTIVE_SET_ATTR)).thenReturn(lastTime);
        when(person.getAttribute(IPerson.USERNAME)).thenReturn("jsmith");
        final IPersonManager personManager = mock(IPersonManager.class);
        when(personManager.getPerson(req)).thenReturn(person);

        final IMaxInactiveStrategy maxInactiveStrategy = mock(IMaxInactiveStrategy.class);

        final MaxInactiveFilter filter = new MaxInactiveFilter();
        ReflectionTestUtils.setField(filter, "personManager", personManager);
        ReflectionTestUtils.setField(filter, "maxInactiveStrategy", maxInactiveStrategy);

        filter.doFilter(req, resp, chain);

        verify(securityContext, times(1)).isAuthenticated();
        verify(person, times(1)).getSecurityContext();
        verify(person, times(1)).getAttribute(SESSION_MAX_INACTIVE_SET_ATTR);
        verify(person, times(1)).getAttribute(IPerson.USERNAME);
        verify(personManager, times(1)).getPerson(req);
        verifyZeroInteractions(maxInactiveStrategy);
        verifyZeroInteractions(resp);
        verifyZeroInteractions(session);
        verify(chain, only()).doFilter(req, resp);
    }

    @Test
    public void notAuthenticatedWorkflow() throws IOException, ServletException {
        final HttpSession session = mock(HttpSession.class);
        final HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getSession(false)).thenReturn(session);
        final ServletResponse resp = mock(ServletResponse.class); // no calls, used in doFilter()
        final FilterChain chain = mock(FilterChain.class);

        final ISecurityContext securityContext = mock(ISecurityContext.class);
        when(securityContext.isAuthenticated()).thenReturn(false);
        final IPerson person = mock(IPerson.class);
        when(person.getSecurityContext()).thenReturn(securityContext);
        when(person.getAttribute(IPerson.USERNAME)).thenReturn("jsmith");
        final IPersonManager personManager = mock(IPersonManager.class);
        when(personManager.getPerson(req)).thenReturn(person);

        final IMaxInactiveStrategy maxInactiveStrategy = mock(IMaxInactiveStrategy.class);

        final MaxInactiveFilter filter = new MaxInactiveFilter();
        ReflectionTestUtils.setField(filter, "personManager", personManager);
        ReflectionTestUtils.setField(filter, "maxInactiveStrategy", maxInactiveStrategy);

        filter.doFilter(req, resp, chain);

        verify(securityContext, times(1)).isAuthenticated();
        verify(person, times(1)).getSecurityContext();
        verify(person, times(1)).getAttribute(IPerson.USERNAME);
        verify(personManager, times(1)).getPerson(req);
        verifyZeroInteractions(maxInactiveStrategy);
        verifyZeroInteractions(resp);
        verifyZeroInteractions(session);
        verify(chain, only()).doFilter(req, resp);
    }

    @Test
    public void noSecurityContextWorkflow() throws IOException, ServletException {
        final HttpSession session = mock(HttpSession.class);
        final HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getSession(false)).thenReturn(session);
        final ServletResponse resp = mock(ServletResponse.class); // no calls, used in doFilter()
        final FilterChain chain = mock(FilterChain.class);

        final IPerson person = mock(IPerson.class);
        when(person.getSecurityContext()).thenReturn(null);
        when(person.getAttribute(IPerson.USERNAME)).thenReturn("jsmith");
        final IPersonManager personManager = mock(IPersonManager.class);
        when(personManager.getPerson(req)).thenReturn(person);

        final IMaxInactiveStrategy maxInactiveStrategy = mock(IMaxInactiveStrategy.class);

        final MaxInactiveFilter filter = new MaxInactiveFilter();
        ReflectionTestUtils.setField(filter, "personManager", personManager);
        ReflectionTestUtils.setField(filter, "maxInactiveStrategy", maxInactiveStrategy);

        filter.doFilter(req, resp, chain);

        verify(person, times(1)).getSecurityContext();
        verify(person, times(1)).getAttribute(IPerson.USERNAME);
        verify(personManager, times(1)).getPerson(req);
        verifyZeroInteractions(maxInactiveStrategy);
        verifyZeroInteractions(resp);
        verifyZeroInteractions(session);
        verify(chain, only()).doFilter(req, resp);
    }

    @Test
    public void noPersonWorkflow() throws IOException, ServletException {
        final HttpSession session = mock(HttpSession.class);
        final HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getSession(false)).thenReturn(session);
        final ServletResponse resp = mock(ServletResponse.class); // no calls, used in doFilter()
        final FilterChain chain = mock(FilterChain.class);

        final IPersonManager personManager = mock(IPersonManager.class);
        when(personManager.getPerson(req)).thenReturn(null);

        final IMaxInactiveStrategy maxInactiveStrategy = mock(IMaxInactiveStrategy.class);

        final MaxInactiveFilter filter = new MaxInactiveFilter();
        ReflectionTestUtils.setField(filter, "personManager", personManager);
        ReflectionTestUtils.setField(filter, "maxInactiveStrategy", maxInactiveStrategy);

        filter.doFilter(req, resp, chain);

        verify(personManager, times(1)).getPerson(req);
        verifyZeroInteractions(maxInactiveStrategy);
        verifyZeroInteractions(resp);
        verifyZeroInteractions(session);
        verify(chain, only()).doFilter(req, resp);
    }

    @Test
    public void noSessionWorkflow() throws IOException, ServletException {
        final HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getSession()).thenReturn(null);
        final ServletResponse resp = mock(ServletResponse.class); // no calls, used in doFilter()
        final FilterChain chain = mock(FilterChain.class);

        final IPersonManager personManager = mock(IPersonManager.class);

        final IMaxInactiveStrategy maxInactiveStrategy = mock(IMaxInactiveStrategy.class);

        final MaxInactiveFilter filter = new MaxInactiveFilter();
        ReflectionTestUtils.setField(filter, "personManager", personManager);
        ReflectionTestUtils.setField(filter, "maxInactiveStrategy", maxInactiveStrategy);

        filter.doFilter(req, resp, chain);

        verifyZeroInteractions(personManager);
        verifyZeroInteractions(maxInactiveStrategy);
        verifyZeroInteractions(resp);
        verify(req, only()).getSession(false);
        verify(chain, only()).doFilter(req, resp);
    }
}
