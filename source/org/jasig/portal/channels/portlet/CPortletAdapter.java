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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.PortletContainer;
import org.apache.pluto.PortletContainerException;
import org.apache.pluto.PortletContainerImpl;
import org.apache.pluto.om.portlet.PortletDefinition;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IMultithreadedCharacterChannel;
import org.jasig.portal.IMultithreadedPrivileged;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.container.om.common.impl.ObjectIDImpl;
import org.jasig.portal.container.om.entity.impl.PortletEntityImpl;
import org.jasig.portal.container.om.window.impl.PortletWindowImpl;
import org.jasig.portal.container.services.factory.impl.FactorManagerServiceImpl;
import org.jasig.portal.container.services.impl.PortletContainerEnvironmentImpl;
import org.jasig.portal.container.services.information.impl.InformationProviderServiceImpl;
import org.jasig.portal.container.services.information.impl.StaticInformationProviderImpl;
import org.jasig.portal.container.services.log.impl.LogServiceImpl;
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
    
    // Publish parameters expected by this channel
    private static final String portletDefinitionIdParamName = "portletDefinitionId";

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
    
    private void initPortletContainer(String uid) throws PortalException {
        ChannelState channelState = (ChannelState)channelStateMap.get(uid);
        PortalControlStructures pcs = channelState.getPortalControlStructures();

        try {
            PortletContainerEnvironmentImpl environment = new PortletContainerEnvironmentImpl();        
            LogServiceImpl logService = new LogServiceImpl();
            FactorManagerServiceImpl factorManagerService = new FactorManagerServiceImpl();
            InformationProviderServiceImpl informationProviderService = new InformationProviderServiceImpl();
            logService.init(servletConfig, null);
            factorManagerService.init(servletConfig, null);
            informationProviderService.init(servletConfig, null);
            environment.addContainerService(logService);
            environment.addContainerService(factorManagerService);
            environment.addContainerService(informationProviderService);
            System.out.println("Adding services: thread=" + Thread.currentThread().getName());

            portletContainer = new PortletContainerImpl();
            portletContainer.init("pluto-in-uPortal", servletConfig, environment, new Properties());
            
            portletContainerInitialized = true;
        
        } catch (Exception e) {
            String message = "Initialization of the portlet container failed.";
            LogService.log(LogService.ERROR, message, e);
            throw new PortalException(message, e);
        }
    }
        
    private void initPortletWindow(String uid) throws PortalException {
        ChannelState channelState = (ChannelState)channelStateMap.get(uid);
        ChannelStaticData sd = channelState.getStaticData();
        ChannelData cd = channelState.getChannelData();
        PortalControlStructures pcs = channelState.getPortalControlStructures();
        
        try {
            synchronized(this) {
                if (!portletContainerInitialized) {
                    initPortletContainer(uid);
                }        
            }

            // Get the portlet definition Id which must be specified as a publish
            // parameter.  The form of the Id is <portlet-context-name>.<portlet-name>
            String portletDefinitionId = sd.getParameter(portletDefinitionIdParamName);
            if (portletDefinitionId == null) {
                throw new PortalException("Missing publish parameter '" + portletDefinitionIdParamName + "'");
            }
            
            // I need to figure out why I can't use this line.
            // When I try it, I get an error saying that the prepare method of
            // PortletContainerServices was never called, even though it is called
            // in the initializtion procedure of this channel.
            //PortletDefinition portletDefinition = InformationProviderAccess.getStaticProvider().getPortletDefinition(ObjectIDImpl.createFromString(portletDefinitionId));                
            PortletDefinition portletDefinition = new StaticInformationProviderImpl().getPortletDefinition(ObjectIDImpl.createFromString(portletDefinitionId));                
                    
            PortletEntityImpl portletEntity = new PortletEntityImpl();
            portletEntity.setId(sd.getChannelPublishId());
            portletEntity.setPortletDefinition(portletDefinition);
            // need to set preferences here based on channel's static parameters
                
            PortletWindowImpl portletWindow = new PortletWindowImpl();
            portletWindow.setId(sd.getChannelSubscribeId());
            portletWindow.setPortletEntity(portletEntity);
            
            cd.setPortletWindow(portletWindow);
                
            portletContainer.portletLoad(portletWindow, pcs.getHttpServletRequest(), pcs.getHttpServletResponse());
            
            cd.setPortletWindowInitialized(true);
            
        } catch (Exception e) {
            String message = "Initialization of the portlet container failed.";
            LogService.log(LogService.ERROR, message, e);
            throw new PortalException(message, e);
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
    private String getMarkup(String uid) {
        ChannelState channelState = (ChannelState)channelStateMap.get(uid);
        ChannelStaticData sd = channelState.getStaticData();
        ChannelData cd = channelState.getChannelData();
        PortalControlStructures pcs = channelState.getPortalControlStructures();
        
        String markup = "<b>Problem rendering portlet " + sd.getParameter("portletDefinitionId") + "</b>";
        
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            //HttpServletResponse wrappedResponse = ServletObjectAccess.getStoredServletResponse(pcs.getHttpServletResponse(), pw);
            HttpServletResponse wrappedResponse = new StoredServletResponseImpl(pcs.getHttpServletResponse(), pw);
            
            portletContainer.renderPortlet(cd.getPortletWindow(), pcs.getHttpServletRequest(), wrappedResponse);
            markup = sw.toString();
            
        } catch (PortletContainerException e) {
            e.printStackTrace(System.err);
        } catch (Throwable t) {
            t.printStackTrace(System.err);
        }
        
        return markup;
    }    
}
