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

package org.jasig.portal.container.services.factory.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletConfig;

import org.apache.pluto.factory.Factory;
import org.apache.pluto.services.factory.FactoryManagerService;
import org.jasig.portal.container.services.PortletContainerService;
import org.jasig.portal.services.LogService;

/**
 * Implementation of Apache Pluto object model.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class FactorManagerServiceImpl implements PortletContainerService, FactoryManagerService {

    private ServletConfig servletConfig = null;
    private Properties properties = null;
    private Map factories = null;
    
    public FactorManagerServiceImpl() {
        factories = new HashMap();        
    }
    
    // PortletContainerService methods
    
    public void init(ServletConfig servletConfig, Properties properties) throws Exception {
        this.servletConfig = servletConfig;
        this.properties = properties;
        
        // Temporarily hard-coding registered factories
        // These need to be read from some config file
        
        //addFactory("javax.servlet.http.HttpServletRequest", "org.jasig.portal.servlet.ServletRequestFactoryImpl");
        addFactory("javax.servlet.http.HttpServletResponse", "org.jasig.portal.servlet.ServletResponseFactoryImpl");

        addFactory("javax.portlet.ActionRequest", "org.apache.pluto.factory.impl.ActionRequestFactoryImpl");
        addFactory("javax.portlet.RenderRequest", "org.apache.pluto.factory.impl.RenderRequestFactoryImpl");
        addFactory("javax.portlet.RenderResponse", "org.apache.pluto.factory.impl.RenderResponseFactoryImpl");
        addFactory("javax.portlet.PortletSession", "org.apache.pluto.factory.impl.PortletSessionFactoryImpl");
        addFactory("javax.portlet.PortletConfig", "org.apache.pluto.factory.impl.PortletConfigFactoryImpl");
        addFactory("javax.portlet.PortletContext", "org.apache.pluto.factory.impl.PortletContextFactoryImpl");
        addFactory("javax.portlet.PortletPreferences", "org.apache.pluto.factory.impl.PortletPreferencesFactoryImpl");
        addFactory("javax.portlet.PortalContext", "org.apache.pluto.factory.impl.PortalContextFactoryImpl");
        addFactory("javax.portlet.ActionResponse", "org.apache.pluto.factory.impl.ActionResponseFactoryImpl");
        addFactory("javax.portlet.PortletURL", "org.apache.pluto.factory.impl.PortletURLFactoryImpl");
        addFactory("javax.portlet.PortletPreferences", "org.apache.pluto.factory.impl.PortletPreferencesFactoryImpl");

        addFactory("org.apache.pluto.invoker.PortletInvoker", "org.apache.pluto.invoker.impl.PortletInvokerFactoryImpl");
        addFactory("org.apache.pluto.util.NamespaceMapper", "org.apache.pluto.util.impl.NamespaceMapperFactoryImpl");
        addFactory("org.apache.pluto.factory.ObjectIDFactory", "org.jasig.portal.container.factory.impl.ObjectIDFactoryImpl");
        addFactory("org.apache.pluto.om.ControllerFactory", "org.jasig.portal.container.om.impl.ControllerFactoryImpl");

        //addFactory("org.apache.pluto.portalImpl.factory.InformationProviderFactory", "org.apache.pluto.portalImpl.core.InformationProviderServiceFactoryImpl");
        //addFactory("org.apache.pluto.portalImpl.factory.DynamicTitleServiceFactory", "org.apache.pluto.portalImpl.core.DynamicTitleServiceFactoryImpl");
        
    }
    
    public void destroy() throws Exception {
        // Nothing to do at this time
    }
    
    // FactoryManagerService methods
    
    public Factory getFactory(Class theClass) {
        return (Factory)factories.get(theClass);
    }
    
    // Additional methods
    
    private void addFactory(String factoryInterfaceName, String factoryImplName) {
        try {
            Class factoryInterface = Class.forName(factoryInterfaceName);
            Class factoryImpl = Class.forName(factoryImplName);
            Factory factory = (Factory)factoryImpl.newInstance();
            factory.init(servletConfig, properties);
            addFactory(factoryInterface, factory);
        } catch (Exception e) {
            LogService.log(LogService.ERROR, e);
        }
    }
    
    private void addFactory(Class factoryInterface, Factory factory) {
        factories.put(factoryInterface, factory);
    }

}
