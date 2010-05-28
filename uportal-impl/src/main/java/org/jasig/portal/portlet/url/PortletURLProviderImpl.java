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

package org.jasig.portal.portlet.url;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.PortletSecurityException;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.container.PortletURLProvider;
import org.jasig.portal.portlet.om.IPortletWindow;

/**
 * Tracks configuration for a portlet URL then generates one when {@link #toString()} is called.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletURLProviderImpl implements PortletURLProvider {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private final IPortletWindow portletWindow;
    private final HttpServletRequest httpServletRequest;
    private final IPortletUrlSyntaxProvider portletUrlSyntaxProvider;
    
    private final Map<String, String[]> renderParameters = new LinkedHashMap<String, String[]>();
    private final PortletUrl portletUrl;
    
    public PortletURLProviderImpl(TYPE type, IPortletWindow portletWindow, HttpServletRequest httpServletRequest, IPortletUrlSyntaxProvider portletUrlSyntaxProvider) {
        Validate.notNull(portletWindow, "portletWindow can not be null");
        Validate.notNull(httpServletRequest, "httpServletRequest can not be null");
        Validate.notNull(portletUrlSyntaxProvider, "portletUrlSyntaxProvider can not be null");
        
        this.portletWindow = portletWindow;
        this.httpServletRequest = httpServletRequest;
        this.portletUrlSyntaxProvider = portletUrlSyntaxProvider;
        
        //Init the portlet URL to have the same default assumptions as the PortletURLProvider interface
        this.portletUrl = new PortletUrl(this.portletWindow.getPortletWindowId());
        this.portletUrl.setParameters(new HashMap<String, List<String>>());
        
        switch (type) {
            case ACTION: {
                this.portletUrl.setRequestType(PortletURLProvider.TYPE.ACTION);
            } break;
            
            case RENDER: {
                this.portletUrl.setRequestType(PortletURLProvider.TYPE.RENDER);
            } break;
            
            default: {
                throw new IllegalArgumentException("Only URL types ACTION and RENDER are supported. " + type + " is not supported");
            }
        }
    }

    @Override
    public String getCacheability() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PortletMode getPortletMode() {
        final PortletMode urlPortletMode = this.portletUrl.getPortletMode();
        if (urlPortletMode != null) {
            return urlPortletMode;
        }
        
        return this.portletWindow.getPortletMode();
    }

    @Override
    public Map<String, List<String>> getProperties() {
        // TODO Auto-generated method stub
        return new LinkedHashMap<String, List<String>>();
    }

    @Override
    public Map<String, String[]> getPublicRenderParameters() {
        // TODO Auto-generated method stub
        return new LinkedHashMap<String, String[]>();
    }

    @Override
    public Map<String, String[]> getRenderParameters() {
        return this.renderParameters;
    }

    @Override
    public String getResourceID() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TYPE getType() {
        return this.portletUrl.getRequestType();
    }

    @Override
    public WindowState getWindowState() {
        final WindowState urlWindowState = this.portletUrl.getWindowState();
        if (urlWindowState != null) {
            return urlWindowState;
        }
        
        return this.portletWindow.getWindowState();
    }

    @Override
    public boolean isSecure() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setCacheability(String cacheLevel) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setPortletMode(PortletMode mode) {
        if (!this.portletWindow.getPortletMode().equals(mode)) {
            this.portletUrl.setPortletMode(mode);
        }        
    }

    @Override
    public void setResourceID(String resourceID) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setSecure(boolean secure) throws PortletSecurityException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setWindowState(WindowState state) {
        if (!this.portletWindow.getWindowState().equals(state)) {
            this.portletUrl.setWindowState(state);
        }        
    }

    @Override
    public String toURL() {
        final Map<String, List<String>> convertedParameters = new LinkedHashMap<String, List<String>>();
        for (final Map.Entry<String, String[]> renderParameterEntry : this.renderParameters.entrySet()) {
            final String[] values = renderParameterEntry.getValue();
            final String name = renderParameterEntry.getKey();
            convertedParameters.put(name, Arrays.asList(values));
        }
        this.portletUrl.setParameters(convertedParameters);
        
        return this.portletUrlSyntaxProvider.generatePortletUrl(this.httpServletRequest, this.portletWindow, this.portletUrl);
    }

    @Override
    public void write(Writer out, boolean escapeXML) throws IOException {
            String url = this.toURL();
            if (escapeXML) {
                //TODO replace with library API
                url = url.replaceAll("&", "&amp;");
                url = url.replaceAll("<", "&lt;");
                url = url.replaceAll(">", "&gt;");
                url = url.replaceAll("\'", "&#039;");
                url = url.replaceAll("\"", "&#034;");
            }
            out.write(url);
    }

    @Override
    public String toString() {
        return this.toURL();
    }
}
