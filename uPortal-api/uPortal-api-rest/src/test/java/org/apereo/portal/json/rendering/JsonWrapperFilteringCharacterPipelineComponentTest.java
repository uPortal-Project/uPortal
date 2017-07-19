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

import junit.framework.Assert;
import org.apereo.portal.character.stream.CharacterEventReader;
import org.apereo.portal.character.stream.events.CharacterEvent;
import org.apereo.portal.rendering.PipelineComponent;
import org.apereo.portal.rendering.PipelineComponentWrapper;
import org.apereo.portal.rendering.PipelineEventReader;
import org.apereo.portal.rendering.PipelineEventReaderImpl;
import org.apereo.portal.utils.cache.CacheKey;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class JsonWrapperFilteringCharacterPipelineComponentTest {

    JsonWrapperFilteringCharacterPipelineComponent jsonWrapperFilteringCharacterPipelineComponent;
    private MockHttpServletRequest req;
    private MockHttpServletResponse res;
    @Mock private PipelineComponent wrappedComponent;

    @Before
    public void setup() {
        jsonWrapperFilteringCharacterPipelineComponent =
                new JsonWrapperFilteringCharacterPipelineComponent();

        res = new MockHttpServletResponse();
        req = new MockHttpServletRequest();
        wrappedComponent = Mockito.mock(PipelineComponentWrapper.class);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetEventReaderNull() {
        jsonWrapperFilteringCharacterPipelineComponent.setWrappedComponent(null);
        Assert.assertNull(jsonWrapperFilteringCharacterPipelineComponent.getEventReader(req, res));
    }

    @Test
    public void testGetEventReader() {
        PipelineEventReader<CharacterEventReader, CharacterEvent> pipelineEventReader =
                Mockito.mock(PipelineEventReaderImpl.class);
        CharacterEventReader eventReader = Mockito.mock(CharacterEventReader.class);
        Mockito.when(pipelineEventReader.getEventReader()).thenReturn(eventReader);
        Mockito.when(wrappedComponent.getEventReader(req, res)).thenReturn(pipelineEventReader);
        jsonWrapperFilteringCharacterPipelineComponent.setWrappedComponent(wrappedComponent);
        jsonWrapperFilteringCharacterPipelineComponent.getEventReader(req, res);
        Mockito.verify(wrappedComponent).getEventReader(req, res);
    }

    @Test
    public void testGetCacheKeyNull() {
        CacheKey cacheKey = jsonWrapperFilteringCharacterPipelineComponent.getCacheKey(req, res);
        Assert.assertNull(cacheKey);
    }

    @Test
    public void testGetCacheKey() {
        jsonWrapperFilteringCharacterPipelineComponent.setWrappedComponent(wrappedComponent);
        Mockito.when(wrappedComponent.getCacheKey(req, res))
                .thenReturn(Mockito.mock(CacheKey.class));
        CacheKey cacheKey = jsonWrapperFilteringCharacterPipelineComponent.getCacheKey(req, res);
        Assert.assertNotNull(cacheKey);
    }
}
