/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jasig.portal.security;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class provides a static "factory" method that returns a security context
 * retrieved based on the information provided in security.properties,
 * including all relevant subcontexts.  A typical sequence would be:
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
 * @author Andrew Newman, newman@yale.edu
 * @author Susan Bramhall (susan.bramhall@yale.edu)
 * @author Shawn Bayern (shawn.bayern@yale.edu)
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$
 */

public class InitialSecurityContextFactory {
    
    private static final Log log = LogFactory.getLog(InitialSecurityContextFactory.class);
    private static final String CONTEXT_PROPERTY_PREFIX = "securityContextProperty";
    
    /**
     * Used to store the configuration for each initial context, this allows
     * a the ISecurityContext chain to be created more quickly since the
     * properties file doesn't need to be parsed at each getInitialContext call.
     */
    private static final Map contextConfigs = new Hashtable();
    
    public static ISecurityContext getInitialContext(String ctx) throws PortalSecurityException {
        BaseContextConfiguration contextConfig;

        /*
         * Synchronize on the contextConfigs Map, this ensures two threads don't
         * end up creating the same BaseContextConfiguration.
         */
        synchronized (contextConfigs) {
            contextConfig = (BaseContextConfiguration)contextConfigs.get(ctx);
    
            //The desired base context configuraton doesn't exist
            if (contextConfig == null) {
                
                //Initial contexts must have names that are not compound
                if (ctx.indexOf('.') != -1) {
                    PortalSecurityException ep = new PortalSecurityException("Initial Context can't be compound");
                    log.error(ep);
                    throw(ep);
                }
                
                contextConfig = new BaseContextConfiguration();
                contextConfigs.put(ctx, contextConfig);
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
        synchronized (contextConfig) {
            if (!contextConfig.initialized) {
                //Try to load the properties
                InputStream secprops = InitialSecurityContextFactory.class.getResourceAsStream("/properties/security.properties");
                Properties pr = new Properties();
                try {
                    pr.load(secprops);      
                }
                catch (IOException e) {
                  PortalSecurityException ep = new PortalSecurityException(e.getMessage());
                  log.error(ep);
                  throw(ep);
                } 
                finally {
                    try {
                        if (secprops != null)
                        {
                            secprops.close();
                        }
                    }
                    catch (IOException ioe) {
                        log.error( "InitialSecurityContextFactory:getInitialContext::unable to close InputStream ", ioe);
                    }
                }
                
                //Load the root context factory name
                String factoryName = pr.getProperty(ctx);
                if (factoryName == null) {
                    PortalSecurityException ep = new PortalSecurityException("No such security context " + ctx);
                    log.error(ep);
                    throw(ep);
                }
                
                //Create the root context factory
                try {
                    ISecurityContextFactory factory = (ISecurityContextFactory)Class.forName(factoryName).newInstance();
                    contextConfig.rootConfig.contextFactory = factory;
                    contextConfig.rootConfig.contextName = ctx;
                }
                catch (Exception e) {
                    PortalSecurityException ep = new PortalSecurityException("Failed to instantiate " + factoryName);
                    log.error( "Failed to instantiate " + factoryName, e);
                    throw(ep);
                }
                
                //Context configuration properties as securityContextProperty. entries
                // Format is:
                //  securityContextProperty.<PropertyName> - root context properties
                //  securityContextProperty.<SecurityContextName>.<PropertyName> - sub context properties
                //  <PropertyName> cannot contain .
                
                //Load sub context factories and context configuration properties
                Map subContextConfigs = new Hashtable();
                Map subContextProperties = new Hashtable();
                for (Enumeration ctxnames = pr.propertyNames(); ctxnames.hasMoreElements(); ) {
                    String secname, sfactoryname;
                    String candidate = (String)ctxnames.nextElement();
          
                    //For sub context properties
                    if (candidate.startsWith(ctx + ".")) {
                        secname = candidate.substring(ctx.length()+1);
                        sfactoryname = pr.getProperty(candidate);
          
                        try {
                            if (subContextConfigs.get(secname) != null) {
                                throw new IllegalArgumentException("Two sub contexts cannot share a name. (" + candidate + "=" + sfactoryname + ")");
                            }
                            
                            ISecurityContextFactory sfactory = (ISecurityContextFactory)Class.forName(sfactoryname).newInstance();
                            
                            ContextConfiguration subContextConfig = new ContextConfiguration();
                            subContextConfig.contextFactory = sfactory;
                            subContextConfig.contextName = secname;
                            
                            Properties existingProps = (Properties)subContextProperties.remove(secname);
                            if (existingProps != null) {
                                subContextConfig.contextProperties = existingProps;
                            }
                            
                            subContextConfigs.put(secname, subContextConfig);
                        }
                        catch (Exception e) {
                            String errorMsg = "(Subcontext) Failed to instantiate " + sfactoryname;
                            PortalSecurityException ep = new PortalSecurityException(errorMsg);
                            ep.setRecordedException(e);
                            log.error( errorMsg);
                            log.error( e);
                            throw ep;
                        }
                    }
                    //For context configuration properties
                    else if (candidate.startsWith(CONTEXT_PROPERTY_PREFIX)) {
                        try {
                            StringTokenizer propNameParts = new StringTokenizer(candidate, ".", true);
                            int tokenCount = propNameParts.countTokens();
                                            
                            if (tokenCount == 3 || tokenCount  == 5) {
                                String ctxName = null;
                                propNameParts.nextToken(); //Skip the prefix
                                propNameParts.nextToken(); //Skip the first . 
                                
                                if (tokenCount == 3) {
                                    ctxName = "";
                                }
                                else if (tokenCount == 5) {
                                    ctxName = propNameParts.nextToken();
                                    propNameParts.nextToken(); //Skip the second .
                                }
                
                                if (ctxName != null) {
                                    String ctxPropName = propNameParts.nextToken();
                                    String ctxPropValue = pr.getProperty(candidate);
                                    
                                    Properties subContextProps; 
                                    if (ctxName.equals("")) {
                                        //"" context name means root context
                                        subContextProps = contextConfig.rootConfig.contextProperties;
                                    }
                                    else {
                                        //See if the desired context has been created yet
                                        ContextConfiguration subContextConfig = (ContextConfiguration)subContextConfigs.get(ctxName);
                                        
                                        if (subContextConfig == null) {
                                            //Desired context doesn't exist
                                            //See if properties have been stored for this sub context yet
                                            subContextProps = (Properties)subContextProperties.get(ctxName);
                                            
                                            if (subContextProps == null) {
                                                //No properties stored for this sub context yet, create and store them
                                                subContextProps = new Properties();
                                                subContextProperties.put(ctxName, subContextProps);
                                            }
                                        }
                                        else {
                                            //Desired context exists, use the properties object from it
                                            subContextProps = subContextConfig.contextProperties;
                                        }
                                    }
                                    
                                    //Set the property on the context
                                    subContextProps.setProperty(ctxPropName, ctxPropValue);
                                }
                            }
                        }
                        catch (Exception e) {
                            log.warn("Error parsing security context property from security.properties: " + candidate, e);
                        }
                    }
                }
                
                contextConfig.subConfigs = (ContextConfiguration[])subContextConfigs.values().toArray(new ContextConfiguration[subContextConfigs.size()]);
                contextConfig.initialized = true;
            }
        }
        
        //Should have a valid contextConfig here
        try {
            //Create the root context
            ISecurityContext ictx = contextConfig.rootConfig.contextFactory.getSecurityContext();
            
            //If it is a configurable SecurityContext pass in the properties
            if (ictx instanceof IConfigurableSecurityContext) {
                ((IConfigurableSecurityContext)ictx).setProperties((Properties)contextConfig.rootConfig.contextProperties.clone());
            }            
            
            //Create all the sub contexts
            for (int index = 0; index < contextConfig.subConfigs.length; index++) {
                ISecurityContext subIctx = contextConfig.subConfigs[index].contextFactory.getSecurityContext();
                
                //If it is a configurable SecurityContext pass in the properties                
                if (subIctx instanceof IConfigurableSecurityContext) {
                    ((IConfigurableSecurityContext)subIctx).setProperties((Properties)contextConfig.subConfigs[index].contextProperties.clone());
                }
                
                ictx.addSubContext(contextConfig.subConfigs[index].contextName, subIctx);
            }
            
            return ictx;
        }
        catch (NullPointerException npe) {
            String errorMsg = "Error while creating ISecurityContext chain.";
            PortalSecurityException ep = new PortalSecurityException(errorMsg);
            ep.setRecordedException(npe);
            log.error( ep);
            throw ep;
        }
    }
}

class BaseContextConfiguration {
    final ContextConfiguration rootConfig = new ContextConfiguration();
    ContextConfiguration[] subConfigs = null;
    boolean initialized = false;
}

class ContextConfiguration {
    ISecurityContextFactory contextFactory = null;
    String contextName = null;
    Properties contextProperties = new Properties();
}