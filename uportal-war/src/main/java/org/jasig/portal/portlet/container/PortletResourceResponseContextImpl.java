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

package org.jasig.portal.portlet.container;

import java.nio.charset.Charset;
import java.util.Locale;

import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.LocaleUtils;
import org.apache.http.HeaderElement;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeaderValueParser;
import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletResourceResponseContext;
import org.jasig.portal.portlet.container.properties.IRequestPropertiesManager;
import org.jasig.portal.portlet.container.services.IPortletCookieService;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.rendering.PortletResourceOutputHandler;
import org.jasig.portal.url.IPortalUrlProvider;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletResourceResponseContextImpl extends PortletMimeResponseContextImpl implements
        PortletResourceResponseContext {

    private final PortletResourceOutputHandler portletResourceOutputHandler;

    public PortletResourceResponseContextImpl(PortletContainer portletContainer, IPortletWindow portletWindow,
            HttpServletRequest containerRequest, HttpServletResponse containerResponse,
            IRequestPropertiesManager requestPropertiesManager, IPortalUrlProvider portalUrlProvider,
            IPortletCookieService portletCookieService) {

        super(portletContainer, portletWindow, containerRequest, containerResponse, requestPropertiesManager,
                portalUrlProvider, portletCookieService);

        this.portletResourceOutputHandler = (PortletResourceOutputHandler) this.getPortletOutputHandler();
    }

    @Override
    public void setCharacterEncoding(String charset) {
        this.checkContextStatus();
        this.portletResourceOutputHandler.setCharacterEncoding(charset);
    }

    @Override
    public void setContentLength(int len) {
        this.checkContextStatus();
        this.portletResourceOutputHandler.setContentLength(len);
    }

    @Override
    public void setLocale(Locale locale) {
        this.checkContextStatus();
        this.portletResourceOutputHandler.setLocale(locale);
    }

    @Override
    protected boolean managerSetProperty(String key, String value) {
        final boolean handled = super.managerSetProperty(key, value);
        if (!handled) {
            if (handleResourceHeader(key, value)) {
                return true;
            }
            
            this.portletResourceOutputHandler.setHeader(key, value);
        }
        return true;
    }

    @Override
    protected boolean managerAddProperty(String key, String value) {
        final boolean handled = super.managerAddProperty(key, value);
        if (!handled) {
            if (handleResourceHeader(key, value)) {
                return true;
            }
            
            this.portletResourceOutputHandler.addHeader(key, value);
        }
        return true;
    }
    
    /**
     * Handles resource response specific headers. Returns true if the header was consumed by this method and requires no further processing
     * 
     * @return
     */
    protected boolean handleResourceHeader(String key, String value) {
        if (ResourceResponse.HTTP_STATUS_CODE.equals(key)) {
            this.portletResourceOutputHandler.setStatus(Integer.parseInt(value));
            return true;
        }
        if ("Content-Type".equals(key)) {
            final ContentType contentType = ContentType.parse(value);
            final Charset charset = contentType.getCharset();
            if (charset != null) { 
                this.portletResourceOutputHandler.setCharacterEncoding(charset.name());
            }
            this.portletResourceOutputHandler.setContentType(contentType.getMimeType());
            return true;
        }
        if ("Content-Length".equals(key)) {
            this.portletResourceOutputHandler.setContentLength(Integer.parseInt(value));
            return true;
        }
        if ("Content-Language".equals(key)) {
            final HeaderElement[] parts = BasicHeaderValueParser.parseElements(value, null);
            if (parts.length > 0) {
                final String localeStr = parts[0].getValue();
                final Locale locale = LocaleUtils.toLocale(localeStr);
                this.portletResourceOutputHandler.setLocale(locale);
                return true;
            }
        }
        
        return false;
    }
}
