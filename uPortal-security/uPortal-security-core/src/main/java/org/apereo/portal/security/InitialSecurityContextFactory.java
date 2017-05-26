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
package org.apereo.portal.security;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class provides a static "factory" method that returns a security context retrieved based on
 * the information provided in security.properties, including all relevant subcontexts. A typical
 * sequence would be:
 *
 * <pre>
 * SecurityContext sec = InitialSecurityContextFactory.getInitialContext("root");
 * Principal princ = sec.getPrincipalInstance();
 * OpaqueCredentials pwd = sec.getOpaqueCredentialsInstance();
 * princ.setUID("user");
 * pwd.setCredentials("password");
 * sec.authenticate();
 * if (sec.isAuthenticated())
 *  System.out.println("Yup");
 * else
 *  System.out.println("Nope");
 * </pre>
 *
 */
public class InitialSecurityContextFactory {

    private static final Log log = LogFactory.getLog(InitialSecurityContextFactory.class);
    private static final String CONTEXT_PROPERTY_PREFIX = "securityContextProperty";

    /**
     * Used to store the configuration for each initial context, this allows a the ISecurityContext
     * chain to be created more quickly since the properties file doesn't need to be parsed at each
     * getInitialContext call.
     */
    private static final Map contextConfigCache = new Hashtable();

    public static ISecurityContext getInitialContext(final String rootContext)
            throws PortalSecurityException {
        BaseContextConfiguration contextConfigBase;

        /*
         * Synchronize on the contextConfigCache Map, this ensures two threads don't
         * end up creating the same BaseContextConfiguration.
         */
        synchronized (contextConfigCache) {
            contextConfigBase = (BaseContextConfiguration) contextConfigCache.get(rootContext);

            //The desired base context configuraton doesn't exist
            if (contextConfigBase == null) {

                //Initial contexts must have names that are not compound
                if (rootContext.indexOf('.') != -1) {
                    PortalSecurityException ep =
                            new PortalSecurityException("Initial Context can't be compound");
                    log.error("Initial context cannot be compound", ep);
                    throw (ep);
                }

                contextConfigBase = new BaseContextConfiguration();
                contextConfigCache.put(rootContext, contextConfigBase);
            }
        }

        /*
         * Changing the synchronized object will minimize blocking in the case
         * of different root contexts. The config initialization code is thread
         * safe as long as each thread is initializing a different config.
         */

        /*
         * Synchronize on the contextConfig, this ensures two threads don't
         * try to intialize the config in parallel. Only one will be allowed
         * into the synchronized block at a time, if the config has not been
         * initialized it will initialize it and set the flag to true then
         * exit the block. The waiting thread(s) will have a reference to a
         * now initialized config and skip the initalization code.
         */
        synchronized (contextConfigBase) {
            if (!contextConfigBase.initialized) {
                //Try to load the properties
                final Properties securityProperties = new Properties();
                InputStream securityPropertiesStream = null;

                try {
                    securityPropertiesStream =
                            InitialSecurityContextFactory.class.getResourceAsStream(
                                    "/properties/security.properties");
                    securityProperties.load(securityPropertiesStream);
                } catch (IOException e) {
                    PortalSecurityException ep = new PortalSecurityException(e);
                    log.error("Exception loading security properties", ep);
                    throw (ep);
                } finally {
                    try {
                        if (securityPropertiesStream != null) {
                            securityPropertiesStream.close();
                        }
                    } catch (IOException ioe) {
                        log.error("getInitialContext() unable to close InputStream", ioe);
                    }
                }

                //Load the context configurations
                contextConfigBase.rootConfig =
                        loadContextConfigurationChain(rootContext, securityProperties);
                contextConfigBase.initialized = true;
            }
        }

        //Should have a valid contextConfig here
        try {
            //Create the context tree
            ISecurityContext ctx = createSecurityContextChain(contextConfigBase.rootConfig);
            return ctx;
        } catch (NullPointerException npe) {
            String errorMsg = "Error while creating ISecurityContext chain.";
            PortalSecurityException ep = new PortalSecurityException(errorMsg, npe);
            log.error(ep, ep);
            throw ep;
        }
    }

    /**
     * Recursively parses the tree of {@link ContextConfiguration} objects to create a tree (chain)
     * of {@link ISecurityContext}s. The root context is returned by the method after all of it's
     * sub-contexts have been created and configured.
     *
     * @param contextConfig The {@link ContextConfiguration} to use as the root
     * @return A configured {@link ISecurityContext}
     * @throws PortalSecurityException If an excetion is thrown by the {@link
     *     ISecurityContext#addSubContext(String, ISecurityContext)} method.
     */
    private static ISecurityContext createSecurityContextChain(
            final ContextConfiguration contextConfig) throws PortalSecurityException {
        final ISecurityContext securityContext = contextConfig.contextFactory.getSecurityContext();

        //If it is a configurable SecurityContext pass in the properties
        if (securityContext instanceof IConfigurableSecurityContext) {
            ((IConfigurableSecurityContext) securityContext)
                    .setProperties((Properties) contextConfig.contextProperties.clone());
        }

        //Create all the sub contexts
        for (int index = 0; index < contextConfig.subConfigs.length; index++) {
            final ISecurityContext subSecurityContext =
                    createSecurityContextChain(contextConfig.subConfigs[index]);

            securityContext.addSubContext(
                    contextConfig.subConfigs[index].contextName, subSecurityContext);
        }

        return securityContext;
    }

    /**
     * This method parses the {@link Properties} file to find the configuration for the specified
     * context. The factory is loaded and the configuration is named then the {@link Properties} are
     * parsed to find all context configuration properties and sub-contexts for this context. For
     * each sub-context this method is called recursively.
     *
     * @param fullContextName The fully qualified name of the context to configure.
     * @param securtiyProperties The {@link Properties} to use for configuration.
     * @return A fully configured {@link ContextConfiguration} object.
     * @throws PortalSecurityException If no context with the specified named exists or the factory
     *     cannot be created.
     */
    private static ContextConfiguration loadContextConfigurationChain(
            final String fullContextName, final Properties securtiyProperties)
            throws PortalSecurityException {
        //Load the context factory name
        final String factoryName = securtiyProperties.getProperty(fullContextName);
        if (factoryName == null) {
            final PortalSecurityException ep =
                    new PortalSecurityException("No such security context " + fullContextName);
            log.error(ep.getMessage(), ep);
            throw (ep);
        }

        //The contextConfig this method will return
        final ContextConfiguration contextConfig = new ContextConfiguration();

        final int lastDotIndex = fullContextName.lastIndexOf(".");
        String localContextName = fullContextName;
        if (lastDotIndex >= 0) {
            try {
                localContextName = fullContextName.substring(lastDotIndex + 1);
            } catch (IndexOutOfBoundsException ioobe) {
                final PortalSecurityException pse =
                        new PortalSecurityException(
                                "Invalid context name " + fullContextName, ioobe);
                log.error(pse.getMessage(), pse);
                throw pse;
            }
        }

        //Create the context factory
        try {
            final ISecurityContextFactory factory =
                    (ISecurityContextFactory) Class.forName(factoryName).newInstance();
            contextConfig.contextFactory = factory;
            contextConfig.contextName = localContextName;
        } catch (Exception e) {
            final PortalSecurityException ep =
                    new PortalSecurityException("Failed to instantiate " + factoryName);
            log.error("Failed to instantiate " + factoryName, e);
            throw (ep);
        }

        //Just move this string concatination out of the loop to save cycles
        final String contextConfigPropertyPrefix = CONTEXT_PROPERTY_PREFIX + "." + fullContextName;

        //Read sub context names & properties for this context
        final Collection subContexts = new Vector();
        for (final Enumeration ctxnames = securtiyProperties.propertyNames();
                ctxnames.hasMoreElements();
                ) {
            final String securityPropName = (String) ctxnames.nextElement();

            if (securityPropName.startsWith(fullContextName)
                    && securityPropName.length() > fullContextName.length()
                    && securityPropName.indexOf(".", fullContextName.length() + 1) < 0) {

                //call getContextConfiguration(name) on each
                final ContextConfiguration subContextConfig =
                        loadContextConfigurationChain(securityPropName, securtiyProperties);
                //add context to list for this context
                subContexts.add(subContextConfig);
            }
            //Context configuration properties as securityContextProperty. entries
            // Format is:
            //  securityContextProperty.<SecurityContextName>.<PropertyName>
            //  <PropertyName> cannot contain .
            else if (securityPropName.startsWith(contextConfigPropertyPrefix)
                    && securityPropName.length() > contextConfigPropertyPrefix.length()
                    && securityPropName.indexOf(".", contextConfigPropertyPrefix.length() + 1)
                            < 0) {

                final String propValue = securtiyProperties.getProperty(securityPropName);
                final String propName =
                        securityPropName.substring(contextConfigPropertyPrefix.length() + 1);

                contextConfig.contextProperties.setProperty(propName, propValue);
            }
        }

        //Set the sub contexts into this context
        contextConfig.subConfigs =
                (ContextConfiguration[])
                        subContexts.toArray(new ContextConfiguration[subContexts.size()]);

        return contextConfig;
    }
}

class BaseContextConfiguration {
    ContextConfiguration rootConfig = null;
    boolean initialized = false;
}

class ContextConfiguration {
    ISecurityContextFactory contextFactory = null;
    String contextName = null;
    final Properties contextProperties = new Properties();
    ContextConfiguration[] subConfigs = null;
}
