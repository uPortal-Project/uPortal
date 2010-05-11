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

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.Validate;
import org.apache.pluto.container.PortletURLProvider;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.url.IPortalRequestInfo;
import org.jasig.portal.url.IPortalUrlProvider;
import org.jasig.portal.url.IPortletPortalUrl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Contains the logic and string constants for generating and parsing portlet URL parameters.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Component
public class PortletUrlSyntaxProviderImpl implements IPortletUrlSyntaxProvider {
    private IPortalUrlProvider portalUrlProvider;
    
    /**
     * @param portalUrlProvider the portalUrlProvider to set
     */
    @Autowired
    public void setPortalUrlProvider(final IPortalUrlProvider portalUrlProvider) {
        this.portalUrlProvider = portalUrlProvider;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.url.IPortletUrlSyntaxProvider#generatePortletUrl(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindow, org.jasig.portal.portlet.url.PortletUrl)
     */
    public String generatePortletUrl(HttpServletRequest request,
            IPortletWindow portletWindow, PortletUrl portletUrl) {
        IPortletPortalUrl portalPortletUrl = portalUrlProvider.getPortletUrl(request, portletWindow.getPortletWindowId());
        portalPortletUrl = mergeWithPortletUrl(portalPortletUrl, portletUrl);
        return portalPortletUrl.toString();
    }

    public PortletUrl parsePortletUrl(HttpServletRequest request) {
        IPortalRequestInfo requestInfo = portalUrlProvider.getPortalRequestInfo(request);
        if(null == requestInfo.getTargetedPortletWindowId()) {
            return null;
        } else {
            IPortletWindowId portletWindowId = requestInfo.getTargetedPortletWindowId(); 
            IPortletPortalUrl portalPortletUrl = portalUrlProvider.getPortletUrl(request, portletWindowId);
            return toPortletUrl(portletWindowId, portalPortletUrl);
        }
    }
    
    /**
     * Convert a {@link IPortalPortletUrl} into a {@link PortletUrl}.
     * 
     * @param portalPortletUrl
     * @return
     */
    protected static PortletUrl toPortletUrl(IPortletWindowId portletWindowId, IPortletPortalUrl portalPortletUrl) {
        PortletUrl result = new PortletUrl(portletWindowId);
        Map<String, List<String>> parameters = portalPortletUrl.getPortletParameters();
        result.setParameters(parameters);
        
        result.setPortletMode(portalPortletUrl.getPortletMode());
        
        if(portalPortletUrl.isAction()) {
            result.setRequestType(PortletURLProvider.TYPE.ACTION);
        } else {
            result.setRequestType(PortletURLProvider.TYPE.RENDER);
        }
        
        // null is the default value for the secure field
        //result.setSecure(null);
        
        result.setWindowState(portalPortletUrl.getWindowState());
        return result;
    }
    
    /**
     * The purpose of this method is to port the fields of the {@link PortletUrl} argument
     * to the appropriate fields of the {@link IPortletPortalUrl} argument.
     * 
     * This method mutates the {@link IPortletPortalUrl} argument and return it.
     * 
     * Neither argument can be null.
     * 
     * @param original
     * @param mergeWith
     * @return the updated original {@link IPortalPortletUrl}
     */
    protected static IPortletPortalUrl mergeWithPortletUrl(IPortletPortalUrl original, PortletUrl mergeWith) {
        Validate.notNull(original, "original IPortalPortletUrl must not be null");
        Validate.notNull(mergeWith, "mergeWith PortletUrl must not be null");
        if (PortletURLProvider.TYPE.ACTION == mergeWith.getRequestType()) {
            original.setAction(true);
        }
        original.setPortletMode(mergeWith.getPortletMode());
        
        original.setWindowState(mergeWith.getWindowState());
        
        final Map<String, List<String>> mergeParameters = mergeWith.getParameters();
        for (final Map.Entry<String, List<String>> mergeParameter : mergeParameters.entrySet()) {
            original.setPortalParameter(mergeParameter.getKey(), mergeParameter.getValue());
        }
        return original;
    }

}
