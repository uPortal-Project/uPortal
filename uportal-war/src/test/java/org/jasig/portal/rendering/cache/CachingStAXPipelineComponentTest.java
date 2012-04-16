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

package org.jasig.portal.rendering.cache;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.XMLEvent;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.jasig.portal.rendering.PipelineEventReader;
import org.jasig.portal.rendering.PipelineEventReaderImpl;
import org.jasig.portal.rendering.StAXPipelineComponent;
import org.jasig.portal.utils.cache.CacheKey;
import org.jasig.portal.xml.stream.XMLEventBufferReader;
import org.jasig.resourceserver.aggr.om.Included;
import org.jasig.resourceserver.utils.aggr.ResourcesElementsProvider;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class CachingStAXPipelineComponentTest {
    @Test
    public void testCacheMiss() {
        final MockHttpServletRequest mockReq = new MockHttpServletRequest();
        final MockHttpServletResponse mockRes = new MockHttpServletResponse();
        final CacheKey cacheKey = CacheKey.build("testCacheKey");
        final List<XMLEvent> eventBuffer = Collections.emptyList();
        final PipelineEventReader<XMLEventReader, XMLEvent> eventReader = new PipelineEventReaderImpl<XMLEventReader, XMLEvent>(new XMLEventBufferReader(eventBuffer.listIterator()));
        
        final Ehcache cache = createMock(Ehcache.class);
        final StAXPipelineComponent targetComponent = createMock(StAXPipelineComponent.class);
        final ResourcesElementsProvider elementsProvider = createMock(ResourcesElementsProvider.class);
        
        expect(elementsProvider.getDefaultIncludedType()).andReturn(Included.AGGREGATED);
        expect(targetComponent.getCacheKey(mockReq, mockRes)).andReturn(cacheKey);
        expect(cache.get(cacheKey)).andReturn(null);
        expect(targetComponent.getEventReader(mockReq, mockRes)).andReturn(eventReader);
        cache.put((Element)notNull());
        expectLastCall();
        
        replay(cache, targetComponent, elementsProvider);
        
        final CachingStAXPipelineComponent cachingComponent = new CachingStAXPipelineComponent();
        cachingComponent.setCache(cache);
        cachingComponent.setWrappedComponent(targetComponent);
        cachingComponent.setResourcesElementsProvider(elementsProvider);
        
        final PipelineEventReader<XMLEventReader, XMLEvent> actualEventReader = cachingComponent.getEventReader(mockReq, mockRes);

        Assert.assertNotNull(actualEventReader);
        Assert.assertNotNull(actualEventReader.getEventReader());
        Assert.assertFalse(actualEventReader.getEventReader().hasNext());
        
        verify(cache, targetComponent, elementsProvider);
    }
    
    @Test
    public void testCacheHit() {
        final MockHttpServletRequest mockReq = new MockHttpServletRequest();
        final MockHttpServletResponse mockRes = new MockHttpServletResponse();
        final CacheKey cacheKey = CacheKey.build("testCacheKey");
        final CachedEventReader<XMLEvent> eventReader = new CachedEventReader<XMLEvent>(Collections.EMPTY_LIST, Collections.EMPTY_MAP);
        final Element cacheElement = new Element(cacheKey, eventReader);
        
        final Ehcache cache = createMock(Ehcache.class);
        final StAXPipelineComponent targetComponent = createMock(StAXPipelineComponent.class);
        final ResourcesElementsProvider elementsProvider = createMock(ResourcesElementsProvider.class);
        
        expect(elementsProvider.getDefaultIncludedType()).andReturn(Included.AGGREGATED);
        expect(targetComponent.getCacheKey(mockReq, mockRes)).andReturn(cacheKey);
        expect(cache.get(cacheKey)).andReturn(cacheElement);
        
        replay(cache, targetComponent, elementsProvider);
        
        final CachingStAXPipelineComponent cachingComponent = new CachingStAXPipelineComponent();
        cachingComponent.setCache(cache);
        cachingComponent.setWrappedComponent(targetComponent);
        cachingComponent.setResourcesElementsProvider(elementsProvider);
        
        final PipelineEventReader<XMLEventReader, XMLEvent> actualEventReader = cachingComponent.getEventReader(mockReq, mockRes);

        Assert.assertNotNull(actualEventReader);
        Assert.assertNotNull(actualEventReader.getEventReader());
        Assert.assertFalse(actualEventReader.getEventReader().hasNext());
        
        verify(cache, targetComponent, elementsProvider);
    }
}
