/**
 * Copyright (c) 2000-2010, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.rendering.cache;

import java.util.Collections;
import java.util.List;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.easymock.EasyMock;
import org.jasig.portal.character.stream.CharacterEventBufferReader;
import org.jasig.portal.character.stream.CharacterEventReader;
import org.jasig.portal.character.stream.events.CharacterEvent;
import org.jasig.portal.rendering.CacheableEventReader;
import org.jasig.portal.rendering.CacheableEventReaderImpl;
import org.jasig.portal.rendering.CharacterPipelineComponent;
import org.jasig.portal.rendering.cache.CachingCharacterPipelineComponent;
import org.jasig.portal.utils.cache.CacheKey;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class CachingCharacterPipelineComponentTest {
    @Test
    public void testCacheMiss() {
        final MockHttpServletRequest mockReq = new MockHttpServletRequest();
        final MockHttpServletResponse mockRes = new MockHttpServletResponse();
        final CacheKey cacheKey = new CacheKey("testCacheKey");
        final List<CharacterEvent> eventBuffer = Collections.emptyList();
        final CacheableEventReader<CharacterEventReader, CharacterEvent> eventReader = new CacheableEventReaderImpl<CharacterEventReader, CharacterEvent>(cacheKey, new CharacterEventBufferReader(eventBuffer.listIterator()));
        
        final Ehcache cache = EasyMock.createMock(Ehcache.class);

        final CharacterPipelineComponent targetComponent = EasyMock.createMock(CharacterPipelineComponent.class);
        
        EasyMock.expect(targetComponent.getCacheKey(mockReq, mockRes)).andReturn(cacheKey);
        EasyMock.expect(cache.get(cacheKey)).andReturn(null);
        EasyMock.expect(targetComponent.getEventReader(mockReq, mockRes)).andReturn(eventReader);
        cache.put((Element)EasyMock.notNull());
        EasyMock.expectLastCall();
        
        EasyMock.replay(cache, targetComponent);
        
        final CachingCharacterPipelineComponent cachingComponent = new CachingCharacterPipelineComponent();
        cachingComponent.setCache(cache);
        cachingComponent.setParentComponent(targetComponent);
        
        final CacheableEventReader<CharacterEventReader, CharacterEvent> actualEventReader = cachingComponent.getEventReader(mockReq, mockRes);

        Assert.assertNotNull(actualEventReader);
        Assert.assertEquals(cacheKey, actualEventReader.getCacheKey());
        Assert.assertNotNull(actualEventReader.getEventReader());
        Assert.assertFalse(actualEventReader.getEventReader().hasNext());
        
        EasyMock.verify(cache, targetComponent);
    }
    
    @Test
    public void testCacheHit() {
        final MockHttpServletRequest mockReq = new MockHttpServletRequest();
        final MockHttpServletResponse mockRes = new MockHttpServletResponse();
        final CacheKey cacheKey = new CacheKey("testCacheKey");
        final List<CharacterEvent> eventBuffer = Collections.emptyList();
        final Element cacheElement = new Element(cacheKey, eventBuffer);
        
        final Ehcache cache = EasyMock.createMock(Ehcache.class);

        final CharacterPipelineComponent targetComponent = EasyMock.createMock(CharacterPipelineComponent.class);
        
        EasyMock.expect(targetComponent.getCacheKey(mockReq, mockRes)).andReturn(cacheKey);
        EasyMock.expect(cache.get(cacheKey)).andReturn(cacheElement);
        
        EasyMock.replay(cache, targetComponent);
        
        final CachingCharacterPipelineComponent cachingComponent = new CachingCharacterPipelineComponent();
        cachingComponent.setCache(cache);
        cachingComponent.setParentComponent(targetComponent);
        
        final CacheableEventReader<CharacterEventReader, CharacterEvent> actualEventReader = cachingComponent.getEventReader(mockReq, mockRes);

        Assert.assertNotNull(actualEventReader);
        Assert.assertEquals(cacheKey, actualEventReader.getCacheKey());
        Assert.assertNotNull(actualEventReader.getEventReader());
        Assert.assertFalse(actualEventReader.getEventReader().hasNext());
        
        EasyMock.verify(cache, targetComponent);
    }
}
