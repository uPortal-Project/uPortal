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

package org.springframework.web.servlet.mvc.annotation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.ObjectUtils;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Ignore
public class ServletAnnotationMappingUtilsTest {
    @Test
    public void checkSpringMultipleHeadersTest() {
        final String[] headers = {"org.jasig.portal.url.UrlType=RENDER", "org.jasig.portal.url.UrlState!=EXCLUSIVE"};
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("org.jasig.portal.url.UrlType", "RENDER");
        request.addHeader("org.jasig.portal.url.UrlState", "EXCLUSIVE");
        
        final boolean matches = ServletAnnotationMappingUtils.checkHeaders(headers, request);
        assertFalse(matches);
    }
    
    @Test
    public void checkSpringNoMatchHeadersTest() {
        final String[] headers = {"org.jasig.portal.url.UrlState!=EXCLUSIVE"};
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("org.jasig.portal.url.UrlState", "EXCLUSIVE");
        
        final boolean matches = ServletAnnotationMappingUtils.checkHeaders(headers, request);
        assertFalse(matches);
    }
    
    @Test
    public void checkSpringMatchHeadersTest() {
        final String[] headers = {"org.jasig.portal.url.UrlState=EXCLUSIVE"};
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("org.jasig.portal.url.UrlState", "EXCLUSIVE");
        
        final boolean matches = ServletAnnotationMappingUtils.checkHeaders(headers, request);
        assertTrue(matches);
    }
    
    @Test
    public void checkSpringNoMatchContentTypeTest() {
        final String[] headers = {"content-type!=application/xml"};
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("content-type", "application/xml");
        
        final boolean matches = ServletAnnotationMappingUtils.checkHeaders(headers, request);
        assertFalse(matches);
    }
    
    @Test
    public void checkSpringMatchContentTypeTest() {
        final String[] headers = {"content-type=application/xml"};
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("content-type", "application/xml");
        
        final boolean matches = ServletAnnotationMappingUtils.checkHeaders(headers, request);
        assertTrue(matches);
    }
    
    

    @Test
    public void checkFixedMultipleHeadersTest() {
        final String[] headers = {"org.jasig.portal.url.UrlType=RENDER", "org.jasig.portal.url.UrlState!=EXCLUSIVE"};
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("org.jasig.portal.url.UrlType", "RENDER");
        request.addHeader("org.jasig.portal.url.UrlState", "EXCLUSIVE");
        
        final boolean matches = checkHeaders(headers, request);
        assertFalse(matches);
    }
    
    @Test
    public void checkFixedNoMatchHeadersTest() {
        final String[] headers = {"org.jasig.portal.url.UrlState!=EXCLUSIVE"};
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("org.jasig.portal.url.UrlState", "EXCLUSIVE");
        
        final boolean matches = checkHeaders(headers, request);
        assertFalse(matches);
    }
    
    @Test
    public void checkFixedMatchHeadersTest() {
        final String[] headers = {"org.jasig.portal.url.UrlState=EXCLUSIVE"};
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("org.jasig.portal.url.UrlState", "EXCLUSIVE");
        
        final boolean matches = checkHeaders(headers, request);
        assertTrue(matches);
    }
    
    @Test
    public void checkFixedNoMatchContentTypeTest() {
        final String[] headers = {"content-type!=application/xml"};
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("content-type", "application/xml");
        
        final boolean matches = checkHeaders(headers, request);
        assertFalse(matches);
    }
    
    @Test
    public void checkFixedMatchContentTypeTest() {
        final String[] headers = {"content-type=application/xml"};
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("content-type", "application/xml");
        
        final boolean matches = checkHeaders(headers, request);
        assertTrue(matches);
    }
    
    public static boolean checkHeaders(String[] headers, HttpServletRequest request) {
        if (!ObjectUtils.isEmpty(headers)) {
            for (String header : headers) {
                int separator = header.indexOf('=');
                if (separator == -1) {
                    if (header.startsWith("!")) {
                        if (request.getHeader(header.substring(1)) != null) {
                            return false;
                        }
                    }
                    else if (request.getHeader(header) == null) {
                        return false;
                    }
                }
                else {
                    boolean negated = separator > 0 && header.charAt(separator - 1) == '!';
                    String key = !negated ? header.substring(0, separator) : header.substring(0, separator - 1);
                    String value = header.substring(separator + 1);
                    if (isMediaTypeHeader(key)) {
                        List<MediaType> requestMediaTypes = MediaType.parseMediaTypes(request.getHeader(key));
                        List<MediaType> valueMediaTypes = MediaType.parseMediaTypes(value);
                        boolean found = false;
                        for (Iterator<MediaType> valIter = valueMediaTypes.iterator(); valIter.hasNext() && !found;) {
                            MediaType valueMediaType = valIter.next();
                            for (Iterator<MediaType> reqIter = requestMediaTypes.iterator();
                                    reqIter.hasNext() && !found;) {
                                MediaType requestMediaType = reqIter.next();
                                if (valueMediaType.includes(requestMediaType)) {
                                    found = true;
                                }
                            }

                        }
                        if (!found && !negated) {
                            return false;
                        }
                        else if (found && negated) {
                            return false;
                        }
                    }
                    else if (value.equals(request.getHeader(key))) {
                        if (negated) {
                            return false;
                        }
                    }
                    else if (!negated) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean isMediaTypeHeader(String headerName) {
        return "Accept".equalsIgnoreCase(headerName) || "Content-Type".equalsIgnoreCase(headerName);
    }
}
