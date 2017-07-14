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
package org.apereo.portal.json.rendering;

import org.apereo.portal.utils.cache.CacheKey;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class JsonWrapperFilteringCharacterPipelineComponentTest {

    JsonWrapperFilteringCharacterPipelineComponent jsonWrapperFilteringCharacterPipelineComponent;
    private MockHttpServletRequest req;
    private MockHttpServletResponse res;

    @Before
    public void setup() {
        jsonWrapperFilteringCharacterPipelineComponent =
                new JsonWrapperFilteringCharacterPipelineComponent();

        res = new MockHttpServletResponse();
        req = new MockHttpServletRequest();
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = NullPointerException.class)
    public void testGetEventReaderNull() {
        jsonWrapperFilteringCharacterPipelineComponent.setWrappedComponent(null);
        jsonWrapperFilteringCharacterPipelineComponent.getEventReader(req, res);
    }

    @Test(expected = NullPointerException.class)
    public void testGetCacheKey() {
        CacheKey cacheKey = jsonWrapperFilteringCharacterPipelineComponent.getCacheKey(req, res);
        Assert.assertNull(cacheKey);
    }
}
