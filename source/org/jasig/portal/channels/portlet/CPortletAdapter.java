/**
 * Copyright © 2003 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.channels.portlet;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.PortletContainer;
import org.apache.pluto.PortletContainerServices;
import org.apache.pluto.core.InternalActionResponse;
import org.apache.pluto.factory.PortletObjectAccess;
import org.apache.pluto.om.portlet.PortletDefinition;
import org.apache.pluto.services.information.DynamicInformationProvider;
import org.apache.pluto.services.information.InformationProviderAccess;
import org.apache.pluto.services.information.PortletActionProvider;
import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.ChannelRegistryStoreFactory;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IMultithreadedCacheable;
import org.jasig.portal.IMultithreadedCharacterChannel;
import org.jasig.portal.IMultithreadedPrivileged;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.container.PortletContainerImpl;
import org.jasig.portal.container.om.common.ObjectIDImpl;
import org.jasig.portal.container.om.common.PreferenceSetImpl;
import org.jasig.portal.container.om.entity.PortletEntityImpl;
import org.jasig.portal.container.om.portlet.PortletDefinitionImpl;
import org.jasig.portal.container.om.window.PortletWindowImpl;
import org.jasig.portal.container.services.FactoryManagerServiceImpl;
import org.jasig.portal.container.services.PortletContainerEnvironmentImpl;
import org.jasig.portal.container.services.information.DynamicInformationProviderImpl;
import org.jasig.portal.container.services.information.InformationProviderServiceImpl;
import org.jasig.portal.container.services.information.PortletStateManager;
import org.jasig.portal.container.services.log.LogServiceImpl;
import org.jasig.portal.container.servlet.EmptyRequestImpl;
import org.jasig.portal.container.servlet.ServletObjectAccess;
import org.jasig.portal.container.servlet.ServletRequestImpl;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.SAXHelper;
import org.xml.sax.ContentHandler;

/**
 * A JSR-168 Portlet-to-Channel adapter that presents a portlet
 * through the uPortal channel interface. 
 * There is a related channel type called
 * "Portlet Adapter" that is included with uPortal, so to use
 * this channel, just select "Portlet Adapter" when publishing.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class CPortletAdapter implements IMultithreadedCharacterChannel, IMultithreadedPrivileged, IMultithreadedCacheable {

    protected static Map channelStateMap;
    private static boolean portletContainerInitialized;
    private static PortletContainer portletContainer;
    private static ServletConfig servletConfig;
    private static ChannelCacheKey systemCacheKey;
    private static ChannelCacheKey instanceCacheKey;
    
    private static final String uniqueContainerName = "Pluto-in-uPortal";
    
    // Publish parameters expected by this channel
    private static final String portletDefinitionIdParamName = "portletDefinitionId";
    public static final String portletPreferenceNamePrefix = "PORTLET.";

    static {
        channelStateMap = Collections.synchronizedMap(new HashMap());
        portletContainerInitialized = false;        

        // Initialize cache keys
        systemCacheKey = new ChannelCacheKey();
        systemCacheKey.setKeyScope(ChannelCacheKey.SYSTEM_KEY_SCOPE);
        systemCacheKey.setKey("SYSTEM_SCOPE_KEY");
        instanceCacheKey = new ChannelCacheKey();
        instanceCacheKey.setKeyScope(ChannelCacheKey.INSTANCE_KEY_SCOPE);
        instanceCacheKey.setKey("INSTANCE_SCOPE_KEY");
    }
    
    /**
     * Receive the servlet config from uPortal's PortalSessionManager servlet.
     * Pluto needs access to this object from serveral places.
     * @param config the servlet config
     */
    public static void setServletConfig(ServletConfig config) {
        servletConfig = config;
    }
    
    protected void initPortletContainer(String uid) throws PortalException {
        ChannelState channelState = (ChannelState)channelStateMap.get(uid);
        PortalControlStructures pcs = channelState.getPortalControlStructures();

        try {
            PortletContainerEnvironmentImpl environment = new PortletContainerEnvironmentImpl();        
            LogServiceImpl logService = new LogServiceImpl();
            FactoryManagerServiceImpl factorManagerService = new FactoryManagerServiceImpl();
            InformationProviderServiceImpl informationProviderService = new InformationProviderServiceImpl();
            logService.init(servletConfig, null);
            factorManagerService.init(servletConfig, null);
            informationProviderService.init(servletConfig, null);
            environment.addContainerService(logService);
            environment.addContainerService(factorManagerService);
            environment.addContainerService(informationProviderService);

            portletContainer = new PortletContainerImpl();
            portletContainer.init(uniqueContainerName, servletConfig, environment, new Properties());
            
            portletContainerInitialized = true;
        
        } catch (Exception e) {
            String message = "Initialization of the portlet container failed.";
            LogService.log(LogService.ERROR, message, e);
            throw new PortalException(message, e);
        }
    }
        
    protected void initPortletWindow(String uid) throws PortalException {
        ChannelState channelState = (ChannelState)channelStateMap.get(uid);
        ChannelRuntimeData rd = channelState.getRuntimeData();
        ChannelStaticData sd = channelState.getStaticData();
        ChannelData cd = channelState.getChannelData();
        PortalControlStructures pcs = channelState.getPortalControlStructures();
        
        try {
            synchronized(this) {
                if (!portletContainerInitialized) {
                    initPortletContainer(uid);
                }        
            }
            
            PortletContainerServices.prepare(uniqueContainerName);

            // Get the portlet definition Id which must be specified as a publish
            // parameter.  The syntax of the ID is [portlet-context-name].[portlet-name]
            String portletDefinitionId = sd.getParameter(portletDefinitionIdParamName);
            if (portletDefinitionId == null) {
                throw new PortalException("Missing publish parameter '" + portletDefinitionIdParamName + "'");
            }
            
            PortletDefinition portletDefinition = InformationProviderAccess.getStaticProvider().getPortletDefinition(ObjectIDImpl.createFromString(portletDefinitionId));
            if (portletDefinition == null) {
                throw new PortalException("Unable to find portlet definition for ID '" + portletDefinitionId + "'");
            }
            
            ChannelDefinition channelDefinition = ChannelRegistryStoreFactory.getChannelRegistryStoreImpl().getChannelDefinition(Integer.parseInt(sd.getChannelPublishId()));
            ((PortletDefinitionImpl)portletDefinition).setChannelDefinition(channelDefinition);      
                    
            PortletEntityImpl portletEntity = new PortletEntityImpl();
            portletEntity.setId(sd.getChannelPublishId());
            portletEntity.setPortletDefinition(portletDefinition);

            // Take all parameters whose names start with "PORTLET." and pass them
            // as portlet entity preferences (after stripping "PORTLET.")
            PreferenceSetImpl preferences = new PreferenceSetImpl();
            Enumeration allKeys = sd.keys();
            while (allKeys.hasMoreElements()) {
                String p = (String)allKeys.nextElement();
                if (p.startsWith(portletPreferenceNamePrefix)) {
                    String prefName = p.substring(portletPreferenceNamePrefix.length());
                    String prefVal = (String)sd.getParameter(p);
                    // Currently we are limited to one value per name
                    // The Preference object supports multiple values per name.
                    // We could consider a convention in which multi-valued preferences
                    // are denoted by a comma-delimited String.  This is a little messy,
                    // but we want to minimize changes to the framework in order to support
                    // the portlet-to-channel adapter.
                    Collection values = new ArrayList(1);
                    values.add(prefVal);
                    preferences.add(prefName, values);
                }
            }
            
            portletEntity.setPreferences(preferences);
             
            // Now create the PortletWindow and hold a reference to it
            PortletWindowImpl portletWindow = new PortletWindowImpl();
            portletWindow.setId(sd.getChannelSubscribeId());
            portletWindow.setPortletEntity(portletEntity);
			portletWindow.setChannelRuntimeData(rd);
            portletWindow.setHttpServletRequest(pcs.getHttpServletRequest());
            cd.setPortletWindow(portletWindow);
                
            // As the container to load the portlet
            HttpServletRequest requestWrapper = pcs.getHttpServletRequest();
            portletContainer.portletLoad(portletWindow, requestWrapper, pcs.getHttpServletResponse());
            
            cd.setPortletWindowInitialized(true);
            
        } catch (Exception e) {
            String message = "Initialization of the portlet container failed.";
            LogService.log(LogService.ERROR, message, e);
            throw new PortalException(message, e);
        } finally {
            PortletContainerServices.release();
        }
    }

    /**
     * Sets channel runtime properties.
     * @param uid a unique ID used to identify the state of the channel
     * @return channel runtime properties
     */
    public ChannelRuntimeProperties getRuntimeProperties(String uid) {
        return new ChannelRuntimeProperties();
    }

    /**
     * React to portal events.
     * Removes channel state from the channel state map when the session expires.
     * @param ev a portal event
     * @param uid a unique ID used to identify the state of the channel
     */
    public void receiveEvent(PortalEvent ev, String uid) {
        ChannelState channelState = (ChannelState)channelStateMap.get(uid);
        ChannelData cd = channelState.getChannelData();
        PortalControlStructures pcs = channelState.getPortalControlStructures();
        
        try {
            PortletContainerServices.prepare(uniqueContainerName);
            
            cd.setReceivedEvent(true);
            DynamicInformationProvider dip = InformationProviderAccess.getDynamicProvider(pcs.getHttpServletRequest());
            PortletActionProvider pap = dip.getPortletActionProvider(cd.getPortletWindow());
            
            switch (ev.getEventNumber()) {
                
                // Detect portlet mode changes   
                     
                case PortalEvent.EDIT_BUTTON_EVENT:
                    pap.changePortletMode(PortletMode.EDIT);
                    break;
                case PortalEvent.HELP_BUTTON_EVENT:
                    pap.changePortletMode(PortletMode.HELP);
                    break;
                case PortalEvent.ABOUT_BUTTON_EVENT:
                    // We might want to consider a custom ABOUT mode here
                    //pap.changePortletMode(new PortletMode("ABOUT"));
                    break;
                    
                // Detect portlet window state changes
                
                case PortalEvent.DETACH_BUTTON_EVENT:
                    // Maybe we want to consider a custom window state here or used MAXIMIZED
                    //pap.changePortletWindowState(new WindowState("DETACHED"));
                    break;
                
                // Detect end of session or portlet removed from layout
                
                case PortalEvent.SESSION_DONE:
                case PortalEvent.UNSUBSCRIBE:
                    // For both SESSION_DONE and UNSUBSCRIBE, we might want to
                    // release resources here if we need to
                    PortletStateManager.clearState(pcs.getHttpServletRequest());
                    break;
                    
                default:
                    break;
            }
                   
            if (channelState != null) {
                channelState.setPortalEvent(ev);
                if (ev.getEventNumber() == PortalEvent.SESSION_DONE) {
                    channelStateMap.remove(uid); // Clean up
                }
            }
        } finally {
            PortletContainerServices.release();     
        }
    }

    /**
     * Sets the channel static data.
     * @param sd the channel static data
     * @param uid a unique ID used to identify the state of the channel
     * @throws org.jasig.portal.PortalException
     */
    public void setStaticData(ChannelStaticData sd, String uid) throws PortalException {
        ChannelState channelState = new ChannelState();
        channelState.setStaticData(sd);
        channelStateMap.put(uid, channelState);
        
        try {
            // Register rendering dependencies to ensure that setRuntimeData is called on
            // all CPortletAdapters before any of them is asked to render
            List portletIds = (List)sd.getJNDIContext().lookup("/portlet-ids");
            Iterator iter = portletIds.iterator();
            while (iter.hasNext()) {
                String portletId = (String)iter.next();
                sd.getICCRegistry().addInstructorChannel(portletId);
                sd.getICCRegistry().addListenerChannel(portletId);
            }
            portletIds.add(sd.getChannelSubscribeId());
        } catch (Exception e) {
            throw new PortalException(e);
        }                   
    }

    /**
     * Sets the channel runtime data.
     * @param rd the channel runtime data
     * @param uid a unique ID used to identify the state of the channel
     * @throws org.jasig.portal.PortalException
     */
    public void setRuntimeData(ChannelRuntimeData rd, String uid) throws PortalException {
        ChannelState channelState = (ChannelState)channelStateMap.get(uid);
        channelState.setRuntimeData(rd);
        
        PortalControlStructures pcs = channelState.getPortalControlStructures();
        ChannelData cd = channelState.getChannelData();
        
        try {
            PortletContainerServices.prepare(uniqueContainerName);
            
            if (cd.isPortletWindowInitialized()) {
                // Put the current runtime data into the portlet window
                PortletWindowImpl portletWindow = (PortletWindowImpl)cd.getPortletWindow();
                portletWindow.setChannelRuntimeData(rd);
                portletWindow.setHttpServletRequest(pcs.getHttpServletRequest());
                
                // Get the portlet url manager which will analyze the request parameters
                DynamicInformationProvider dip = InformationProviderAccess.getDynamicProvider(pcs.getHttpServletRequest());
                PortletStateManager psm = ((DynamicInformationProviderImpl)dip).getPortletStateManager(portletWindow);
                
                // If portlet is rendering as root, change mode to maximized, otherwise minimized
                PortletActionProvider pap = dip.getPortletActionProvider(portletWindow);
                if (rd.isRenderingAsRoot()) {
                    pap.changePortletWindowState(WindowState.MAXIMIZED);
                } else {
                    pap.changePortletWindowState(WindowState.NORMAL);
                }
                
                // Process action if this is the targeted channel and the URL is an action URL
                if (rd.isTargeted() && psm.isAction()) {
                    try {
                        StringWriter sw = new StringWriter();
                        PrintWriter pw = new PrintWriter(sw);
                        HttpServletRequest request = pcs.getHttpServletRequest();
                        HttpServletResponse wrappedResponse = ServletObjectAccess.getStoredServletResponse(pcs.getHttpServletResponse(), pw);
                        //System.out.println("Processing portlet action on " + cd.getPortletWindow().getId());
                        portletContainer.processPortletAction(cd.getPortletWindow(), request, wrappedResponse);
                        InternalActionResponse actionResponse = (InternalActionResponse)PortletObjectAccess.getActionResponse(cd.getPortletWindow(), pcs.getHttpServletRequest(), pcs.getHttpServletResponse());
                        cd.setProcessedAction(true);
                    } catch (Exception e) {
                        throw new PortalException(e);
                    }
                }
            }
        } finally {
            PortletContainerServices.release();
        }
        
    }

    /**
     * Sets the portal control structures.
     * @param pcs the portal control structures
     * @param uid a unique ID used to identify the state of the channel
     * @throws org.jasig.portal.PortalException
     */
    public void setPortalControlStructures(PortalControlStructures pcs, String uid) throws PortalException {
      ChannelState channelState = (ChannelState)channelStateMap.get(uid);
      channelState.setPortalControlStructures(pcs);
    }

    /**
     * Output channel content to the portal as raw characters
     * @param pw a print writer
     * @param uid a unique ID used to identify the state of the channel
     */
    public void renderCharacters(PrintWriter pw, String uid) throws PortalException {        
        ChannelState channelState = (ChannelState)channelStateMap.get(uid);
        ChannelData cd = channelState.getChannelData();
        
        if (!cd.isPortletWindowInitialized()) {
            initPortletWindow(uid);
        }

        try {           
            String markupString = getMarkup(uid);
            pw.print(markupString);                       
        } catch (Exception e) {
            throw new PortalException(e);
        }    
    }
  
    /**
     * Output channel content to the portal.  This version of the 
     * render method is normally not used since this is a "character channel".
     * @param out a sax document handler
     * @param uid a unique ID used to identify the state of the channel
     */
    public void renderXML(ContentHandler out, String uid) throws PortalException {        
        ChannelState channelState = (ChannelState)channelStateMap.get(uid);
        ChannelData cd = channelState.getChannelData();

        if (!cd.isPortletWindowInitialized()) {
            initPortletWindow(uid);
        }
        
        try {
            String markupString = getMarkup(uid);
                                
            // Output content.  This assumes that markupString
            // is well-formed.  Consider changing to a character
            // channel when it becomes available.  Until we use the
            // character channel, these <div> tags will be necessary.
            SAXHelper.outputContent(out, "<div>" + markupString + "</div>");
                     
        } catch (Exception e) {
            throw new PortalException(e);
        }
    }

    /**
     * This is where we do the real work of getting the markup.
     * This is called from both renderXML() and renderCharacters().
     * @param uid a unique ID used to identify the state of the channel
     */
    protected synchronized String getMarkup(String uid) throws PortalException {
        ChannelState channelState = (ChannelState)channelStateMap.get(uid);
        ChannelRuntimeData rd = channelState.getRuntimeData();
        ChannelStaticData sd = channelState.getStaticData();
        ChannelData cd = channelState.getChannelData();
        PortalControlStructures pcs = channelState.getPortalControlStructures();
        
        String markup = "<b>Problem rendering portlet " + sd.getParameter("portletDefinitionId") + "</b>";
        
        try {
            PortletContainerServices.prepare(uniqueContainerName);

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            HttpServletRequest wrappedRequest = new ServletRequestImpl(pcs.getHttpServletRequest());
            HttpServletResponse wrappedResponse = ServletObjectAccess.getStoredServletResponse(pcs.getHttpServletResponse(), pw);
            
            // Check if this portlet has just processed an action during this request.
            // If so, then we capture the changes that the portlet may have made during
            // its processAction implementation (captured in the portlet's ActionResponse)
            // and we pass them to the render request.
            // Pluto's portlet container implementation does this by creating a new render URL 
            // and redirecting, but we have overidden that behavior in our own version of PortletContainerImpl.
            if (cd.hasProcessedAction()) {
                InternalActionResponse actionResponse = ((PortletWindowImpl)cd.getPortletWindow()).getInternalActionResponse();
                PortletActionProvider pap = InformationProviderAccess.getDynamicProvider(pcs.getHttpServletRequest()).getPortletActionProvider(cd.getPortletWindow());
                // Change modes
                if (actionResponse.getChangedPortletMode() != null) {
                    pap.changePortletMode(actionResponse.getChangedPortletMode());
                }
                // Change window states
                if (actionResponse.getChangedWindowState() != null) {
                    pap.changePortletWindowState(actionResponse.getChangedWindowState());
                }
                // Change render parameters
                Map renderParameters = actionResponse.getRenderParameters();
                ((ServletRequestImpl)wrappedRequest).setParameters(renderParameters);
            }
                                    
            // Hide the request parameters if this portlet isn't targeted
            if (!rd.isTargeted()) {
                wrappedRequest = new EmptyRequestImpl(wrappedRequest);
            }
            
            //System.out.println("Rendering portlet " + cd.getPortletWindow().getId());
            portletContainer.renderPortlet(cd.getPortletWindow(), wrappedRequest, wrappedResponse);
            
            markup = sw.toString();
            
            cd.setProcessedAction(false);
            ((PortletWindowImpl)cd.getPortletWindow()).setInternalActionResponse(null);
                        
        } catch (Throwable t) {
            t.printStackTrace();
            LogService.log(LogService.ERROR, t);
            throw new PortalException(t.getMessage());
        } finally {
            PortletContainerServices.release();
        }
        
        return markup;
    }
    
    // IMultithreadedCacheable methods
    
    /**
     * Generates a channel cache key.  The key scope is set to be system-wide
     * when the channel is anonymously accessed, otherwise it is set to be
     * instance-wide.  The caching implementation here is simple and may not
     * handle all cases.  It may also violate the Portlet Specification so
     * this obviously needs further discussion.
     * @param uid the unique identifier
     * @return the channel cache key
     */
    public ChannelCacheKey generateKey(String uid) {
        ChannelState channelState = (ChannelState)channelStateMap.get(uid);
        ChannelStaticData staticData = channelState.getStaticData();

        ChannelCacheKey cck = null;
        // Anonymously accessed pages can be cached system-wide
        if(staticData.getPerson().isGuest()) {
            cck = systemCacheKey;
        } else {
            cck = instanceCacheKey;
        }
        return cck;
    }

    /**
     * Determines whether the cached content for this channel is still valid.
     * <p>
     * Return <code>true</code> when:<br>
     * <ol>
     * <li>We have not just received an event</li>
     * <li>No runtime parameters are sent to the channel</li>
     * <li>The focus hasn't switched.</li>
     * </ol>
     * Otherwise, return <code>false</code>.  
     * <p>
     * In other words, cache the content in all cases <b>except</b> 
     * for when a user clicks a channel button, a link or form button within the channel, 
     * or the <i>focus</i> or <i>unfocus</i> button.
     * @param validity the validity object
     * @param uid the unique identifier
     * @return <code>true</code> if the cache is still valid, otherwise <code>false</code>
     */
    public boolean isCacheValid(Object validity, String uid) {
        ChannelState channelState = (ChannelState)channelStateMap.get(uid);
        ChannelStaticData staticData = channelState.getStaticData();
        ChannelRuntimeData runtimeData = channelState.getRuntimeData();
        PortalControlStructures pcs = channelState.getPortalControlStructures();
        ChannelData cd = channelState.getChannelData();

        // Determine if the channel focus has changed
        boolean previouslyFocused = cd.isFocused();
        cd.setFocused(runtimeData.isRenderingAsRoot());
        boolean focusHasSwitched = cd.isFocused() != previouslyFocused;
    
        // Dirty cache only when we receive an event, one or more request params, or a change in focus
        boolean cacheValid = !(cd.hasReceivedEvent() || runtimeData.isTargeted() || focusHasSwitched);
    
        cd.setReceivedEvent(false);
        return cacheValid;
    }

}
