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

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.PortletContainer;
import org.apache.pluto.PortletContainerServices;
import org.apache.pluto.om.portlet.PortletDefinition;
import org.apache.pluto.services.information.InformationProviderAccess;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IMultithreadedCharacterChannel;
import org.jasig.portal.IMultithreadedPrivileged;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.container.PortletContainerImpl;
import org.jasig.portal.container.om.common.ObjectIDImpl;
import org.jasig.portal.container.om.common.PreferenceSetImpl;
import org.jasig.portal.container.om.entity.PortletEntityImpl;
import org.jasig.portal.container.om.window.PortletWindowImpl;
import org.jasig.portal.container.services.FactoryManagerServiceImpl;
import org.jasig.portal.container.services.PortletContainerEnvironmentImpl;
import org.jasig.portal.container.services.information.InformationProviderServiceImpl;
import org.jasig.portal.container.services.log.LogServiceImpl;
import org.jasig.portal.container.servlet.EmptyRequestImpl;
import org.jasig.portal.container.servlet.ServletRequestImpl;
import org.jasig.portal.container.servlet.StoredServletResponseImpl;
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
public class CPortletAdapter implements IMultithreadedCharacterChannel, IMultithreadedPrivileged {

    protected static Map channelStateMap;
    private static boolean portletContainerInitialized;
    private static PortletContainer portletContainer;
    private static ServletConfig servletConfig;
    
    private static final String uniqueContainerName = "pluto-in-uPortal";
    
    // Publish parameters expected by this channel
    private static final String portletDefinitionIdParamName = "portletDefinitionId";
    private static final String portletPreferenceNamePrefix = "PORTLET.";

    static {
        channelStateMap = Collections.synchronizedMap(new HashMap());
        portletContainerInitialized = false;        
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

            portletContainer = new PortletContainerImpl(uniqueContainerName);
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
                    preferences.add(prefName, values, true);
                }
            }
            
            portletEntity.setPreferences(preferences);
             
            // Now create the PortletWindow and hold a reference to it
            PortletWindowImpl portletWindow = new PortletWindowImpl();
            portletWindow.setId(sd.getChannelSubscribeId());
            portletWindow.setPortletEntity(portletEntity);
            portletWindow.setChannelRuntimeData(rd);
            cd.setPortletWindow(portletWindow);
                
            // As the container to load the portlet
            HttpServletRequest requestWrapper = new ServletRequestImpl(pcs.getHttpServletRequest(), rd);
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
        
        // For session done and unsubscribe events, 
        // release session resources in remote portal
        try {
            if (ev.getEventNumber() == PortalEvent.SESSION_DONE ||
                ev.getEventNumber() == PortalEvent.UNSUBSCRIBE) {
            }
        } catch (Exception e) {
            e.printStackTrace(); // TODO: Take out printStackTrace for release
            LogService.log(LogService.ERROR, e);      
        }
               
        if (channelState != null) {
            channelState.setPortalEvent(ev);
            if (ev.getEventNumber() == PortalEvent.SESSION_DONE) {
                channelStateMap.remove(uid); // Clean up
            }
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
        // Process action if this is the targeted channel
        if (rd.isTargeted() && rd.getParameters().size() > 0) {
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                HttpServletRequest wrappedRequest = new ServletRequestImpl(pcs.getHttpServletRequest(), rd);
                HttpServletResponse wrappedResponse = new StoredServletResponseImpl(pcs.getHttpServletResponse(), pw);
                portletContainer.processPortletAction(cd.getPortletWindow(), wrappedRequest, wrappedResponse);
            } catch (Exception e) {
                throw new PortalException(e);
            }
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
    protected synchronized String getMarkup(String uid) {
        ChannelState channelState = (ChannelState)channelStateMap.get(uid);
        ChannelRuntimeData rd = channelState.getRuntimeData();
        ChannelStaticData sd = channelState.getStaticData();
        ChannelData cd = channelState.getChannelData();
        PortalControlStructures pcs = channelState.getPortalControlStructures();
        
        String markup = "<b>Problem rendering portlet " + sd.getParameter("portletDefinitionId") + "</b>";
        
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            HttpServletRequest wrappedRequest = new ServletRequestImpl(pcs.getHttpServletRequest(), rd);
            //HttpServletResponse wrappedResponse = ServletObjectAccess.getStoredServletResponse(pcs.getHttpServletResponse(), pw);
            HttpServletResponse wrappedResponse = new StoredServletResponseImpl(pcs.getHttpServletResponse(), pw);
                        
            // Clear the request parameters if this portlet isn't targeted
            if (!rd.isTargeted()) {
                wrappedRequest = new EmptyRequestImpl(wrappedRequest);
            }
            
            portletContainer.renderPortlet(cd.getPortletWindow(), wrappedRequest, wrappedResponse);
            
            markup = sw.toString();
                        
        } catch (Throwable t) {
            t.printStackTrace();
        }
        
        return markup;
    }    
}
