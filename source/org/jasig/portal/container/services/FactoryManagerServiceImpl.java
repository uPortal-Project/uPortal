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

package org.jasig.portal.container.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Enumeration;

import javax.servlet.ServletConfig;

import org.apache.pluto.factory.Factory;
import org.apache.pluto.services.factory.FactoryManagerService;
import org.jasig.portal.utils.ResourceLoader;


/**
 * Implementation of Apache Pluto object model.
 * @author Michael Ivanov, mvi@immagic.com
 * @version $Revision$
 */
public class FactoryManagerServiceImpl implements PortletContainerService, FactoryManagerService {

    private ServletConfig servletConfig = null;
    private Properties properties = null;
    private Map factories = null;
    protected final static String FACTORY_PROPERTIES = "/properties/container/factory.properties";
    
    public FactoryManagerServiceImpl() {
        factories = new HashMap();        
    }
    
    // PortletContainerService methods
    
    public void init(ServletConfig servletConfig, Properties properties) throws Exception {
        this.servletConfig = servletConfig;
        this.properties = properties;
        Properties factoryProperties = null;
        try {
         factoryProperties = ResourceLoader.getResourceAsProperties(FactoryManagerServiceImpl.class,FACTORY_PROPERTIES);
        } catch ( Exception e ) {
           throw new IllegalAccessException ( "Could not load " + FACTORY_PROPERTIES + " file");	 
          }
        for ( Enumeration names = factoryProperties.propertyNames(); names.hasMoreElements(); ) {
        	String name = (String) names.nextElement();
            addFactory(name,factoryProperties.getProperty(name));
        }    
        
    }
    
    public void destroy() throws Exception {
        factories = null;
    }
    
    // FactoryManagerService methods
    
    public Factory getFactory(Class theClass) {
        return (Factory)factories.get(theClass);
    }
    
    // Additional methods
    
    private void addFactory(String factoryInterfaceName, String factoryImplName) throws Exception {
            Class factoryInterface = Class.forName(factoryInterfaceName);
            Class factoryImpl = Class.forName(factoryImplName);
            Factory factory = (Factory)factoryImpl.newInstance();
            factory.init(servletConfig, properties);
            addFactory(factoryInterface, factory);
    }
    
    private void addFactory(Class factoryInterface, Factory factory) {
        factories.put(factoryInterface, factory);
    }

}
