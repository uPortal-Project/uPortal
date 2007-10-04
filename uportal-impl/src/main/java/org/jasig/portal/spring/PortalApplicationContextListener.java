/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.spring;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang.Validate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Tracks the {@link ServletContext} in a static field to provide to the
 * {@link WebApplicationContextUtils} to load a {@link WebApplicationContext}.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 * @deprecated This class is a work-around for areas in uPortal that do not have the ability to use the {@link WebApplicationContextUtils} directly.
 */
@Deprecated
public class PortalApplicationContextListener implements ServletContextListener {
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
    
    public static WebApplicationContext getRequiredWebApplicationContext() {
        Validate.notNull(servletContext, "No ServletContext is available to load a WebApplicationContext for. Is this ServletContextListener not configured or has the ServletContext been destroyed?");
        
        return WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
    }

    public static WebApplicationContext getWebApplicationContext() {
        if (servletContext == null) {
            return null;
        }
        
        return WebApplicationContextUtils.getWebApplicationContext(servletContext);
    }
}
