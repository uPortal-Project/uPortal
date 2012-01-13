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

package org.jasig.portal.concurrency.caching;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import org.jasig.portal.url.IPortalRequestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:requestCacheAspectTestContext.xml")
public class RequestCacheAspectTest {
    @Autowired
    private IPortalRequestUtils portalRequestUtils;
    @Autowired
    private CacheTestInterface cacheTestInterface;
    
    @Before
    public void setup() {
        reset(portalRequestUtils);
        cacheTestInterface.reset();
    }

    @Test
    public void testNoRequestCache() {
        assertEquals(0, cacheTestInterface.testMethodNoCacheCount());
        
        String result = cacheTestInterface.testMethodNoCache("1", false, false);
        assertEquals("testMethodNoCache(1)", result);
        assertEquals(1, cacheTestInterface.testMethodNoCacheCount());
        
        result = cacheTestInterface.testMethodNoCache("1", true, false);
        assertNull(result);
        assertEquals(2, cacheTestInterface.testMethodNoCacheCount());
        
        result = cacheTestInterface.testMethodNoCache("1", true, false);
        assertNull(result);
        assertEquals(3, cacheTestInterface.testMethodNoCacheCount());
        
        try {
            result = cacheTestInterface.testMethodNoCache("1", false, true);
            fail();
        }
        catch (Throwable t) {
            //expected
        }
        assertEquals(4, cacheTestInterface.testMethodNoCacheCount());
        
        try {
            result = cacheTestInterface.testMethodNoCache("1", false, true);
            fail();
        }
        catch (Throwable t) {
            //expected
        }
        assertEquals(5, cacheTestInterface.testMethodNoCacheCount());
        
        result = cacheTestInterface.testMethodNoCache("1", false, false);
        assertEquals("testMethodNoCache(1)", result);
        assertEquals(6, cacheTestInterface.testMethodNoCacheCount());
        
        result = cacheTestInterface.testMethodNoCache("2", false, false);
        assertEquals("testMethodNoCache(2)", result);
        assertEquals(7, cacheTestInterface.testMethodNoCacheCount());
    }

    @Test
    public void testMethodCacheDefaultNoArgs() {
        when(this.portalRequestUtils.getCurrentPortalRequest()).thenReturn(new MockHttpServletRequest());
        
        assertEquals(0, cacheTestInterface.testMethodNoCacheCountNoArgsCount());
        
        String result = cacheTestInterface.testMethodCacheDefaultNoArgs();
        assertEquals("testMethodCacheDefaultNoArgs()", result);
        assertEquals(1, cacheTestInterface.testMethodNoCacheCountNoArgsCount());
        
        result = cacheTestInterface.testMethodCacheDefaultNoArgs();
        assertEquals("testMethodCacheDefaultNoArgs()", result);
        assertEquals(1, cacheTestInterface.testMethodNoCacheCountNoArgsCount());
    }

    @Test
    public void testMethodCacheDefault() {
        when(this.portalRequestUtils.getCurrentPortalRequest()).thenReturn(new MockHttpServletRequest());
        
        assertEquals(0, cacheTestInterface.testMethodCacheDefaultCount());
        
        String result = cacheTestInterface.testMethodCacheDefault("1", false, false);
        assertEquals("testMethodCacheDefault(1)", result);
        assertEquals(1, cacheTestInterface.testMethodCacheDefaultCount());
        
        result = cacheTestInterface.testMethodCacheDefault("1", true, false);
        assertNull(result);
        assertEquals(2, cacheTestInterface.testMethodCacheDefaultCount());
        
        result = cacheTestInterface.testMethodCacheDefault("1", true, false);
        assertNull(result);
        assertEquals(3, cacheTestInterface.testMethodCacheDefaultCount());
        
        try {
            result = cacheTestInterface.testMethodCacheDefault("1", false, true);
            fail();
        }
        catch (Throwable t) {
            //expected
        }
        assertEquals(4, cacheTestInterface.testMethodCacheDefaultCount());
        
        try {
            result = cacheTestInterface.testMethodCacheDefault("1", false, true);
            fail();
        }
        catch (Throwable t) {
            //expected
        }
        assertEquals(5, cacheTestInterface.testMethodCacheDefaultCount());
        
        result = cacheTestInterface.testMethodCacheDefault("1", false, false);
        assertEquals("testMethodCacheDefault(1)", result);
        assertEquals(5, cacheTestInterface.testMethodCacheDefaultCount());
        
        result = cacheTestInterface.testMethodCacheDefault("2", false, false);
        assertEquals("testMethodCacheDefault(2)", result);
        assertEquals(6, cacheTestInterface.testMethodCacheDefaultCount());
    }

    @Test
    public void testMethodCacheDefaultNoRequest() {
        when(this.portalRequestUtils.getCurrentPortalRequest()).thenThrow(new IllegalStateException());
        
        assertEquals(0, cacheTestInterface.testMethodCacheDefaultCount());
        
        String result = cacheTestInterface.testMethodCacheDefault("1", false, false);
        assertEquals("testMethodCacheDefault(1)", result);
        assertEquals(1, cacheTestInterface.testMethodCacheDefaultCount());
        
        result = cacheTestInterface.testMethodCacheDefault("1", true, false);
        assertNull(result);
        assertEquals(2, cacheTestInterface.testMethodCacheDefaultCount());
        
        result = cacheTestInterface.testMethodCacheDefault("1", true, false);
        assertNull(result);
        assertEquals(3, cacheTestInterface.testMethodCacheDefaultCount());
        
        try {
            result = cacheTestInterface.testMethodCacheDefault("1", false, true);
            fail();
        }
        catch (Throwable t) {
            //expected
        }
        assertEquals(4, cacheTestInterface.testMethodCacheDefaultCount());
        
        try {
            result = cacheTestInterface.testMethodCacheDefault("1", false, true);
            fail();
        }
        catch (Throwable t) {
            //expected
        }
        assertEquals(5, cacheTestInterface.testMethodCacheDefaultCount());
        
        result = cacheTestInterface.testMethodCacheDefault("1", false, false);
        assertEquals("testMethodCacheDefault(1)", result);
        assertEquals(6, cacheTestInterface.testMethodCacheDefaultCount());
        
        result = cacheTestInterface.testMethodCacheDefault("2", false, false);
        assertEquals("testMethodCacheDefault(2)", result);
        assertEquals(7, cacheTestInterface.testMethodCacheDefaultCount());
    }

    @Test
    public void testMethodCacheNull() {
        when(this.portalRequestUtils.getCurrentPortalRequest()).thenReturn(new MockHttpServletRequest());
        
        assertEquals(0, cacheTestInterface.testMethodCacheNullCount());
        
        String result = cacheTestInterface.testMethodCacheNull("1", false, false);
        assertEquals("testMethodCacheNull(1)", result);
        assertEquals(1, cacheTestInterface.testMethodCacheNullCount());
        
        result = cacheTestInterface.testMethodCacheNull("1", true, false);
        assertNull(result);
        assertEquals(2, cacheTestInterface.testMethodCacheNullCount());
        
        result = cacheTestInterface.testMethodCacheNull("1", true, false);
        assertNull(result);
        assertEquals(2, cacheTestInterface.testMethodCacheNullCount());
        
        try {
            result = cacheTestInterface.testMethodCacheNull("1", false, true);
            fail();
        }
        catch (Throwable t) {
            //expected
        }
        assertEquals(3, cacheTestInterface.testMethodCacheNullCount());
        
        try {
            result = cacheTestInterface.testMethodCacheNull("1", false, true);
            fail();
        }
        catch (Throwable t) {
            //expected
        }
        assertEquals(4, cacheTestInterface.testMethodCacheNullCount());
        
        result = cacheTestInterface.testMethodCacheNull("1", false, false);
        assertEquals("testMethodCacheNull(1)", result);
        assertEquals(4, cacheTestInterface.testMethodCacheNullCount());
        
        result = cacheTestInterface.testMethodCacheNull("2", false, false);
        assertEquals("testMethodCacheNull(2)", result);
        assertEquals(5, cacheTestInterface.testMethodCacheNullCount());
    }

    @Test
    public void testMethodCacheThrows() {
        when(this.portalRequestUtils.getCurrentPortalRequest()).thenReturn(new MockHttpServletRequest());
        
        assertEquals(0, cacheTestInterface.testMethodCacheThrowsCount());
        
        String result = cacheTestInterface.testMethodCacheThrows("1", false, false);
        assertEquals("testMethodCacheThrows(1)", result);
        assertEquals(1, cacheTestInterface.testMethodCacheThrowsCount());
        
        result = cacheTestInterface.testMethodCacheThrows("1", true, false);
        assertNull(result);
        assertEquals(2, cacheTestInterface.testMethodCacheThrowsCount());
        
        result = cacheTestInterface.testMethodCacheThrows("1", true, false);
        assertNull(result);
        assertEquals(3, cacheTestInterface.testMethodCacheThrowsCount());
        
        try {
            result = cacheTestInterface.testMethodCacheThrows("1", false, true);
            fail();
        }
        catch (Throwable t) {
            //expected
        }
        assertEquals(4, cacheTestInterface.testMethodCacheThrowsCount());
        
        try {
            result = cacheTestInterface.testMethodCacheThrows("1", false, true);
            fail();
        }
        catch (Throwable t) {
            //expected
        }
        assertEquals(4, cacheTestInterface.testMethodCacheThrowsCount());
        
        result = cacheTestInterface.testMethodCacheThrows("1", false, false);
        assertEquals("testMethodCacheThrows(1)", result);
        assertEquals(4, cacheTestInterface.testMethodCacheThrowsCount());
        
        result = cacheTestInterface.testMethodCacheThrows("2", false, false);
        assertEquals("testMethodCacheThrows(2)", result);
        assertEquals(5, cacheTestInterface.testMethodCacheThrowsCount());
    }
}
