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
package org.jasig.portal.portlet.delegation;

import java.util.Locale;
import java.util.TimeZone;

import javax.portlet.ResourceResponse;

import org.apache.commons.lang.time.FastDateFormat;
import org.jasig.portal.portlet.rendering.PortletResourceOutputHandler;

/**
 * Delegates to a {@link ResourceResponse}
 * 
 * @author Eric Dalquist
 */
public class ResourceResponsePortletOutputHandler extends MimeResponsePortletOutputHandler implements PortletResourceOutputHandler {
    private static final TimeZone GMT_ZONE = TimeZone.getTimeZone("GMT");
    private static final FastDateFormat HTTP_HEADER_DATE_FORMAT = FastDateFormat.getInstance("EEE, dd MMM yyyy HH:mm:ss Z", GMT_ZONE, Locale.US);
    
    
    private final ResourceResponse resourceResponse;
    
    public ResourceResponsePortletOutputHandler(ResourceResponse resourceResponse) {
        super(resourceResponse);
        this.resourceResponse = resourceResponse;
    }

    @Override
    public void setCharacterEncoding(String charset) {
        resourceResponse.setCharacterEncoding(charset);
    }

    @Override
    public void setContentLength(int len) {
        resourceResponse.setContentLength(len);
    }

    @Override
    public void setLocale(Locale locale) {
        resourceResponse.setLocale(locale);
    }

    @Override
    public void setStatus(int status) {
        resourceResponse.setProperty(ResourceResponse.HTTP_STATUS_CODE, Integer.toString(status));
    }

    @Override
    public void setDateHeader(String name, long date) {
        final String value = HTTP_HEADER_DATE_FORMAT.format(date);
        this.setHeader(name, value);
    }

    @Override
    public void addDateHeader(String name, long date) {
        final String value = HTTP_HEADER_DATE_FORMAT.format(date);
        this.addHeader(name, value);
    }

    @Override
    public void setHeader(String name, String value) {
        this.resourceResponse.setProperty(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        this.resourceResponse.addProperty(name, value);        
    }

    @Override
    public void setIntHeader(String name, int value) {
        this.resourceResponse.setProperty(name, Integer.toString(value));
    }

    @Override
    public void addIntHeader(String name, int value) {
        this.resourceResponse.addProperty(name, Integer.toString(value));        
    }
}
