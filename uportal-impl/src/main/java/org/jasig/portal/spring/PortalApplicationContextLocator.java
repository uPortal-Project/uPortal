/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.spring;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.utils.threading.SingletonDoubleCheckedCreator;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Provides standard access to the portal's {@link ApplicationContext}. If running in a web application a
 * {@link WebApplicationContext} is available.
 * 
 * {@link #getApplicationContext()} should be used by most uPortal code that needs access to the portal's
 * {@link ApplicationContext}. It ensures that a single {@link ApplicationContext} is used portal-wide both
 * when the portal is running as a web-application and when tools are run from the command line.
 * 
 * For legacy portal code that is not yet Spring managed and does not have access to the {@link ServletContext} this
 * class provides similar functionality to  {@link WebApplicationContextUtils} via the
 * {@link #getWebApplicationContext()} and {@link #getRequiredWebApplicationContext()}. These methods are deprecated as
 * any code that requires a {@link WebApplicationContext} should either be refactored as a Spring managed bean or have
 * access to the {@link ServletContext}
 *  
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortalApplicationContextLocator implements ServletContextListener {
    private static Log LOGGER = LogFactory.getLog(PortalApplicationContextLocator.class);
    
    private static final SingletonDoubleCheckedCreator<ApplicationContext> applicationContextCreator = new PortalApplicationContextCreator();
    private static ServletContext servletContext;

    /* (non-Javadoc)
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent sce) {
        servletContext = sce.getServletContext();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent sce) {
        servletContext = null;
    }
    
    /**
     * @return <code>true</code> if a WebApplicationContext is available, <code>false</code> if only an ApplicationContext is available
     * @deprecated Only needed for using {@link #getRequiredWebApplicationContext()} or {@link #getWebApplicationContext()}.
     */
    @Deprecated
    public static boolean isRunningInWebApplication() {
        return servletContext != null;
    }
    
    /**
     * @return The WebApplicationContext for the portal
     * @throws IllegalStateException if no ServletContext is available to retrieve a WebApplicationContext for or if the root WebApplicationContext could not be found
     * @deprecated This method is a work-around for areas in uPortal that do not have the ability to use the {@link WebApplicationContextUtils#getRequiredWebApplicationContext(ServletContext)} directly.
     */
    @Deprecated
    public static WebApplicationContext getRequiredWebApplicationContext() {
        final ServletContext context = servletContext;
        if (context == null) {
            throw new IllegalStateException("No ServletContext is available to load a WebApplicationContext for. Is this ServletContextListener not configured or has the ServletContext been destroyed?");
        }
        
        return WebApplicationContextUtils.getRequiredWebApplicationContext(context);
    }

    /**
     * @return The WebApplicationContext for the portal, null if no ServletContext is available
     * @deprecated This method is a work-around for areas in uPortal that do not have the ability to use the {@link WebApplicationContextUtils#getWebApplicationContext(ServletContext)} directly.
     */
    @Deprecated
    public static WebApplicationContext getWebApplicationContext() {
        final ServletContext context = servletContext;
        if (context == null) {
            return null;
        }
        
        return WebApplicationContextUtils.getWebApplicationContext(context);
    }

    /**
     * If running in a web application the existing {@link WebApplicationContext} will be returned. if
     * not a singleton {@link ApplicationContext} is created if needed and returned. Unless a {@link WebApplicationContext}
     * is specifically needed this method should be used as it will work both when running in and out
     * of a web application
     * 
     * @return The {@link ApplicationContext} for the portal. 
     */
    public static ApplicationContext getApplicationContext() {
        final ServletContext context = servletContext;

        if (context != null) {
            LOGGER.debug("Using WebApplicationContext");
            
            if (applicationContextCreator.isCreated()) {
                LOGGER.error("A portal managed ApplicationContext has already been created but now a ServletContext is available and a WebApplicationContext will be returned. This situation should be resolved by delaying calls to this class until after the web-application has completely initialized.");
            }
            return WebApplicationContextUtils.getWebApplicationContext(context);
        }
        
        return applicationContextCreator.get();
    }
    
    /**
     * Creator class that knows how to instatiate the lazily initialized portal application context if needed
     */
    private static class PortalApplicationContextCreator extends SingletonDoubleCheckedCreator<ApplicationContext> {
        
        @Override
        protected ApplicationContext createSingleton(Object... args) {
            LOGGER.info("Creating new lazily initialized GenericApplicationContext for the portal");

            final long startTime = System.currentTimeMillis();

            final GenericApplicationContext genericApplicationContext = new GenericApplicationContext();
            final XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(genericApplicationContext);
            reader.setDocumentReaderClass(LazyInitByDefaultBeanDefinitionDocumentReader.class);
            reader.loadBeanDefinitions("/properties/contexts/*.xml");

            genericApplicationContext.refresh();

            LOGGER.info("Created new lazily initialized GenericApplicationContext for the portal in " + (System.currentTimeMillis() - startTime) + "ms");

            return genericApplicationContext;
        }
    }
}
