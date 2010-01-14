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
import org.apache.pluto.spi.PortletURLProvider;
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
    
    private final PortletUrl portletUrl;
    
    public PortletURLProviderImpl(IPortletWindow portletWindow, HttpServletRequest httpServletRequest, IPortletUrlSyntaxProvider portletUrlSyntaxProvider) {
        Validate.notNull(portletWindow, "portletWindow can not be null");
        Validate.notNull(httpServletRequest, "httpServletRequest can not be null");
        Validate.notNull(portletUrlSyntaxProvider, "portletUrlSyntaxProvider can not be null");
        
        this.portletWindow = portletWindow;
        this.httpServletRequest = httpServletRequest;
        this.portletUrlSyntaxProvider = portletUrlSyntaxProvider;
        
        //Init the portlet URL to have the same default assumptions as the PortletURLProvider interface
        this.portletUrl = new PortletUrl(this.portletWindow.getPortletWindowId());
        this.portletUrl.setParameters(new HashMap<String, List<String>>());
        this.portletUrl.setRequestType(RequestType.RENDER);
    }
    

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.PortletURLProvider#clearParameters()
     */
    public void clearParameters() {
        this.portletUrl.getParameters().clear();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.PortletURLProvider#isSecureSupported()
     */
    public boolean isSecureSupported() {
        return this.httpServletRequest.isSecure();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.PortletURLProvider#setSecure()
     */
    public void setSecure() throws PortletSecurityException {
        if (!this.httpServletRequest.isSecure()) {
            throw new PortletSecurityException("Secure URLs are not supported at this time");
        }
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.PortletURLProvider#setAction(boolean)
     */
    public void setAction(boolean action) {
        if (action) {
            this.portletUrl.setRequestType(RequestType.ACTION);
        }
        else {
            this.portletUrl.setRequestType(RequestType.RENDER);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.PortletURLProvider#setParameters(java.util.Map)
     * @param parmeters is Map<String, String[]>
     */
    @SuppressWarnings("unchecked")
    public void setParameters(Map parameters) {
        final Map<String, List<String>> listParameters = new LinkedHashMap<String, List<String>>();
        
        for (final Map.Entry<String, String[]> parameterEntry : ((Map<String, String[]>)parameters).entrySet()) {
            final String name = parameterEntry.getKey();
            final String[] values = parameterEntry.getValue();
            
            if (values == null) {
                listParameters.put(name, null);
            }
            else {
                listParameters.put(name, Arrays.asList(values));
            }
        }
        
        this.portletUrl.setParameters(listParameters);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.PortletURLProvider#setPortletMode(javax.portlet.PortletMode)
     */
    public void setPortletMode(PortletMode mode) {
        if (!this.portletWindow.getPortletMode().equals(mode)) {
            this.portletUrl.setPortletMode(mode);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.PortletURLProvider#setWindowState(javax.portlet.WindowState)
     */
    public void setWindowState(WindowState state) {
        if (!this.portletWindow.getWindowState().equals(state)) {
            this.portletUrl.setWindowState(state);
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.portletUrlSyntaxProvider.generatePortletUrl(this.httpServletRequest, this.portletWindow, this.portletUrl);
    }
}
