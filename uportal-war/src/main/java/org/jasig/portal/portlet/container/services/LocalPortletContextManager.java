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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

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
import org.apache.pluto.container.util.ClasspathScanner;
import org.apache.pluto.container.util.StringManager;
import org.apache.pluto.driver.container.ApplicationIdResolver;
import org.apache.pluto.driver.container.Configuration;
import org.apache.pluto.driver.container.DriverPortletConfigImpl;
import org.apache.pluto.driver.container.DriverPortletContextImpl;
import org.apache.pluto.driver.container.PortletContextManager;
import org.apache.pluto.driver.container.PortletDescriptorRegistry;
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

    /** Exception Messages. */
    private static final StringManager EXCEPTIONS = StringManager.getManager(
            PortletDescriptorRegistry.class.getPackage().getName());

	/**
     * Logger Instance
     */
    protected static final Log logger = LogFactory.getLog(PortletContextManager.class);

    /**
     * The PortletContext cache map: key is servlet context, and value is the
     * associated portlet context.
     */
    private Map<String,DriverPortletContext> portletContexts = new HashMap<String,DriverPortletContext>();

    /**
     * List of application id resolvers. *
     */
    private static final List<ApplicationIdResolver> APP_ID_RESOLVERS = new ArrayList<ApplicationIdResolver>();


    // Private Member Variables ------------------------------------------------

    /**
     * The PortletContext cache map: key is servlet context, and value is the
     * associated portlet context.
     */
    private final Map<String,DriverPortletConfig> portletConfigs = new HashMap<String,DriverPortletConfig>();


    /**
     * The registered listeners that should be notified upon
     * registry events.
     */
    private final List<PortletRegistryListener> registryListeners = new ArrayList<PortletRegistryListener>();

    /**
     * The classloader for the portal, key is portletWindow and value is the classloader.
     * TODO this looks like a horrible memory leak
     */
    private final Map<String,ClassLoader> classLoaders = new HashMap<String,ClassLoader>();
    
    /**
     * Cache of descriptors.  WeakHashMap is used so that
     * once the context is destroyed (kinda), the cache is eliminated.
     * Ideally we'd use a ServletContextListener, but at this
     * point I'm wondering if we really want to add another
     * config requirement in the servlet xml? Hmm. . .
     */
    private final Map<ServletContext, PortletApplicationDefinition> portletAppDefinitionCache = new WeakHashMap<ServletContext, PortletApplicationDefinition>();
    
    private RequestDispatcherService requestDispatcherService;
    private PortletAppDescriptorService portletAppDescriptorService = new PortletAppDescriptorServiceImpl();

    /**
	 * @param requestDispatcherService the requestDispatcherService to set
	 */
    @Autowired
	public void setRequestDispatcherService(
			RequestDispatcherService requestDispatcherService) {
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
	public String register(ServletConfig config) throws PortletContainerException {
	    ServletContext servletContext = config.getServletContext();
	    String contextPath = getContextPath(servletContext);
        String applicationName = contextPath.substring(1);
        if (!portletContexts.containsKey(applicationName)) {

            PortletApplicationDefinition portletApp = this.getPortletAppDD(servletContext, applicationName, contextPath);

            DriverPortletContext portletContext = new DriverPortletContextImpl(servletContext, portletApp, requestDispatcherService);

            portletContexts.put(applicationName, portletContext);

            fireRegistered(portletContext);

            if (logger.isInfoEnabled()) {
                logger.info("Registered portlet application for context '/" + applicationName + "'");

                logger.info("Registering "+portletApp.getPortlets().size()+" portlets for context /"+portletContext.getApplicationName());
            }

            classLoaders.put(portletApp.getName(), Thread.currentThread().getContextClassLoader());
            for (PortletDefinition portlet: portletApp.getPortlets()) {
                String appName = portletContext.getApplicationName();
                if (appName == null) {
                    throw new PortletContainerException("Portlet application name should not be null.");
                }
                portletConfigs.put(
                    portletContext.getApplicationName() + "/" + portlet.getPortletName(),
                    new DriverPortletConfigImpl(portletContext, portlet)
                );
            }
        } else {
             if (logger.isInfoEnabled()) {
                logger.info("Portlet application for context '/" + applicationName + "' already registered.");
            }
        }
        return applicationName;
    }

    

	/**
     * @see org.apache.pluto.container.driver.PortletContextService#unregister(org.apache.pluto.container.driver.DriverPortletContext)
     */
    public void unregister(DriverPortletContext context) {
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

    /**
     * @see org.apache.pluto.container.driver.PortletRegistryService#getRegisteredPortletApplicationNames()
     */
    public Iterator<String> getRegisteredPortletApplicationNames() {
        return new HashSet<String>(portletContexts.keySet()).iterator();
    }

    /**
     * @see org.apache.pluto.container.driver.PortletContextService#getPortletContexts()
     */
    public Iterator<DriverPortletContext> getPortletContexts() {
        return new HashSet<DriverPortletContext>(portletContexts.values()).iterator();
    }

    /**
     * @see org.apache.pluto.container.driver.PortletContextService#getPortletContext(java.lang.String)
     */
    public DriverPortletContext getPortletContext(String applicationName) {
        return portletContexts.get(applicationName);
    }

    /**
     * @see org.apache.pluto.container.driver.PortletContextService#getPortletContext(org.apache.pluto.container.PortletWindow)
     */
    public DriverPortletContext getPortletContext(PortletWindow portletWindow) throws PortletContainerException {
        return portletContexts.get(portletWindow.getPortletDefinition().getApplication().getName());
    }


    /**
     * @see org.apache.pluto.container.driver.PortletContextService#getPortletConfig(java.lang.String, java.lang.String)
     */
    public DriverPortletConfig getPortletConfig(String applicationName, String portletName) throws PortletContainerException {
        DriverPortletConfig ipc = portletConfigs.get(applicationName + "/" + portletName);
        if (ipc != null) {
            return ipc;
        }
        String msg = "Unable to locate portlet config [applicationName="+applicationName+"]/["+portletName+"].";
        logger.warn(msg);
        throw new PortletContainerException(msg);
    }

    /**
     * @see org.apache.pluto.container.driver.PortletRegistryService#getPortlet(java.lang.String, java.lang.String)
     */
    public PortletDefinition getPortlet(String applicationName, String portletName) throws PortletContainerException {
        DriverPortletConfig ipc = portletConfigs.get(applicationName + "/" + portletName);
        if (ipc != null) {
            return ipc.getPortletDefinition();
        }
        String msg = "Unable to retrieve portlet: '"+applicationName+"/"+portletName+"'";
        logger.warn(msg);
        throw new PortletContainerException(msg);
    }

    /**
     * @see org.apache.pluto.container.driver.PortletRegistryService#getPortletApplication(java.lang.String)
     */
    public PortletApplicationDefinition getPortletApplication(String applicationName) throws PortletContainerException {
        DriverPortletContext ipc = portletContexts.get(applicationName);
        if (ipc != null) {
            return ipc.getPortletApplicationDefinition();
        }
        String msg = "Unable to retrieve portlet application: '"+applicationName+"'";
        logger.warn(msg);
        throw new PortletContainerException(msg);
    }

    /**
     * @see org.apache.pluto.container.driver.PortletContextService#getClassLoader(java.lang.String)
     */
    public ClassLoader getClassLoader(String applicationName){
    	return classLoaders.get(applicationName);
    }

    /**
     * @see org.apache.pluto.container.driver.PortletRegistryService#addPortletRegistryListener(org.apache.pluto.container.driver.PortletRegistryListener)
     */
    public void addPortletRegistryListener(PortletRegistryListener listener) {
        registryListeners.add(listener);
    }

    /**
     * @see org.apache.pluto.container.driver.PortletRegistryService#removePortletRegistryListener(org.apache.pluto.container.driver.PortletRegistryListener)
     */
    public void removePortletRegistryListener(PortletRegistryListener listener) {
        registryListeners.remove(listener);
    }

    private void fireRegistered(DriverPortletContext context) {
        PortletRegistryEvent event = new PortletRegistryEvent();
        event.setPortletApplication(context.getPortletApplicationDefinition());

        for (PortletRegistryListener l: registryListeners) {
            l.portletApplicationRegistered(event);
        }
        logger.info("Portlet Context '/" + context.getApplicationName() + "' registered.");
    }

    private void fireRemoved(DriverPortletContext context) {
        PortletRegistryEvent event = new PortletRegistryEvent();
        event.setPortletApplication(context.getPortletApplicationDefinition());

        for (PortletRegistryListener l: registryListeners) {
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
    public PortletApplicationDefinition getPortletAppDD(ServletContext servletContext, String name, String contextPath)
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
                throw new PortletContainerException("Cannot find '" + PORTLET_XML +
                    "'. Are you sure it is in the deployed package?");
            }
            if (webIn == null) {
                throw new PortletContainerException("Cannot find '" + WEB_XML +
                    "'. Are you sure it is in the deployed package?");
            }
            portletApp = this.portletAppDescriptorService.read(name, contextPath, paIn);
            this.portletAppDescriptorService.mergeWebDescriptor(portletApp, webIn);
        } catch (Exception ex) {
            throw new PortletContainerException(EXCEPTIONS.getString(
                    "error.context.descriptor.load",
                    new String[] { servletContext.getServletContextName() }),
                    ex);
        }
        return portletApp;
    }

//
// Utility

    /**
     * Retrieve the servlet context of the portlet web app.
     * @param portalContext The servlet context of the portal web app.
     * @param portletContextPath The context path of the portlet web app.
     * The given path must be begin with "/" (see {@link ServletContext#getContext(String)}).
     * @return The servlet context of the portlet web app.
     * @throws PortletContainerException if the servlet context cannot be
     * retrieved for the given context path
     */
    public static ServletContext getPortletContext(ServletContext portalContext,
        String portletContextPath) throws PortletContainerException {
        if (Configuration.preventUnecessaryCrossContext()) {
            String portalPath = getContextPath(portalContext);
            if (portalPath.equals(portletContextPath)) {
                return portalContext;
            }
        }

        //Hack to deal with inconsistence in root context handling between
        //ServletContext.getContextPath and ServletContext.getContext
        if ("".equals(portletContextPath)) {
            portletContextPath = "/";
        }
        ServletContext portletAppCtx = portalContext.getContext(portletContextPath);
        if (portletAppCtx == null) {
            final String msg = "Unable to obtain the servlet context for the " +
              "portlet app context path [" + portletContextPath + "]. Make " +
              "sure that the portlet app has been deployed and that cross " +
              "context support is enabled for the portal app.";
            throw new PortletContainerException(msg);
        }
        return portletAppCtx;
    }

    /**
     * Servlet 2.5 ServletContext.getContextPath() method.
     */
    private static Method contextPathGetter;

    static {
        try {
            contextPathGetter = ServletContext.class.getMethod("getContextPath", (Class[])null);
        }
        catch (NoSuchMethodException e) {
            logger.warn("Servlet 2.4 or below detected.  Unable to find getContextPath on ServletContext.");
        }
    }

    protected static String getContextPath(ServletContext context) {
        String contextPath = null;
        if (contextPathGetter != null) {
            try {
                contextPath = (String) contextPathGetter.invoke(context, (Object[])null);
            } catch (Exception e) {
                logger.warn("Unable to directly retrieve context path from ServletContext. Computing. . . ");
            }
        }

        if (contextPath == null) {
            contextPath = computeContextPath(context);
        }

        return contextPath;
    }


    @SuppressWarnings("unchecked")
    protected static String computeContextPath(ServletContext context) {
        if (APP_ID_RESOLVERS.size() < 1) {
            List<Class> classes = null;
            try {
                classes = ClasspathScanner.findConfiguredImplementations(ApplicationIdResolver.class);
            } catch (IOException e) {
                throw new RuntimeException("Unable to find any ApplicationIdResolvers");
            }
            for (Class c : classes) {
                try {
                    APP_ID_RESOLVERS.add((ApplicationIdResolver)c.newInstance());
                } catch (Exception e) {
                    logger.warn("Unable to instantiate ApplicationIdResolver for class " + c.getName());
                }
            }
            if (logger.isInfoEnabled()) {
                logger.info("Found " + APP_ID_RESOLVERS.size() + " application id resolvers.");
            }
        }

        String path = null;
        int authority = Integer.MAX_VALUE;

        for (ApplicationIdResolver resolver : APP_ID_RESOLVERS) {
            if (resolver.getAuthority() < authority || path == null) {
                authority = resolver.getAuthority();
                String temp = resolver.resolveApplicationId(context);
                if (temp != null) {
                    path = temp;
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Resolved application id '" + path + "' with authority " + authority);
        }
        return path;
    }

}
