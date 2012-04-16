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

package org.jasig.portal.portlet.container.services;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.container.PortletAppDescriptorService;
import org.apache.pluto.container.PortletContainerException;
import org.apache.pluto.container.PortletWindow;
import org.apache.pluto.container.RequestDispatcherService;
import org.apache.pluto.container.driver.DriverPortletConfig;
import org.apache.pluto.container.driver.DriverPortletContext;
import org.apache.pluto.container.driver.PortletContextService;
import org.apache.pluto.container.driver.PortletRegistryEvent;
import org.apache.pluto.container.driver.PortletRegistryListener;
import org.apache.pluto.container.driver.PortletRegistryService;
import org.apache.pluto.container.impl.PortletAppDescriptorServiceImpl;
import org.apache.pluto.container.om.portlet.PortletApplicationDefinition;
import org.apache.pluto.container.om.portlet.PortletDefinition;
import org.apache.pluto.driver.container.DriverPortletConfigImpl;
import org.apache.pluto.driver.container.DriverPortletContextImpl;
import org.jasig.portal.portlet.dao.jpa.ThreadContextClassLoaderAspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Nicholas Blair, npblair@wisc.edu
 * @version $Revision$
 */
@Service
public class LocalPortletContextManager implements PortletRegistryService, PortletContextService {

    /** Web deployment descriptor location. */
    private static final String WEB_XML = "/WEB-INF/web.xml";

    /** Portlet deployment descriptor location. */
    private static final String PORTLET_XML = "/WEB-INF/portlet.xml";

    protected final Log logger = LogFactory.getLog(this.getClass());
    
    /**
     * The PortletContext cache map: key is servlet context, and value is the
     * associated portlet context.
     */
    private Map<String, DriverPortletContext> portletContexts = new ConcurrentHashMap<String, DriverPortletContext>();

    /**
     * The PortletContext cache map: key is servlet context, and value is the
     * associated portlet context.
     */
    private final Map<String, DriverPortletConfig> portletConfigs = new ConcurrentHashMap<String, DriverPortletConfig>();

    /**
     * The registered listeners that should be notified upon
     * registry events.
     */
    private final List<PortletRegistryListener> registryListeners = new CopyOnWriteArrayList<PortletRegistryListener>();

    /**
     * The classloader for the portal, key is portletWindow and value is the classloader.
     * TODO this looks like a horrible memory leak
     */
    private final Map<String, ClassLoader> classLoaders = new ConcurrentHashMap<String, ClassLoader>();

    /**
     * Cache of descriptors.  WeakHashMap is used so that
     * once the context is destroyed (kinda), the cache is eliminated.
     * Ideally we'd use a ServletContextListener, but at this
     * point I'm wondering if we really want to add another
     * config requirement in the servlet xml? Hmm. . .
     */
    private final Map<ServletContext, PortletApplicationDefinition> portletAppDefinitionCache = new WeakHashMap<ServletContext, PortletApplicationDefinition>();

    private PortletAppDescriptorService portletAppDescriptorService = new PortletAppDescriptorServiceImpl();
    private RequestDispatcherService requestDispatcherService;


    @Autowired
    public void setRequestDispatcherService(RequestDispatcherService requestDispatcherService) {
        this.requestDispatcherService = requestDispatcherService;
    }


    public void setPortletAppDescriptorService(PortletAppDescriptorService portletAppDescriptorService) {
        this.portletAppDescriptorService = portletAppDescriptorService;
    }

    // Public Methods ----------------------------------------------------------

    /**
     * Retrieves the PortletContext associated with the given ServletContext.
     * If one does not exist, it is created.
     *
     * @param config the servlet config.
     * @return the InternalPortletContext associated with the ServletContext.
     * @throws PortletContainerException
     */
    @Override
    public synchronized String register(ServletConfig config) throws PortletContainerException {
        ServletContext servletContext = config.getServletContext();
        String contextPath = servletContext.getContextPath();
        if (!portletContexts.containsKey(contextPath)) {

            PortletApplicationDefinition portletApp = this
                    .getPortletAppDD(servletContext, contextPath, contextPath);

            DriverPortletContext portletContext = new DriverPortletContextImpl(servletContext, portletApp,
                    requestDispatcherService);

            portletContexts.put(contextPath, portletContext);

            fireRegistered(portletContext);

            if (logger.isInfoEnabled()) {
                logger.info("Registered portlet application for context '" + contextPath + "'");

                logger.info("Registering " + portletApp.getPortlets().size() + " portlets for context "
                        + portletContext.getApplicationName());
            }

            //TODO have the portlet servlet provide the portlet's classloader as parameter to this method
            //This approach is needed as all pluto callbacks in uPortal have an aspect that switches the thread classloader back
            //to uPortal's classloader.
            ClassLoader classLoader = ThreadContextClassLoaderAspect.getPreviousClassLoader();
            if (classLoader == null) {
                classLoader = Thread.currentThread().getContextClassLoader();
            }
            classLoaders.put(portletApp.getName(), classLoader);
            for (PortletDefinition portlet : portletApp.getPortlets()) {
                String appName = portletContext.getApplicationName();
                if (appName == null) {
                    throw new PortletContainerException("Portlet application name should not be null.");
                }
                portletConfigs.put(portletContext.getApplicationName() + "/" + portlet.getPortletName(),
                        new DriverPortletConfigImpl(portletContext, portlet));
            }
        }
        else {
            if (logger.isInfoEnabled()) {
                logger.info("Portlet application for context '" + contextPath + "' already registered.");
            }
        }
        return contextPath;
    }

    @Override
    public synchronized void unregister(DriverPortletContext context) {
        portletContexts.remove(context.getApplicationName());
        classLoaders.remove(context.getApplicationName());
        Iterator<String> configs = portletConfigs.keySet().iterator();
        while (configs.hasNext()) {
            String key = configs.next();
            if (key.startsWith(context.getApplicationName() + "/")) {
                configs.remove();
            }
        }
        fireRemoved(context);
    }

    @Override
    public Iterator<String> getRegisteredPortletApplicationNames() {
        return new HashSet<String>(portletContexts.keySet()).iterator();
    }

    @Override
    public Iterator<DriverPortletContext> getPortletContexts() {
        return new HashSet<DriverPortletContext>(portletContexts.values()).iterator();
    }

    @Override
    public DriverPortletContext getPortletContext(String applicationName) {
        return portletContexts.get(applicationName);
    }

    @Override
    public DriverPortletContext getPortletContext(PortletWindow portletWindow) throws PortletContainerException {
        return portletContexts.get(portletWindow.getPortletDefinition().getApplication().getName());
    }

    @Override
    public DriverPortletConfig getPortletConfig(String applicationName, String portletName)
            throws PortletContainerException {
        DriverPortletConfig ipc = portletConfigs.get(applicationName + "/" + portletName);
        if (ipc != null) {
            return ipc;
        }
        String msg = "Unable to locate portlet config [applicationName=" + applicationName + "]/[" + portletName + "].";
        logger.warn(msg);
        throw new PortletContainerException(msg);
    }

    @Override
    public PortletDefinition getPortlet(String applicationName, String portletName) throws PortletContainerException {
        DriverPortletConfig ipc = portletConfigs.get(applicationName + "/" + portletName);
        if (ipc != null) {
            return ipc.getPortletDefinition();
        }
        String msg = "Unable to retrieve portlet: '" + applicationName + "/" + portletName + "'";
        logger.warn(msg);
        throw new PortletContainerException(msg);
    }

    @Override
    public PortletApplicationDefinition getPortletApplication(String applicationName) throws PortletContainerException {
        DriverPortletContext ipc = portletContexts.get(applicationName);
        if (ipc != null) {
            return ipc.getPortletApplicationDefinition();
        }
        String msg = "Unable to retrieve portlet application: '" + applicationName + "'";
        logger.warn(msg);
        throw new PortletContainerException(msg);
    }

    @Override
    public ClassLoader getClassLoader(String applicationName) {
        return classLoaders.get(applicationName);
    }

    @Override
    public void addPortletRegistryListener(PortletRegistryListener listener) {
        registryListeners.add(listener);
    }

    @Override
    public void removePortletRegistryListener(PortletRegistryListener listener) {
        registryListeners.remove(listener);
    }
    
    private void fireRegistered(DriverPortletContext context) {
        PortletRegistryEvent event = new PortletRegistryEvent();
        event.setPortletApplication(context.getPortletApplicationDefinition());

        for (PortletRegistryListener l : registryListeners) {
            l.portletApplicationRegistered(event);
        }
        logger.info("Portlet Context '/" + context.getApplicationName() + "' registered.");
    }

    private void fireRemoved(DriverPortletContext context) {
        PortletRegistryEvent event = new PortletRegistryEvent();
        event.setPortletApplication(context.getPortletApplicationDefinition());

        for (PortletRegistryListener l : registryListeners) {
            l.portletApplicationRemoved(event);
        }

        logger.info("Portlet Context '/" + context.getApplicationName() + "' removed.");
    }

    /**
     * Retrieve the Portlet Application Deployment Descriptor for the given
     * servlet context.  Create it if it does not allready exist.
     *
     * @param servletContext  the servlet context.
     * @return The portlet application deployment descriptor.
     * @throws PortletContainerException if the descriptor can not be found or parsed
     */
    public synchronized PortletApplicationDefinition getPortletAppDD(ServletContext servletContext, String name, String contextPath)
            throws PortletContainerException {
        PortletApplicationDefinition portletApp = this.portletAppDefinitionCache.get(servletContext);
        if (portletApp == null) {
            portletApp = createDefinition(servletContext, name, contextPath);
            this.portletAppDefinitionCache.put(servletContext, portletApp);
        }
        return portletApp;
    }

    // Private Methods ---------------------------------------------------------

    /**
     * Creates the portlet.xml deployment descriptor representation.
     *
     * @param servletContext  the servlet context for which the DD is requested.
     * @return the Portlet Application Deployment Descriptor.
     * @throws PortletContainerException
     */
    private PortletApplicationDefinition createDefinition(ServletContext servletContext, String name, String contextPath)
            throws PortletContainerException {
        PortletApplicationDefinition portletApp = null;
        try {
            InputStream paIn = servletContext.getResourceAsStream(PORTLET_XML);
            InputStream webIn = servletContext.getResourceAsStream(WEB_XML);
            if (paIn == null) {
                throw new PortletContainerException("Cannot find '" + PORTLET_XML
                        + "'. Are you sure it is in the deployed package?");
            }
            if (webIn == null) {
                throw new PortletContainerException("Cannot find '" + WEB_XML
                        + "'. Are you sure it is in the deployed package?");
            }
            portletApp = this.portletAppDescriptorService.read(name, contextPath, paIn);
            this.portletAppDescriptorService.mergeWebDescriptor(portletApp, webIn);
        }
        catch (Exception ex) {
            throw new PortletContainerException("Exception loading portlet descriptor for: " + servletContext.getServletContextName(), ex);
        }
        return portletApp;
    }
}
