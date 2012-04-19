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
package org.jasig.portal.portlet.container.cache;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.io.output.NullWriter;
import org.jasig.portal.portlet.rendering.PortletOutputHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test CachingPortletOutputHandler
 * 
 * @author Eric Dalquist
 */
@RunWith(MockitoJUnitRunner.class)
public class CachingPortletOutputHandlerTest {
    @Mock private PortletOutputHandler portletOutputHandler;
    
    @Test
    public void testBasicWriterCaching() throws IOException {
        final CachingPortletOutputHandler cachingOutputHandler = new CachingPortletOutputHandler(portletOutputHandler, 10000);
        
        when(portletOutputHandler.getPrintWriter()).thenReturn(new PrintWriter(NullWriter.NULL_WRITER));
        
        final String output = "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
        
        final PrintWriter printWriter = cachingOutputHandler.getPrintWriter();
        printWriter.write(output);
        
        
        final CachedPortletData<Long> cachedPortletData = cachingOutputHandler.getCachedPortletData(1l, new CacheControlImpl());
        assertNotNull(cachedPortletData);
        assertEquals(output, cachedPortletData.getCachedWriterOutput());
    }
    
    @Test
    public void testTooMuchWriterContent() throws IOException {
        final CachingPortletOutputHandler cachingOutputHandler = new CachingPortletOutputHandler(portletOutputHandler, 100);
        
        when(portletOutputHandler.getPrintWriter()).thenReturn(new PrintWriter(NullWriter.NULL_WRITER));
        
        final String output = "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
        
        final PrintWriter printWriter = cachingOutputHandler.getPrintWriter();
        printWriter.write(output);
        printWriter.write(output);
        
        final CachedPortletData<Long> cachedPortletData = cachingOutputHandler.getCachedPortletData(1l, new CacheControlImpl());
        assertNull(cachedPortletData);
    }
    
    @Test
    public void testTooMuchWriterContentThenReset() throws IOException {
        final CachingPortletOutputHandler cachingOutputHandler = new CachingPortletOutputHandler(portletOutputHandler, 100);
        
        when(portletOutputHandler.getPrintWriter()).thenReturn(new PrintWriter(NullWriter.NULL_WRITER));
        
        final String output = "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
        
        final PrintWriter printWriter = cachingOutputHandler.getPrintWriter();
        printWriter.write(output);
        printWriter.write(output);
        
        //Verify limit was hit
        CachedPortletData<Long> cachedPortletData = cachingOutputHandler.getCachedPortletData(1l, new CacheControlImpl());
        assertNull(cachedPortletData);
        
        cachingOutputHandler.reset();
        
        printWriter.write(output);
        
        //Verify limit reset
        cachedPortletData = cachingOutputHandler.getCachedPortletData(1l, new CacheControlImpl());
        assertNotNull(cachedPortletData);
        assertEquals(output, cachedPortletData.getCachedWriterOutput());
    }
    
    @Test
    public void testBasicStreamCaching() throws IOException {
        final CachingPortletOutputHandler cachingOutputHandler = new CachingPortletOutputHandler(portletOutputHandler, 10000);
        
        when(portletOutputHandler.getOutputStream()).thenReturn(NullOutputStream.NULL_OUTPUT_STREAM);
        
        final String output = "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
        
        final OutputStream outputStream = cachingOutputHandler.getOutputStream();
        outputStream.write(output.getBytes());
        
        
        final CachedPortletData<Long> cachedPortletData = cachingOutputHandler.getCachedPortletData(1l, new CacheControlImpl());
        assertNotNull(cachedPortletData);
        assertArrayEquals(output.getBytes(), cachedPortletData.getCachedStreamOutput());
    }
    
    @Test
    public void testTooMuchStreamContent() throws IOException {
        final CachingPortletOutputHandler cachingOutputHandler = new CachingPortletOutputHandler(portletOutputHandler, 100);
        
        when(portletOutputHandler.getOutputStream()).thenReturn(NullOutputStream.NULL_OUTPUT_STREAM);
        
        final String output = "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";

        final OutputStream outputStream = cachingOutputHandler.getOutputStream();
        outputStream.write(output.getBytes());
        outputStream.write(output.getBytes());
        
        final CachedPortletData<Long> cachedPortletData = cachingOutputHandler.getCachedPortletData(1l, new CacheControlImpl());
        assertNull(cachedPortletData);
    }
    
    @Test
    public void testTooMuchStreamContentThenReset() throws IOException {
        final CachingPortletOutputHandler cachingOutputHandler = new CachingPortletOutputHandler(portletOutputHandler, 100);
        
        when(portletOutputHandler.getOutputStream()).thenReturn(NullOutputStream.NULL_OUTPUT_STREAM);
        
        final String output = "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";

        final OutputStream outputStream = cachingOutputHandler.getOutputStream();
        outputStream.write(output.getBytes());
        outputStream.write(output.getBytes());
        
        CachedPortletData<Long> cachedPortletData = cachingOutputHandler.getCachedPortletData(1l, new CacheControlImpl());
        assertNull(cachedPortletData);
        
        cachingOutputHandler.reset();

        outputStream.write(output.getBytes());
        
        
        cachedPortletData = cachingOutputHandler.getCachedPortletData(1l, new CacheControlImpl());
        assertNotNull(cachedPortletData);
        assertArrayEquals(output.getBytes(), cachedPortletData.getCachedStreamOutput());
    }
}
