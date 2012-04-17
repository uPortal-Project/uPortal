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

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.portlet.CacheControl;

import org.jasig.portal.portlet.rendering.PortletResourceOutputHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Nicholas Blair
 * @version $Id$
 */
@RunWith(MockitoJUnitRunner.class)
public class HeaderSettingCacheControlTest {
    private HeaderSettingCacheControl headerSettingCacheControl;
    @Mock private CacheControl cacheControl;
    @Mock private PortletResourceOutputHandler portletResourceOutputHandler;
    
    @Before
    public void setup() {
        headerSettingCacheControl = new HeaderSettingCacheControl(this.cacheControl, this.portletResourceOutputHandler);
    }

	
	@Test
	public void testSetEtagWithResponse() {
        headerSettingCacheControl.setETag("123456");
        
        verify(cacheControl).setETag("123456");
        verify(portletResourceOutputHandler).setHeader("ETag", "123456");
        
        verifyNoMoreInteractions(cacheControl);
        verifyNoMoreInteractions(portletResourceOutputHandler);
	}
	
	@Test
	public void testSetEtagNullWithResponse() {
	    headerSettingCacheControl.setETag(null);
		
		verify(cacheControl).setETag(null);
        verify(portletResourceOutputHandler).setHeader("ETag", null);
        
        verifyNoMoreInteractions(cacheControl);
        verifyNoMoreInteractions(portletResourceOutputHandler);
	}
	
	@Test
	public void testSetZeroExpirationTime() {
		// 0 value for expiration time should not trigger header set
	    headerSettingCacheControl.setExpirationTime(0);
	    
	    verify(cacheControl).setExpirationTime(0);
	    verify(cacheControl).isPublicScope();
        
        verifyNoMoreInteractions(cacheControl);
        verifyNoMoreInteractions(portletResourceOutputHandler);
	}
    
    @Test
    public void testSetNeverExpirationTime() {
        headerSettingCacheControl.setExpirationTime(-1);
        
        verify(cacheControl).setExpirationTime(-1);
        verify(cacheControl).isPublicScope();
        
        verify(portletResourceOutputHandler).setDateHeader(eq("Last-Modified"), anyLong());
        verify(portletResourceOutputHandler).setHeader("CacheControl", "private");
        verify(portletResourceOutputHandler).setDateHeader(eq("Expires"), anyLong());
        verify(portletResourceOutputHandler).addHeader("CacheControl", "max-age=31536000");
        
        verifyNoMoreInteractions(cacheControl);
        verifyNoMoreInteractions(portletResourceOutputHandler);
    }
    
    @Test
    public void testSetNeverExpirationTimeThenScope() {
        when(cacheControl.getExpirationTime()).thenReturn(-1);
        
        headerSettingCacheControl.setPublicScope(true);
        
        verify(cacheControl).setPublicScope(true);
        verify(cacheControl).getExpirationTime();
        
        verify(portletResourceOutputHandler).setDateHeader(eq("Last-Modified"), anyLong());
        verify(portletResourceOutputHandler).setHeader("CacheControl", "public");
        verify(portletResourceOutputHandler).setDateHeader(eq("Expires"), anyLong());
        verify(portletResourceOutputHandler).addHeader("CacheControl", "max-age=31536000");
        
        verifyNoMoreInteractions(cacheControl);
        verifyNoMoreInteractions(portletResourceOutputHandler);
    }
    
    @Test
    public void testSetMinuteExpirationTime() {
        when(cacheControl.isPublicScope()).thenReturn(true);
        
        headerSettingCacheControl.setExpirationTime(60);
        
        verify(cacheControl).setExpirationTime(60);
        verify(cacheControl).isPublicScope();
        
        verify(portletResourceOutputHandler).setDateHeader(eq("Last-Modified"), anyLong());
        verify(portletResourceOutputHandler).setHeader("CacheControl", "public");
        verify(portletResourceOutputHandler).setDateHeader(eq("Expires"), anyLong());
        verify(portletResourceOutputHandler).addHeader("CacheControl", "max-age=60");
        
        verifyNoMoreInteractions(cacheControl);
        verifyNoMoreInteractions(portletResourceOutputHandler);
    }
}
