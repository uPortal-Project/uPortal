/**
 * Copyright (c) 2000-2010, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.rendering.cache;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.XMLEvent;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.easymock.EasyMock;
import org.jasig.portal.rendering.PipelineEventReader;
import org.jasig.portal.rendering.PipelineEventReaderImpl;
import org.jasig.portal.rendering.StAXPipelineComponent;
import org.jasig.portal.rendering.cache.CachingStAXPipelineComponent;
import org.jasig.portal.utils.cache.CacheKey;
import org.jasig.portal.xml.stream.XMLEventBufferReader;
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
        final CacheKey cacheKey = new CacheKey("testCacheKey");
        final List<XMLEvent> eventBuffer = Collections.emptyList();
        final PipelineEventReader<XMLEventReader, XMLEvent> eventReader = new PipelineEventReaderImpl<XMLEventReader, XMLEvent>(new XMLEventBufferReader(eventBuffer.listIterator()));
        
        final Ehcache cache = EasyMock.createMock(Ehcache.class);

        final StAXPipelineComponent targetComponent = EasyMock.createMock(StAXPipelineComponent.class);
        
        EasyMock.expect(targetComponent.getCacheKey(mockReq, mockRes)).andReturn(cacheKey);
        EasyMock.expect(cache.get(cacheKey)).andReturn(null);
        EasyMock.expect(targetComponent.getEventReader(mockReq, mockRes)).andReturn(eventReader);
        cache.put((Element)EasyMock.notNull());
        EasyMock.expectLastCall();
        
        EasyMock.replay(cache, targetComponent);
        
        final CachingStAXPipelineComponent cachingComponent = new CachingStAXPipelineComponent();
        cachingComponent.setCache(cache);
        cachingComponent.setParentComponent(targetComponent);
        
        final PipelineEventReader<XMLEventReader, XMLEvent> actualEventReader = cachingComponent.getEventReader(mockReq, mockRes);

        Assert.assertNotNull(actualEventReader);
        Assert.assertNotNull(actualEventReader.getEventReader());
        Assert.assertFalse(actualEventReader.getEventReader().hasNext());
        
        EasyMock.verify(cache, targetComponent);
    }
    
    @Test
    public void testCacheHit() {
        final MockHttpServletRequest mockReq = new MockHttpServletRequest();
        final MockHttpServletResponse mockRes = new MockHttpServletResponse();
        final CacheKey cacheKey = new CacheKey("testCacheKey");
        final List<XMLEvent> eventBuffer = Collections.emptyList();
        final Element cacheElement = new Element(cacheKey, eventBuffer);
        
        final Ehcache cache = EasyMock.createMock(Ehcache.class);

        final StAXPipelineComponent targetComponent = EasyMock.createMock(StAXPipelineComponent.class);
        
        EasyMock.expect(targetComponent.getCacheKey(mockReq, mockRes)).andReturn(cacheKey);
        EasyMock.expect(cache.get(cacheKey)).andReturn(cacheElement);
        
        EasyMock.replay(cache, targetComponent);
        
        final CachingStAXPipelineComponent cachingComponent = new CachingStAXPipelineComponent();
        cachingComponent.setCache(cache);
        cachingComponent.setParentComponent(targetComponent);
        
        final PipelineEventReader<XMLEventReader, XMLEvent> actualEventReader = cachingComponent.getEventReader(mockReq, mockRes);

        Assert.assertNotNull(actualEventReader);
        Assert.assertNotNull(actualEventReader.getEventReader());
        Assert.assertFalse(actualEventReader.getEventReader().hasNext());
        
        EasyMock.verify(cache, targetComponent);
    }
}
