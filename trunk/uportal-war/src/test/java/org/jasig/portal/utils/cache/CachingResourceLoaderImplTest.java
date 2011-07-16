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

package org.jasig.portal.utils.cache;

import static org.easymock.EasyMock.anyLong;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;

import org.apache.commons.io.IOUtils;
import org.jasig.portal.utils.cache.resource.CachedResource;
import org.jasig.portal.utils.cache.resource.CachingResourceLoaderImpl;
import org.jasig.portal.utils.cache.resource.LoadedResource;
import org.jasig.portal.utils.cache.resource.LoadedResourceImpl;
import org.jasig.portal.utils.cache.resource.Loader;
import org.jasig.resourceserver.aggr.om.Included;
import org.jasig.resourceserver.utils.aggr.ResourcesElementsProvider;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class CachingResourceLoaderImplTest {
    private static File doc1;
    
    @BeforeClass
    public static void setupResources() throws Exception {
        final InputStream doc1In = CachingResourceLoaderImplTest.class.getResourceAsStream("CachingResourceLoaderImplTest_doc1.txt");
        doc1 = File.createTempFile("CachingResourceLoaderImplTest_doc1.", ".txt");
        
        final FileOutputStream doc1Out = new FileOutputStream(doc1);
        IOUtils.copy(doc1In, doc1Out);
        IOUtils.closeQuietly(doc1In);
        IOUtils.closeQuietly(doc1Out);
        doc1.deleteOnExit();
        
    }
    
    @Test
    public void testUncachedLoadNoDigest() throws Exception {
        final Resource doc1Resouce = new FileSystemResource(doc1);
        
        final CachingResourceLoaderImpl loader = new CachingResourceLoaderImpl();
        
        final Ehcache cache = createMock(Ehcache.class);
        final ResourcesElementsProvider elementsProvider = createMock(ResourcesElementsProvider.class);
        
        expect(elementsProvider.getDefaultIncludedType()).andReturn(Included.AGGREGATED);
        expect(cache.getInternalContext()).andReturn(null).anyTimes();
        expect(cache.getCacheConfiguration()).andReturn(new CacheConfiguration());
        expect(cache.get(doc1Resouce)).andReturn(null);
        expect(cache.getQuiet(doc1Resouce)).andReturn(null);
        cache.put(anyObject(Element.class));
        expectLastCall();
        
        replay(cache, elementsProvider);
        
        loader.setResourceCache(cache);
        loader.setResourcesElementsProvider(elementsProvider);
        
        final CachedResource<String> cachedResource1 = loader.getResource(doc1Resouce, StringResourceBuilder.INSTANCE);
        
        verify(cache, elementsProvider);
        
        assertNotNull(cachedResource1);
        final String expected = IOUtils.toString(new FileReader(doc1));
        assertEquals(expected, cachedResource1.getCachedResource());
    }
    
    @Test
    public void testUncachedLoad() throws Exception {
        final Resource doc1Resouce = new FileSystemResource(doc1);

        final CachingResourceLoaderImpl loader = new CachingResourceLoaderImpl();
        
        final Ehcache cache = createMock(Ehcache.class);
        final ResourcesElementsProvider elementsProvider = createMock(ResourcesElementsProvider.class);
        
        expect(elementsProvider.getDefaultIncludedType()).andReturn(Included.AGGREGATED);
        expect(cache.getInternalContext()).andReturn(null).anyTimes();
        expect(cache.getCacheConfiguration()).andReturn(new CacheConfiguration());
        expect(cache.get(doc1Resouce)).andReturn(null);
        expect(cache.getQuiet(doc1Resouce)).andReturn(null);
        cache.put(anyObject(Element.class));
        expectLastCall();
        
        replay(cache, elementsProvider);
        
        loader.setResourceCache(cache);
        loader.setResourcesElementsProvider(elementsProvider);
        
        final CachedResource<String> cachedResource1 = loader.getResource(doc1Resouce, StringResourceBuilder.INSTANCE);
        
        verify(cache, elementsProvider);
        
        assertNotNull(cachedResource1);
        final String expected = IOUtils.toString(new FileReader(doc1));
        assertEquals(expected, cachedResource1.getCachedResource());
    }
    
    @Test
    public void testCachedModifiedLoad() throws Exception {
        final Resource doc1Resouce = new FileSystemResource(doc1);

        final CachingResourceLoaderImpl loader = new CachingResourceLoaderImpl();
        
        final Ehcache cache = createMock(Ehcache.class);
        final CachedResource<?> cachedResource = createMock(CachedResource.class);
        final ResourcesElementsProvider elementsProvider = createMock(ResourcesElementsProvider.class);
        
        expect(elementsProvider.getDefaultIncludedType()).andReturn(Included.AGGREGATED);
        expect(cache.getInternalContext()).andReturn(null).anyTimes();
        expect(cache.getCacheConfiguration()).andReturn(new CacheConfiguration());
        expect(cache.get(doc1Resouce))
            .andReturn(new Element(doc1Resouce, cachedResource));
        
        final long lastModified = doc1.lastModified();
        
        expect(cachedResource.getResource()).andReturn(doc1Resouce);
        expect(cachedResource.getLastCheckTime()).andReturn(lastModified - TimeUnit.MINUTES.toMillis(5));
        expect(cachedResource.getLastLoadTime()).andReturn(lastModified - TimeUnit.MINUTES.toMillis(5));
        
        cache.put(anyObject(Element.class));
        expectLastCall();
        
        replay(cache, cachedResource, elementsProvider);
        
        loader.setResourceCache(cache);
        loader.setResourcesElementsProvider(elementsProvider);
        
        final CachedResource<String> cachedResource1 = loader.getResource(doc1Resouce, StringResourceBuilder.INSTANCE);
        
        verify(cache, cachedResource, elementsProvider);
        
        assertNotNull(cachedResource1);
        final String expected = IOUtils.toString(new FileReader(doc1));
        assertEquals(expected, cachedResource1.getCachedResource());
    }
    
    @Test
    public void testCachedNotModified() throws Exception {
        final Resource doc1Resouce = new FileSystemResource(doc1);
        
        final CachingResourceLoaderImpl loader = new CachingResourceLoaderImpl();
        
        final Ehcache cache = createMock(Ehcache.class);
        final CachedResource<?> cachedResource = createMock(CachedResource.class);
        final ResourcesElementsProvider elementsProvider = createMock(ResourcesElementsProvider.class);
        
        expect(elementsProvider.getDefaultIncludedType()).andReturn(Included.AGGREGATED);
        expect(cache.getInternalContext()).andReturn(null).anyTimes();
        expect(cache.getCacheConfiguration()).andReturn(new CacheConfiguration());
        final Element element = new Element("class path resource [CachingResourceLoaderImplTest_doc1.txt]", cachedResource);
        expect(cache.get(doc1Resouce)).andReturn(element);
        
        final long lastModified = doc1.lastModified();
        
        expect(cachedResource.getResource()).andReturn(doc1Resouce);
        expect(cachedResource.getLastCheckTime()).andReturn(0L);
        expect(cachedResource.getLastLoadTime()).andReturn(lastModified +  TimeUnit.MINUTES.toMillis(5));
        expect(cachedResource.getAdditionalResources()).andReturn(Collections.EMPTY_MAP);
        cachedResource.setLastCheckTime(anyLong());
        cache.put(element);
        expectLastCall();
        
        replay(cache, cachedResource, elementsProvider);
        
        loader.setResourceCache(cache);
        loader.setResourcesElementsProvider(elementsProvider);
        
        final CachedResource<String> cachedResource1 = loader.getResource(doc1Resouce, StringResourceBuilder.INSTANCE);
        
        verify(cache, cachedResource, elementsProvider);
        
        assertNotNull(cachedResource1);
        assertTrue(cachedResource1 == cachedResource);
    }
    
    @Test
    public void testCachedWithinInterval() throws Exception {
        final Resource doc1Resouce = new FileSystemResource(doc1);
        
        final CachingResourceLoaderImpl loader = new CachingResourceLoaderImpl();
        
        final Ehcache cache = createMock(Ehcache.class);
        final CachedResource<?> cachedResource = createMock(CachedResource.class);
        final ResourcesElementsProvider elementsProvider = createMock(ResourcesElementsProvider.class);
        
        expect(elementsProvider.getDefaultIncludedType()).andReturn(Included.AGGREGATED);
        expect(cache.getInternalContext()).andReturn(null).anyTimes();
        expect(cache.getCacheConfiguration()).andReturn(new CacheConfiguration());
        expect(cache.get(doc1Resouce))
            .andReturn(new Element(doc1Resouce, cachedResource));
        
        expect(cachedResource.getLastCheckTime()).andReturn(System.currentTimeMillis());
        
        replay(cache, cachedResource, elementsProvider);
        
        loader.setResourceCache(cache);
        loader.setResourcesElementsProvider(elementsProvider);
        
        final CachedResource<String> cachedResource1 = loader.getResource(doc1Resouce, StringResourceBuilder.INSTANCE);
        
        verify(cache, cachedResource, elementsProvider);
        
        assertNotNull(cachedResource1);
        assertTrue(cachedResource1 == cachedResource);
    }
    
    private static class StringResourceBuilder implements Loader<String> {
        public static final StringResourceBuilder INSTANCE = new StringResourceBuilder();

        @Override
        public LoadedResource<String> loadResource(Resource resource) throws IOException {
            final InputStream stream = resource.getInputStream();
            try {
                final String string = IOUtils.toString(stream);
                return new LoadedResourceImpl<String>(string);
            }
            finally {
                IOUtils.closeQuietly(stream);
            }
        }
    }
}
