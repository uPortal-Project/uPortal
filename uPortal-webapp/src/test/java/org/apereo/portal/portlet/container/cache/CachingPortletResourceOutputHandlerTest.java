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
package org.apereo.portal.portlet.container.cache;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

import org.apereo.portal.portlet.rendering.PortletResourceOutputHandler;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** Test CachingPortletResourceOutputHandler */
@Ignore // Breaks on move to Gradle
@RunWith(MockitoJUnitRunner.class)
public class CachingPortletResourceOutputHandlerTest {
    @Mock private PortletResourceOutputHandler portletResourceOutputHandler;

    @Test
    public void testBasicCaching() {
        final CachingPortletResourceOutputHandler cachingOutputHandler =
                new CachingPortletResourceOutputHandler(portletResourceOutputHandler, 10000);

        final CachedPortletData<Long> cachedPortletData =
                cachingOutputHandler.getCachedPortletData(1l, new CacheControlImpl());
        assertNotNull(cachedPortletData);
    }

    @Test
    public void testBadStatusCode() {
        final CachingPortletResourceOutputHandler cachingOutputHandler =
                new CachingPortletResourceOutputHandler(portletResourceOutputHandler, 10000);

        cachingOutputHandler.setStatus(304);

        final CachedPortletResourceData<Long> cachedPortletResourceData =
                cachingOutputHandler.getCachedPortletResourceData(1l, new CacheControlImpl());
        assertNull(cachedPortletResourceData);
    }
}
