/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
        } catch (Exception e) {
            throw new IllegalAccessException ("Could not load " + FACTORY_PROPERTIES + " file");	 
        }
        for (Enumeration names = factoryProperties.propertyNames(); names.hasMoreElements();) {
            String name = (String)names.nextElement();
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
