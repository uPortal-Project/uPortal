/**
 * Copyright © 2004 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jasig.portal.container.services.information;

import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

import org.apache.pluto.services.information.DynamicInformationProvider;
import org.apache.pluto.services.information.InformationProviderService;
import org.apache.pluto.services.information.StaticInformationProvider;
import org.jasig.portal.container.services.PortletContainerService;

/**
 * Implementation of Apache Pluto InformationProviderService.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class InformationProviderServiceImpl implements PortletContainerService, InformationProviderService {
    
    private ServletConfig servletConfig = null;
    private Properties properties = null;
	private static StaticInformationProvider staticInfoProvider = null;
    
    private static final String dynamicInformationProviderRequestParameterName = "org.apache.pluto.services.information.DynamicInformationProvider";

    // PortletContainerService methods
    
    public void init(ServletConfig servletConfig, Properties properties) throws Exception {
        this.servletConfig = servletConfig;
        this.properties = properties;
		staticInfoProvider = new StaticInformationProviderImpl();	
    }
    
    public void destroy() throws Exception {
        properties = null;
        servletConfig = null;
		staticInfoProvider = null;
    }    
    
    // InformationProviderService methods
    
    public StaticInformationProvider getStaticProvider() {
	   return staticInfoProvider;
    }

    public DynamicInformationProvider getDynamicProvider(HttpServletRequest request) {
        DynamicInformationProvider provider = (DynamicInformationProvider)request.getAttribute(dynamicInformationProviderRequestParameterName);

        if (provider == null) {
            provider = new DynamicInformationProviderImpl(request,servletConfig);
            request.setAttribute(dynamicInformationProviderRequestParameterName, provider);
        }

        return provider;
    }

}
