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

package org.jasig.portal.channels.wsrp;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IMultithreadedCharacterChannel;
import org.jasig.portal.IMultithreadedPrivileged;
import org.jasig.portal.MediaManager;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.SAXHelper;
import org.jasig.portal.wsrp.MarkupService;
import org.jasig.portal.wsrp.types.ClientData;
import org.jasig.portal.wsrp.types.InteractionParams;
import org.jasig.portal.wsrp.types.MarkupContext;
import org.jasig.portal.wsrp.types.MarkupParams;
import org.jasig.portal.wsrp.types.MarkupResponse;
import org.jasig.portal.wsrp.types.NamedString;
import org.jasig.portal.wsrp.types.PersonName;
import org.jasig.portal.wsrp.types.PortletContext;
import org.jasig.portal.wsrp.types.RegistrationContext;
import org.jasig.portal.wsrp.types.RuntimeContext;
import org.jasig.portal.wsrp.types.SessionContext;
import org.jasig.portal.wsrp.types.StateChange;
import org.jasig.portal.wsrp.types.Templates;
import org.jasig.portal.wsrp.types.UserContext;
import org.jasig.portal.wsrp.types.UserProfile;
import org.xml.sax.ContentHandler;

/**
 * A uPortal WSRP consumer channel.  
 * There is a related channel type called
 * "WSRP Consumer" that is included with uPortal, so to use
 * this channel, just select "WSRP Consumer" when publishing.
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class CConsumer implements IMultithreadedCharacterChannel, IMultithreadedPrivileged {

    protected static Map channelStateMap;
    protected static RegistrationContext registrationContext;
    protected static Map serviceDescriptionMap;

    /**
     * The channel's state including the static data, runtime data,
     * portal event, portal control structures and a ChannelData object. 
     */
    protected class ChannelState {
        private ChannelStaticData staticData = null;
        private ChannelRuntimeData runtimeData = null;
        private PortalEvent portalEvent = null;
        private PortalControlStructures pcs = null;
        private ChannelData channelData = new ChannelData();

        public ChannelStaticData getStaticData() { return this.staticData; }
        public ChannelRuntimeData getRuntimeData() { return this.runtimeData; }
        public PortalControlStructures getPortalControlStructures() { return this.pcs; }
        public PortalEvent getPortalEvent() { return this.portalEvent; }
        public ChannelData getChannelData() { return this.channelData; }

        public void setStaticData(ChannelStaticData sd) { this.staticData = sd; }
        public void setRuntimeData(ChannelRuntimeData rd) { this.runtimeData = rd; }
        public void setPortalControlStructures(PortalControlStructures pcs) { this.pcs = pcs; }
        public void setPortalEvent(PortalEvent ev) { this.portalEvent = ev; }
        public void setChannelData(ChannelData cd) { this.channelData = cd; }
    }

    /**
     * An object that keeps track of session data.
     */
    protected class ChannelData {
        private String sessionId = null;
        private MarkupService markupService = null;
        private PortletContext portletContext = null;
        
        public String getSessionId() { return this.sessionId; }
        public MarkupService getMarkupService() { return this.markupService; }
        public PortletContext getPortletContext() { return this.portletContext; }
        
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public void setMarkupService(MarkupService markupService) { this.markupService = markupService; }
        public void setPortletContext(PortletContext portletContext) { this.portletContext = portletContext; }
    }


    static {
        channelStateMap = Collections.synchronizedMap(new HashMap());
        
        // Setup registration context
        // The registration context would be normally be produced when the consumer
        // registers with the producer - right now we are skipping this optional step
        registrationContext = new RegistrationContext();            
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
                cd.getMarkupService().releaseSessions(registrationContext, new String[] { cd.getSessionId() });
            }
        } catch (Exception e) {
            e.printStackTrace(); // TODO: Take out printStackTrace for release
            LogService.log(LogService.ERROR, "Unable to release session '" + cd.getSessionId() + "' in remote portal");      
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
        
        ChannelData cd = channelState.getChannelData();
        
        // Get static parameters
        String baseEndpoint = sd.getParameter("baseEndpoint");
        String portletHandle = sd.getParameter("portletHandle");
        if (baseEndpoint == null)
            throw new PortalException("Missing publish parameter 'baseEndpoint'");
        if (portletHandle == null)
            throw new PortalException("Missing publish parameter 'portletHandle'");
                    
        // Get markup service
        MarkupService markupService = null;
        try {
            markupService = MarkupService.getService(baseEndpoint);
            cd.setMarkupService(markupService);
        } catch (Exception e) {
            throw new PortalException(e);
        }
        
        // Portlet context
        PortletContext portletContext = new PortletContext();
        portletContext.setPortletHandle(portletHandle);
        cd.setPortletContext(portletContext);
    }

    /**
     * Sets the channel runtime data.
     * @param rd the channel runtime data
     * @param uid a unique ID used to identify the state of the channel
     * @throws org.jasig.portal.PortalException
     */
    public void setRuntimeData(ChannelRuntimeData rd, String uid) throws PortalException {
        ChannelState channelState = (ChannelState)channelStateMap.get(uid);
        ChannelStaticData staticData = channelState.getStaticData();
        channelState.setRuntimeData(rd);
        
        ChannelData cd = channelState.getChannelData();
        MarkupService markupService = cd.getMarkupService();
        PortletContext portletContext = cd.getPortletContext();
        
        // If request contains request parameters, then the
        // user has just interacted with the channel so
        // call performBlockingInteraction on markup service
        if (rd.getParameters().size() > 0) {
            try {
                // Runtime context
                RuntimeContext runtimeContext = new RuntimeContext();
                runtimeContext.setUserAuthentication("wsrp:none");
                runtimeContext.setSessionID(cd.getSessionId());
                Templates templates = new Templates();
                templates.setRenderTemplate(rd.getBaseActionURL());
                runtimeContext.setTemplates(templates);        
                
                // User context
                UserContext userContext = new UserContext();
                IPerson person = staticData.getPerson();
                UserProfile userProfile = new UserProfile();
                PersonName personName = new PersonName();
                personName.setGiven((String)person.getAttribute("givenName"));
                personName.setFamily((String)person.getAttribute("sn"));
                userProfile.setName(personName); // much more could be set here!
                userContext.setUserContextKey((String)person.getAttribute(IPerson.USERNAME));
                userContext.setProfile(userProfile);
                
                // Markup params
                MarkupParams markupParams = new MarkupParams();
                ClientData clientData = new ClientData();
                clientData.setUserAgent(rd.getBrowserInfo().getUserAgent());
                markupParams.setClientData(clientData);
                markupParams.setSecureClientCommunication(false); // is consumer currently communicating securly with end user?
                markupParams.setLocales(getLocalesAsStringArray(rd.getLocales()));
                MediaManager mediaManager = new MediaManager();
                markupParams.setMimeTypes(new String[] { mediaManager.getReturnMimeType(mediaManager.getMedia(rd.getBrowserInfo())) });
                markupParams.setMode("wsrp:view"); // can be a different mode
                markupParams.setWindowState("wsrp:normal");
                markupParams.setNavigationalState(""); // ???
                markupParams.setMarkupCharacterSets(new String[] {"UTF-8"});
                markupParams.setValidateTag(""); // ???
                markupParams.setValidNewModes(new String[] {""}); // ??
                markupParams.setValidNewWindowStates(new String[] {""}); // ??
                
                // Interaction params
                InteractionParams interactionParams = new InteractionParams();
                interactionParams.setPortletStateChange(StateChange.readWrite);
                //interactionParams.setInteractionState(""); ??
                interactionParams.setFormParameters(getFormParameters(rd));
                
                markupService.performBlockingInteraction(registrationContext, portletContext, runtimeContext, userContext, markupParams, interactionParams);
                
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
        ChannelStaticData staticData = channelState.getStaticData();
        ChannelRuntimeData runtimeData = channelState.getRuntimeData();
        PortalControlStructures pcs = channelState.getPortalControlStructures();
        ChannelData cd = channelState.getChannelData();
        
        String markupString = null;
        try {
            // Portlet context
            PortletContext portletContext = cd.getPortletContext();            
                      
            // Runtime context
            RuntimeContext runtimeContext = new RuntimeContext();
            runtimeContext.setUserAuthentication("wsrp:none");
            runtimeContext.setSessionID(cd.getSessionId());
            Templates templates = new Templates();
            templates.setRenderTemplate(runtimeData.getBaseActionURL());
            runtimeContext.setTemplates(templates);           
          
            // User context
            UserContext userContext = new UserContext();
            IPerson person = staticData.getPerson();
            UserProfile userProfile = new UserProfile();
            PersonName personName = new PersonName();
            personName.setGiven((String)person.getAttribute("givenName"));
            personName.setFamily((String)person.getAttribute("sn"));
            userProfile.setName(personName); // much more could be set here!
            userContext.setUserContextKey((String)person.getAttribute(IPerson.USERNAME));
            userContext.setProfile(userProfile);
          
            // Markup params
            MarkupParams markupParams = new MarkupParams();
            ClientData clientData = new ClientData();
            clientData.setUserAgent(runtimeData.getBrowserInfo().getUserAgent());
            markupParams.setClientData(clientData);
            markupParams.setSecureClientCommunication(false); // is consumer currently communicating securly with end user?
            markupParams.setLocales(getLocalesAsStringArray(runtimeData.getLocales()));
            MediaManager mediaManager = new MediaManager();
            markupParams.setMimeTypes(new String[] { mediaManager.getReturnMimeType(mediaManager.getMedia(runtimeData.getBrowserInfo())) });
            markupParams.setMode("wsrp:view"); // can be a different mode
            markupParams.setWindowState("wsrp:normal");
            markupParams.setNavigationalState(""); // ???
            markupParams.setMarkupCharacterSets(new String[] {"UTF-8"});
            markupParams.setValidateTag(""); // ???
            markupParams.setValidNewModes(new String[] {""}); // ??
            markupParams.setValidNewWindowStates(new String[] {""}); // ??
            
            
            MarkupResponse markupResponse = cd.getMarkupService().getMarkup(registrationContext, portletContext, runtimeContext, userContext, markupParams);                          
            MarkupContext markupContext = markupResponse.getMarkupContext();
            SessionContext sessionContext = markupResponse.getSessionContext();
            
            // Obtain the markup string from the producer
            markupString = markupContext.getMarkupString();
            
            // Check if URLs need to be rewritten - we don't support that yet
            if (markupContext.getRequiresUrlRewriting().booleanValue()) {
                throw new PortalException("Consumer URL rewriting is currently not supported");
            }
            
            // Set the session id
            String sessionId = sessionContext.getSessionID();
            cd.setSessionId(sessionId);
                                      
        } catch (Exception e) {
            e.printStackTrace();
        } 
        
        return markupString;   
    }
    
    private static String[] getLocalesAsStringArray(Locale[] locales) {
        if (locales == null) {
            locales = new Locale[] { Locale.getDefault() };
        }
        String[] localesStringArray = new String[locales.length];
        for (int i = 0; i < locales.length; i++) {
            localesStringArray[i] = locales[i].toString();
        }
        return localesStringArray;
    }
    
    private static NamedString[] getFormParameters(ChannelRuntimeData runtimeData) {
        NamedString[] namedStrings = new NamedString[runtimeData.size()];
        int i = 0;
        Enumeration enum = runtimeData.keys();
        while (enum.hasMoreElements()) {
            String key = (String)enum.nextElement();
            String value = runtimeData.getParameter(key);
            NamedString namedString = new NamedString();
            namedString.setName(key);
            namedString.setValue(value);
            namedStrings[i++] = namedString;            
        }
        return namedStrings;
    }

}
