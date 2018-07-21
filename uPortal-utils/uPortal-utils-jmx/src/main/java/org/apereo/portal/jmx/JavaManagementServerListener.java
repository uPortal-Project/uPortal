/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.jmx;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A Listener that wraps a {@link JavaManagementServerBean}. Please look at its documentation for
 * the appropriate system properties to set. <br>
 * <br>
 * The ports are configured via &lt;context-param&gt; elements in the web.xml<br>
 * The <code>org.apereo.portal.servlet.JavaManagementServerListener.portOne</code> context-param is
 * <b>required</b>. The <code>org.apereo.portal.servlet.JavaManagementServerListener.portTwo</code>
 * context-param is optional, if not specified portTwo is calculated as portOne + 1. <br>
 * <br>
 * If a failure to start or stop the JMX server should cause the listener to throw an exception set
 * the context-param <code>org.apereo.portal.servlet.JavaManagementServerListener.failOnException
 * </code> to true.
 */
public class JavaManagementServerListener implements ServletContextListener {
    // Init-parameters
    public static final String JMX_RMI_HOST =
            "org.apereo.portal.servlet.JavaManagementServerListener.host";
    public static final String JMX_RMI_PORT_1 =
            "org.apereo.portal.servlet.JavaManagementServerListener.portOne";
    public static final String JMX_RMI_PORT_2 =
            "org.apereo.portal.servlet.JavaManagementServerListener.portTwo";
    public static final String FAIL_ON_EXCEPTION =
            "org.apereo.portal.servlet.JavaManagementServerListener.failOnException";

    private static final String LOGGER_NAME = JavaManagementServerListener.class.getName();

    private JavaManagementServerBean javaManagementServerBean;

    private Log logger;

    /**
     * Inits and/or returns already initialized logger. <br>
     * You have to use this method in order to use the logger,<br>
     * you should not call the private variable directly.<br>
     * This was done because Tomcat may instantiate all listeners before calling contextInitialized
     * on any listener.<br>
     * Note that there is no synchronization here on purpose. The object returned by getLog for a
     * logger name is<br>
     * idempotent and getLog itself is thread safe. Eventually all <br>
     * threads will see an instance level logger variable and calls to getLog will stop.
     *
     * @return the log for this class
     */
    protected Log getLogger() {
        Log l = this.logger;
        if (l == null) {
            l = LogFactory.getLog(LOGGER_NAME);
            this.logger = l;
        }
        return l;
    }

    /**
     * @see
     *     javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextInitialized(ServletContextEvent event) {
        final ServletContext servletContext = event.getServletContext();

        // Create the bean
        this.javaManagementServerBean = new JavaManagementServerBean();

        // Get the failOnException option
        final String failOnExceptionStr = servletContext.getInitParameter(FAIL_ON_EXCEPTION);
        boolean failOnException = Boolean.parseBoolean(failOnExceptionStr);
        this.javaManagementServerBean.setFailOnException(failOnException);

        final String host = servletContext.getInitParameter(JMX_RMI_HOST);
        this.javaManagementServerBean.setHost(host);

        // Get the base rmi port from the init parameters
        final String portOneStr = servletContext.getInitParameter(JMX_RMI_PORT_1);
        try {
            final int portOne = Integer.parseInt(portOneStr);
            this.javaManagementServerBean.setPortOne(portOne);
        } catch (NumberFormatException nfe) {
            getLogger()
                    .warn(
                            "init-parameter '"
                                    + JMX_RMI_PORT_1
                                    + "' is required and must contain a number. '"
                                    + portOneStr
                                    + "' is not a valid number.",
                            nfe);
        }

        // Get the second rmi port from the init parameters
        final String portTwoStr = servletContext.getInitParameter(JMX_RMI_PORT_2);
        try {
            final int portTwo = Integer.parseInt(portTwoStr);
            this.javaManagementServerBean.setPortTwo(portTwo);
        } catch (NumberFormatException nfe) {
            getLogger()
                    .debug(
                            "Failed to convert init-parameter '"
                                    + JMX_RMI_PORT_2
                                    + "' with value '"
                                    + portTwoStr
                                    + "' to a number, defaulting portTwo to portOne + 1",
                            nfe);
        }

        this.javaManagementServerBean.startServer();
    }

    /**
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextDestroyed(ServletContextEvent event) {
        this.javaManagementServerBean.stopServer();
        this.javaManagementServerBean = null;
    }
}
