/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.spring;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Locates the {@link ApplicationContext} for the portal when running as both a
 * command line application and a web application. 
 * 
 * Tracks the {@link ServletContext} in a static field to provide to the
 * {@link WebApplicationContextUtils} to load a {@link WebApplicationContext}.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 * @deprecated This class is a work-around for areas in uPortal that do not have the ability to use the {@link WebApplicationContextUtils} directly.
 */
@Deprecated
public class PortalApplicationContextLocator implements ServletContextListener {
    private static ApplicationContext applicationContext;
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
     */
    public static boolean isRunningInWebApplication() {
        return servletContext != null;
    }
    
    /**
     * @return The WebApplicationContext for the portal
     * @throws IllegalStateException if no ServletContext is available to retrieve a WebApplicationContext for or if the root WebApplicationContext could not be found
     */
    public static WebApplicationContext getRequiredWebApplicationContext() {
        if (!isRunningInWebApplication()) {
            throw new IllegalStateException("No ServletContext is available to load a WebApplicationContext for. Is this ServletContextListener not configured or has the ServletContext been destroyed?");
        }
        
        return WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
    }

    /**
     * @return The WebApplicationContext for the portal, null if no ServletContext is available
     */
    public static WebApplicationContext getWebApplicationContext() {
        if (!isRunningInWebApplication()) {
            return null;
        }
        
        return WebApplicationContextUtils.getWebApplicationContext(servletContext);
    }

    /**
     * If running in a web application the existing WebApplicaitonContext will be returned. if
     * not a singleton ApplicationContext is created if needed and returned. Unless a WebApplicationContext
     * is specifically needed this method should be used as it will work both when running in and out
     * of a web application
     * 
     * @return The ApplicationContext for the portal. 
     */
    public static ApplicationContext getApplicationContext() {
        if (isRunningInWebApplication()) {
            return getWebApplicationContext();
        }
        
        synchronized (PortalApplicationContextLocator.class) {
            if (applicationContext == null) {
                System.err.println("***** ***** CREATING NEW APPLICATION CONTEXT ***** *****");
                applicationContext = new ClassPathXmlApplicationContext("/properties/contexts/*.xml"); 
            }
        }
        
        return applicationContext;
    }
}
