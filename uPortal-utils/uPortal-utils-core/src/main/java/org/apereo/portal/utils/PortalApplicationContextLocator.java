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
package org.apereo.portal.utils;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.utils.threading.SingletonDoubleCheckedCreator;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Provides standard access to the portal's {@link ApplicationContext}. If running in a web
 * application a {@link WebApplicationContext} is available.
 *
 * <p>{@link #getApplicationContext()} should be used by most uPortal code that needs access to the
 * portal's {@link ApplicationContext}. It ensures that a single {@link ApplicationContext} is used
 * portal-wide both when the portal is running as a web-application and when tools are run from the
 * command line.
 *
 * <p>For legacy portal code that is not yet Spring managed and does not have access to the {@link
 * ServletContext} this class provides similar functionality to {@link WebApplicationContextUtils}
 * via the {@link #getWebApplicationContext()} and {@link #getRequiredWebApplicationContext()}.
 * These methods are deprecated as any code that requires a {@link WebApplicationContext} should
 * either be refactored as a Spring managed bean or have access to the {@link ServletContext}
 */
public class PortalApplicationContextLocator implements ServletContextListener {
    private static String LOGGER_NAME = PortalApplicationContextLocator.class.getName();

    private static Log logger;

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
    protected static Log getLogger() {
        Log l = logger;
        if (l == null) {
            l = LogFactory.getLog(LOGGER_NAME);
            logger = l;
        }
        return l;
    }

    private static final SingletonDoubleCheckedCreator<ConfigurableApplicationContext>
            applicationContextCreator = new PortalApplicationContextCreator();
    private static Throwable directCreatorThrowable;
    private static ServletContext servletContext;

    /* (non-Javadoc)
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        servletContext = sce.getServletContext();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        servletContext = null;
    }

    /**
     * @return <code>true</code> if a WebApplicationContext is available, <code>false</code> if only
     *     an ApplicationContext is available
     * @deprecated Only needed for using {@link #getRequiredWebApplicationContext()} or {@link
     *     #getWebApplicationContext()}.
     */
    @Deprecated
    public static boolean isRunningInWebApplication() {
        return servletContext != null;
    }

    /**
     * @return The WebApplicationContext for the portal
     * @throws IllegalStateException if no ServletContext is available to retrieve a
     *     WebApplicationContext for or if the root WebApplicationContext could not be found
     * @deprecated This method is a work-around for areas in uPortal that do not have the ability to
     *     use the {@link
     *     WebApplicationContextUtils#getRequiredWebApplicationContext(ServletContext)} directly.
     */
    @Deprecated
    public static WebApplicationContext getRequiredWebApplicationContext() {
        final ServletContext context = servletContext;
        if (context == null) {
            throw new IllegalStateException(
                    "No ServletContext is available to load a WebApplicationContext for. Is this ServletContextListener not configured or has the ServletContext been destroyed?");
        }

        return WebApplicationContextUtils.getRequiredWebApplicationContext(context);
    }

    /**
     * @return The WebApplicationContext for the portal, null if no ServletContext is available
     * @deprecated This method is a work-around for areas in uPortal that do not have the ability to
     *     use the {@link WebApplicationContextUtils#getWebApplicationContext(ServletContext)}
     *     directly.
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
     * If running in a web application the existing {@link WebApplicationContext} will be returned.
     * if not a singleton {@link ApplicationContext} is created if needed and returned. Unless a
     * {@link WebApplicationContext} is specifically needed this method should be used as it will
     * work both when running in and out of a web application
     *
     * @return The {@link ApplicationContext} for the portal.
     */
    public static ApplicationContext getApplicationContext() {
        final ServletContext context = servletContext;

        if (context != null) {
            getLogger().debug("Using WebApplicationContext");

            if (applicationContextCreator.isCreated()) {
                final IllegalStateException createException =
                        new IllegalStateException(
                                "A portal managed ApplicationContext has already been created but now a ServletContext is available and a WebApplicationContext will be returned. "
                                        + "This situation should be resolved by delaying calls to this class until after the web-application has completely initialized.");
                getLogger().error(createException, createException);
                getLogger()
                        .error(
                                "Stack trace of original ApplicationContext creator",
                                directCreatorThrowable);
                throw createException;
            }

            final WebApplicationContext webApplicationContext =
                    WebApplicationContextUtils.getWebApplicationContext(context);
            if (webApplicationContext == null) {
                throw new IllegalStateException(
                        "ServletContext is available but WebApplicationContextUtils.getWebApplicationContext(ServletContext) returned null. Either the application context failed to load or is not yet done loading.");
            }
            return webApplicationContext;
        }

        return applicationContextCreator.get();
    }

    /**
     * If the ApplicationContext returned by {@link #getApplicationContext()} is 'portal managed'
     * the shutdown hook for the context is called, closing and cleaning up all spring managed
     * resources.
     *
     * <p>If the ApplicationContext returned by {@link #getApplicationContext()} is actually a
     * WebApplicationContext this method does nothing but log an error message.
     */
    public static void shutdown() {

        if (applicationContextCreator.isCreated()) {
            final ConfigurableApplicationContext applicationContext =
                    applicationContextCreator.get();
            applicationContext.close();
        } else {
            final IllegalStateException createException =
                    new IllegalStateException(
                            "No portal managed ApplicationContext has been created, there is nothing to shutdown.");
            getLogger().error(createException, createException);
        }
    }

    /**
     * Creator class that knows how to instantiate the lazily initialized portal application context
     * if needed
     */
    private static class PortalApplicationContextCreator
            extends SingletonDoubleCheckedCreator<ConfigurableApplicationContext> {

        @Override
        protected ConfigurableApplicationContext createSingleton(Object... args) {

            if (Boolean.getBoolean("org.apereo.portal.test")) {
                throw new IllegalStateException(
                        PortalApplicationContextLocator.class.getName()
                                + " MUST NOT be used in unit tests");
            }

            getLogger()
                    .info(
                            "Creating new lazily initialized GenericApplicationContext for the portal");

            final long startTime = System.currentTimeMillis();

            final GenericApplicationContext genericApplicationContext =
                    new GenericApplicationContext();
            final XmlBeanDefinitionReader reader =
                    new XmlBeanDefinitionReader(genericApplicationContext);
            reader.setDocumentReaderClass(LazyInitByDefaultBeanDefinitionDocumentReader.class);
            reader.loadBeanDefinitions("/properties/contexts/*.xml");
            reader.loadBeanDefinitions("/properties/contextOverrides/*.xml");

            genericApplicationContext.refresh();
            genericApplicationContext.registerShutdownHook();

            directCreatorThrowable = new Throwable();
            directCreatorThrowable.fillInStackTrace();
            getLogger()
                    .info(
                            "Created new lazily initialized GenericApplicationContext for the portal in "
                                    + (System.currentTimeMillis() - startTime)
                                    + "ms");

            if (getLogger().isDebugEnabled()) {
                ConfigurableListableBeanFactory beanFactory =
                        genericApplicationContext.getBeanFactory();
                for (String beanName : genericApplicationContext.getBeanDefinitionNames()) {
                    getLogger()
                            .debug(
                                    "Bean Name : "
                                            + beanName
                                            + " loaded from "
                                            + beanFactory
                                                    .getBeanDefinition(beanName)
                                                    .getResourceDescription());
                }
            }

            return genericApplicationContext;
        }
    }
}
