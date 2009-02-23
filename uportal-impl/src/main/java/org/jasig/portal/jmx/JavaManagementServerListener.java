/* Copyright 2006 The JA-SIG Collaborative.  All rights reserved.
 *  See license distributed with this file and
 *  available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.jmx;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A Listener that wraps a {@link JavaManagementServerBean}. Please look at its documentation
 * for the appropriate system properties to set.
 * <br>
 * <br>
 * The ports are configured via &lt;context-param&gt; elements in the web.xml<br>
 * The <code>org.jasig.portal.servlet.JavaManagementServerListener.portOne</code> context-param
 * is <b>required</b>. The <code>org.jasig.portal.servlet.JavaManagementServerListener.portTwo</code>
 * context-param is optional, if not specified portTwo is calculated as portOne + 1.
 * <br>
 * <br>
 * If a failure to start or stop the JMX server should cause the listener to throw an exception set
 * the context-param <code>org.jasig.portal.servlet.JavaManagementServerListener.failOnException</code>
 * to true.
 * 
 * @author Eric Dalquist <a href="mailto:eric.dalquist@doit.wisc.edu">eric.dalquist@doit.wisc.edu</a>
 * @version $Revision$
 */
public class JavaManagementServerListener implements ServletContextListener {
    //Init-parameters
    public static final String JMX_RMI_HOST         = "org.jasig.portal.servlet.JavaManagementServerListener.host";
    public static final String JMX_RMI_PORT_1       = "org.jasig.portal.servlet.JavaManagementServerListener.portOne";
    public static final String JMX_RMI_PORT_2       = "org.jasig.portal.servlet.JavaManagementServerListener.portTwo";
    public static final String FAIL_ON_EXCEPTION    = "org.jasig.portal.servlet.JavaManagementServerListener.failOnException";
    
    protected final Log logger = LogFactory.getLog(this.getClass());

    private JavaManagementServerBean javaManagementServerBean;
    

    /**
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent event) {
        final ServletContext servletContext = event.getServletContext();
        
        //Create the bean
        this.javaManagementServerBean = new JavaManagementServerBean();
        
        //Get the failOnException option
        final String failOnExceptionStr = servletContext.getInitParameter(FAIL_ON_EXCEPTION);
        boolean failOnException = Boolean.parseBoolean(failOnExceptionStr);
        this.javaManagementServerBean.setFailOnException(failOnException);
        
        final String host = servletContext.getInitParameter(JMX_RMI_HOST);
        this.javaManagementServerBean.setHost(host);
        
        //Get the base rmi port from the init parameters
        final String portOneStr = servletContext.getInitParameter(JMX_RMI_PORT_1);
        try {
            final int portOne = Integer.parseInt(portOneStr);
            this.javaManagementServerBean.setPortOne(portOne);
        }
        catch (NumberFormatException nfe) {
            this.logger.warn("init-parameter '" + JMX_RMI_PORT_1 + "' is required and must contain a number. '" + portOneStr + "' is not a valid number.", nfe);
        }
        
        //Get the second rmi port from the init parameters
        final String portTwoStr = servletContext.getInitParameter(JMX_RMI_PORT_2);
        try {
            final int portTwo = Integer.parseInt(portTwoStr);
            this.javaManagementServerBean.setPortTwo(portTwo);
        }
        catch (NumberFormatException nfe) {
            this.logger.debug("Failed to convert init-parameter '" + JMX_RMI_PORT_2 + "' with value '" + portTwoStr + "' to a number, defaulting portTwo to portOne + 1", nfe);
        }
        
        this.javaManagementServerBean.startServer();
    }

    /**
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent event) {
        this.javaManagementServerBean.stopServer();
        this.javaManagementServerBean = null;
    }
}
