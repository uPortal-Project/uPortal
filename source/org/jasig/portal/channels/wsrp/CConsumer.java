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
import java.util.WeakHashMap;

import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IMultithreadedCacheable;
import org.jasig.portal.IMultithreadedCharacterChannel;
import org.jasig.portal.IMultithreadedPrivileged;
import org.jasig.portal.MediaManager;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.SAXHelper;
import org.jasig.portal.wsrp.Constants;
import org.jasig.portal.wsrp.MarkupService;
import org.jasig.portal.wsrp.ServiceDescriptionService;
import org.jasig.portal.wsrp.types.CacheControl;
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
import org.jasig.portal.wsrp.types.ServiceDescription;
import org.jasig.portal.wsrp.types.SessionContext;
import org.jasig.portal.wsrp.types.StateChange;
import org.jasig.portal.wsrp.types.Templates;
import org.jasig.portal.wsrp.types.UploadContext;
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
public class CConsumer implements IMultithreadedCharacterChannel, IMultithreadedPrivileged, IMultithreadedCacheable {

    protected static Map channelStateMap;
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
        public PortalEvent getPortalEvent() { return this.portalEvent; }
        public PortalControlStructures getPortalControlStructures() { return this.pcs; }
        public ChannelData getChannelData() { return this.channelData; }

        public void setStaticData(ChannelStaticData sd) { this.staticData = sd; }
        public void setRuntimeData(ChannelRuntimeData rd) { this.runtimeData = rd; }
        public void setPortalEvent(PortalEvent ev) { this.portalEvent = ev; }
        public void setPortalControlStructures(PortalControlStructures pcs) { this.pcs = pcs; }
        public void setChannelData(ChannelData cd) { this.channelData = cd; }
    }

    /**
     * An object that keeps track of session data.
     */
    protected class ChannelData {
        private String sessionId = null;
        private boolean receivedEvent = false;
        private boolean focused = false;
        private String mode = null;
        private ServiceDescriptionService serviceDescriptionService = null;
        private MarkupService markupService = null;
        private RegistrationContext registrationContext = null;
        private PortletContext portletContext = null;
        private MarkupParams markupParams = null;
        private InteractionParams interactionParams = null;
        private MarkupContext markupContext = null;
        private MarkupCache markupCache = null;
        
        public ChannelData() {
            this.mode = Constants.WSRP_VIEW;
            this.markupCache = new MarkupCache();
        }
        
        // Getters
        public String getSessionId() { return this.sessionId; }
        public boolean getReceivedEvent() { return this.receivedEvent; }
        public boolean getFocused() { return this.focused; }
        public String getMode() { return this.mode; }
        public ServiceDescriptionService getServiceDescriptionService() { return this.serviceDescriptionService; }
        public MarkupService getMarkupService() { return this.markupService; }
        public RegistrationContext getRegistrationContext() { return this.registrationContext; }
        public PortletContext getPortletContext() { return this.portletContext; }
        public MarkupParams getMarkupParams() { return this.markupParams; }
        public InteractionParams getInteractionParams() { return this.interactionParams; }
        public MarkupContext getMarkupContext() { return this.markupContext; }
        public MarkupCache getMarkupCache() { return this.markupCache; }
        
        public String getValidateTag() {
            String validateTag = null;
            if (markupContext != null && markupContext.getCacheControl() != null) {
                validateTag = markupContext.getCacheControl().getValidateTag();
            }
            return validateTag;
        }
        
        // Setters
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public void setReceivedEvent(boolean receivedEvent) { this.receivedEvent = receivedEvent; }
        public void setFocused(boolean focused) { this.focused = focused; }
        public void setMode(String mode) { this.mode = mode; }
        public void setServiceDescriptionService(ServiceDescriptionService serviceDescriptionService) { this.serviceDescriptionService = serviceDescriptionService; }
        public void setMarkupService(MarkupService markupService) { this.markupService = markupService; }
        public void setRegistrationContext(RegistrationContext registrationContext) { this.registrationContext = registrationContext; }
        public void setPortletContext(PortletContext portletContext) { this.portletContext = portletContext; }
        public void setMarkupParams(MarkupParams markupParams) { this.markupParams = markupParams; }
        public void setInteractionParams(InteractionParams interactionParams) { this.interactionParams = interactionParams; }
        public void setMarkupContext(MarkupContext markupContext) { this.markupContext = markupContext; }
        public void setMarkupCache(MarkupCache markupCache) { this.markupCache = markupCache; }
    }

    static {
        channelStateMap = Collections.synchronizedMap(new HashMap());
        serviceDescriptionMap = new WeakHashMap();
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
        
        cd.setReceivedEvent(true);
        
        // Help or Edit button clicked
        if (ev.getEventNumber() == PortalEvent.HELP_BUTTON_EVENT) {
            cd.setMode(Constants.WSRP_EDIT);
        } else if (ev.getEventNumber() == PortalEvent.EDIT_BUTTON_EVENT) {
            cd.setMode(Constants.WSRP_HELP);
        }
        
        switch (ev.getEventNumber()) {
            case PortalEvent.HELP_BUTTON_EVENT:
                cd.setMode(Constants.WSRP_HELP);
                break;
            case PortalEvent.EDIT_BUTTON_EVENT:
                cd.setMode(Constants.WSRP_EDIT);
                break;
            case PortalEvent.ABOUT_BUTTON_EVENT:
                cd.setMode(Constants.UP_ABOUT);
                break;
            default:
                break;
        }
                
        // For session done and unsubscribe events, 
        // release session resources in remote portal
        try {
            if (ev.getEventNumber() == PortalEvent.SESSION_DONE ||
                ev.getEventNumber() == PortalEvent.UNSUBSCRIBE) {
                cd.getMarkupService().releaseSessions(cd.getRegistrationContext(), new String[] { cd.getSessionId() });
            }
        } catch (Exception e) {
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

        // Get service description service
        ServiceDescriptionService serviceDescriptionService = null;
        try {
            serviceDescriptionService = ServiceDescriptionService.getService(baseEndpoint);
            cd.setServiceDescriptionService(serviceDescriptionService);
            
            ServiceDescription serviceDescription = (ServiceDescription)serviceDescriptionMap.get(baseEndpoint);
            if (serviceDescription == null) {
                serviceDescription = serviceDescriptionService.getServiceDescription(null, null);
                serviceDescriptionMap.put(baseEndpoint, serviceDescription);
            }
            boolean requiresRegistration = serviceDescription.isRequiresRegistration();
            if (requiresRegistration) {
                //RegistrationContext registrationContext = new RegistrationContext();
                //cd.setRegistrationContext(registrationContext);
                throw new PortalException("Remote portlet '" + portletHandle + "' requires registration which is not yet supported.");
            }
        } catch (Exception e) {
            throw new PortalException(e);
        }
                    
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
        RegistrationContext registrationContext = cd.getRegistrationContext();
        MarkupService markupService = cd.getMarkupService();
        PortletContext portletContext = cd.getPortletContext();
        
        // If request contains request parameters, then the
        // user has just interacted with the channel so
        // call performBlockingInteraction on markup service
        if (rd.getParameters().size() > 0) {
            try {
                RuntimeContext runtimeContext = getRuntimeContext(uid);                       
                UserContext userContext = getUserContext(uid);          
                MarkupParams markupParams = getMarkupParams(uid);
                
                // Interaction params
                InteractionParams interactionParams = new InteractionParams();
                interactionParams.setPortletStateChange(StateChange.readWrite);
                //interactionParams.setInteractionState(""); ??
                interactionParams.setFormParameters(getFormParameters(rd));
                cd.setInteractionParams(interactionParams);
                
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
    
    // IMultithreadedCacheable methods

    /**
     * Generates a channel cache key.  The key scope is set to be system-wide
     * when the channel is anonymously accessed, otherwise it is set to be
     * instance-wide.  The caching implementation here is simple and may not
     * handle all cases.
     * @param uid the unique identifier
     * @return the channel cache key
     */
    public ChannelCacheKey generateKey(String uid) {
        ChannelState channelState = (ChannelState)channelStateMap.get(uid);
        ChannelStaticData staticData = channelState.getStaticData();
        ChannelRuntimeData runtimeData = channelState.getRuntimeData();
        PortalControlStructures pcs = channelState.getPortalControlStructures();
        ChannelData cd = channelState.getChannelData();

        ChannelCacheKey cck = new ChannelCacheKey();

        // Anonymously accessed pages can be cached system-wide
        if(staticData.getPerson().isGuest()) {
            cck.setKeyScope(ChannelCacheKey.SYSTEM_KEY_SCOPE);
            cck.setKey("SYSTEM_SCOPE_KEY");
        } else {
            cck.setKeyScope(ChannelCacheKey.INSTANCE_KEY_SCOPE);
            cck.setKey("INSTANCE_SCOPE_KEY");
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
        boolean previouslyFocused = cd.getFocused();
        cd.setFocused(runtimeData.isRenderingAsRoot());
        boolean focusHasSwitched = cd.getFocused() != previouslyFocused;
    
        // Dirty cache only when we receive an event, one or more request params, or a change in focus
        boolean cacheValid = !cd.getReceivedEvent() && runtimeData.size() == 0 && !focusHasSwitched;
    
        cd.setReceivedEvent(false);
        return cacheValid;
    }    

    /**
     * This is where we do the real work of getting the markup.
     * This is called from both renderXML() and renderCharacters().
     * We first check the markup cache to see if any markup has been
     * cached previously.
     * @param uid a unique ID used to identify the state of the channel
     * @return the portlet markup
     * @throws PortalException
     */
    private String getMarkup(String uid) throws Exception {
        ChannelState channelState = (ChannelState)channelStateMap.get(uid);
        ChannelStaticData staticData = channelState.getStaticData();
        ChannelRuntimeData runtimeData = channelState.getRuntimeData();
        PortalControlStructures pcs = channelState.getPortalControlStructures();
        ChannelData cd = channelState.getChannelData();

        String markup = null;
        MarkupContext markupContext = cd.getMarkupContext();
        String key = generateKey(cd.getMarkupParams(), cd.getInteractionParams()); 

        if (markupContext != null) { // Have we rendered previously?
            CacheControl cacheControl = markupContext.getCacheControl();
            if (cacheControl != null) { // Was a CacheControl previously supplied?
                String userScope = cacheControl.getUserScope();
                MarkupWrapper markupWrapper = (MarkupWrapper)cd.getMarkupCache().get(key, userScope);
                if (markupWrapper != null) { // Have we previously cached some markup?                   
                    if (!markupWrapper.hasExpired()) { // Is the markup still valid?
                        markup = markupWrapper.getMarkup();
                    } else {                         
                        // Note sure how to interpret the WSRP spec here.
                        // Should we remove expired markup from the cache
                        // or hang onto it in case the producer says to use it later?
                        // Remove the expired markup from the cache
                        cd.getMarkupCache().remove(key, userScope);
                        if (markupContext.getUseCachedMarkup().booleanValue()) { // Does the producer say to use the expired markup?
                            markup = markupWrapper.getMarkup();
                        }
                    }
                    
                }
            }
        }
                
        if (markup == null) {
            // Go get the markup from the producer
            MarkupResponse markupResponse = getMarkupFromProducer(uid);
            
            markupContext = markupResponse.getMarkupContext();
            cd.setMarkupContext(markupContext);
            
            // Check if URLs need to be rewritten - we don't support that yet
            if (markupContext.getRequiresUrlRewriting().booleanValue()) {
                throw new PortalException("Consumer URL rewriting is currently not supported");
            }
            
            // Obtain the actual markup as a String
            markup = markupContext.getMarkupString();

            // Set the session id
            SessionContext sessionContext = markupResponse.getSessionContext();
            String sessionId = sessionContext.getSessionID();
            cd.setSessionId(sessionId);
                        
            // If a CacheControl has been supplied, cache the markup
            CacheControl cacheControl = markupContext.getCacheControl();
            if (cacheControl != null) {
                MarkupWrapper markupWrapper = new MarkupWrapper(markup, cacheControl);
                cd.getMarkupCache().put(key, markupWrapper);
            }
        }
        
        return markup;
    }
            
    /**
     * This is where we do the real work of getting the markup from the producer.
     * @param uid a unique ID used to identify the state of the channel
     * @return the markup response
     */
    private MarkupResponse getMarkupFromProducer(String uid) throws Exception {
        ChannelState channelState = (ChannelState)channelStateMap.get(uid);
        ChannelStaticData staticData = channelState.getStaticData();
        ChannelRuntimeData runtimeData = channelState.getRuntimeData();
        PortalControlStructures pcs = channelState.getPortalControlStructures();
        ChannelData cd = channelState.getChannelData();
        
        RegistrationContext registrationContext = cd.getRegistrationContext();            
        PortletContext portletContext = cd.getPortletContext();            
        RuntimeContext runtimeContext = getRuntimeContext(uid);                  
        UserContext userContext = getUserContext(uid);          
        MarkupParams markupParams = getMarkupParams(uid);
                    
        MarkupService markupService = cd.getMarkupService();
        MarkupResponse markupResponse = markupService.getMarkup(registrationContext, 
                                                 portletContext, 
                                                 runtimeContext, 
                                                 userContext, 
                                                 markupParams);                                                               
        
        return markupResponse;   
    }
    
    /**
     * Helps construct and populate a RuntimeContext object.
     * @param uid the unique identifier
     * @return a populated RuntimeContext
     */
    private RuntimeContext getRuntimeContext(String uid) {
        ChannelState channelState = (ChannelState)channelStateMap.get(uid);
        ChannelRuntimeData runtimeData = channelState.getRuntimeData();
        ChannelData cd = channelState.getChannelData();

        RuntimeContext runtimeContext = new RuntimeContext();
        runtimeContext.setUserAuthentication(Constants.WSRP_NONE);
        runtimeContext.setSessionID(cd.getSessionId());
        Templates templates = new Templates();
        templates.setRenderTemplate(runtimeData.getBaseActionURL());
        runtimeContext.setTemplates(templates);           
        return runtimeContext;
    }
    
    /**
     * Helps construct and populate a UserContext object.
     * @param uid the unique identifier
     * @return a populated UserContext
     */
    private UserContext getUserContext(String uid) {
        ChannelState channelState = (ChannelState)channelStateMap.get(uid);
        ChannelStaticData staticData = channelState.getStaticData();

        UserContext userContext = new UserContext();
        IPerson person = staticData.getPerson();
        UserProfile userProfile = new UserProfile();
        PersonName personName = new PersonName();
        personName.setGiven((String)person.getAttribute("givenName"));
        personName.setFamily((String)person.getAttribute("sn"));
        userProfile.setName(personName); // much more could be set here!
        userContext.setUserContextKey((String)person.getAttribute(IPerson.USERNAME));
        userContext.setProfile(userProfile);
        return userContext;
    }
    
    /**
     * Helps construct and populate a MarkupParams object.
     * @param uid the unique identifier
     * @return a populated MarkupParams
     */
    private MarkupParams getMarkupParams(String uid) {
        ChannelState channelState = (ChannelState)channelStateMap.get(uid);
        ChannelRuntimeData runtimeData = channelState.getRuntimeData();
        ChannelData cd = channelState.getChannelData();

        MarkupParams markupParams = new MarkupParams();
        ClientData clientData = new ClientData();
        clientData.setUserAgent(runtimeData.getBrowserInfo().getUserAgent());
        markupParams.setClientData(clientData);
        markupParams.setSecureClientCommunication(false); // is consumer currently communicating securly with end user?
        markupParams.setLocales(getLocalesAsStringArray(runtimeData.getLocales()));
        MediaManager mediaManager = new MediaManager();
        markupParams.setMimeTypes(new String[] { mediaManager.getReturnMimeType(mediaManager.getMedia(runtimeData.getBrowserInfo())) });
        markupParams.setMode(cd.getMode());
        markupParams.setWindowState(cd.getFocused() ? Constants.WSRP_SOLO : Constants.WSRP_NORMAL);
        //markupParams.setNavigationalState(""); // ???
        markupParams.setMarkupCharacterSets(new String[] {"UTF-8"});
        markupParams.setValidateTag(cd.getValidateTag());
        markupParams.setValidNewModes(new String[] {""}); // ??
        markupParams.setValidNewWindowStates(new String[] {""}); // ??
        cd.setMarkupParams(markupParams);
        return markupParams;        
    }
    
    /**
     * Generate a key that is based on the state of the
     * MarkupParams and InteractionParams objects. I would prefer
     * to just serialize these two objects into XML strings but
     * I had a hard time figuring out how to do that easily without
     * bringing in another third-party lib like Castor.
     * @param mp the current MarkupParams
     * @param ip the current InteractionParams
     * @return a key based on the MarkupParams and InteractionParams
     */
    private String generateKey(MarkupParams mp, InteractionParams ip) {
        StringBuffer sbKey = new StringBuffer(1024);
        if (mp != null) {
            sbKey.append("MarkupParams: ");
            // Capture the state of MarkupParams as a String
            sbKey.append(mp.isSecureClientCommunication()).append("|");
            sbKey.append(getStringArrayAsString(mp.getLocales())).append("|");
            sbKey.append(getStringArrayAsString(mp.getMimeTypes())).append("|");
            sbKey.append(mp.getMode()).append("|");
            sbKey.append(mp.getWindowState()).append("|");
            sbKey.append(mp.getClientData().getUserAgent()).append("|");
            sbKey.append(mp.getNavigationalState()).append("|");
            sbKey.append(getStringArrayAsString(mp.getMarkupCharacterSets())).append("|");
            sbKey.append(mp.getValidateTag()).append("|");
            sbKey.append(getStringArrayAsString(mp.getValidNewModes())).append("|");
            sbKey.append(getStringArrayAsString(mp.getValidNewWindowStates()));
            // Right now we're ignoring any extensions 
            sbKey.append("\n");          
        }
        
        if (ip != null) {
            sbKey.append("InteractionParams: ");
            // Capture the state of InteractionParams as a String
            sbKey.append(ip.getPortletStateChange().getValue()).append("|");
            sbKey.append(ip.getInteractionState()).append("|");
            sbKey.append(getNamedStringArrayAsString(ip.getFormParameters())).append("|");
            sbKey.append(getUploadContextArrayAsString(ip.getUploadContexts()));
            // Right now we're ignoring any extensions 
            sbKey.append("\n");          
        }
        //System.out.println("key:\n" + sbKey.toString());
        return sbKey.toString();
    }    
    
    /**
     * Helps turn a String array into a single string.
     * Used in generation of cache key.
     * @param strings the String array
     * @return the String array as a string
     */
    private String getStringArrayAsString(String[] strings) {
        String string = "null";
        if (strings != null) {
            StringBuffer sb = new StringBuffer();
            sb.append("{");
            for (int i = 0; i < strings.length; i++) {
                sb.append(strings[i]);
                if (i < strings.length - 1) {
                    sb.append(",");
                }            
            }
            sb.append("}");
            string = sb.toString();
        }
        return string;
    }

    /**
     * Helps turn a NamedString array into a single string.
     * Used in generation of cache key.
     * @param namedStrings the NamedString array
     * @return the NamedString array as a string
     */
    private String getNamedStringArrayAsString(NamedString[] namedStrings) {
        String string = "null";
        if (namedStrings != null) {
            StringBuffer sb = new StringBuffer();
            sb.append("{");
            for (int i = 0; i < namedStrings.length; i++) {
                sb.append(namedStrings[i].getName()).append("=");
                sb.append(namedStrings[i].getValue());
                if (i < namedStrings.length - 1) {
                    sb.append(",");
                }
            }
            sb.append("}");
            string = sb.toString();
        }
        return string;
    }
    
    /**
     * Helps turn a UploadContext array into a single string.
     * Used in generation of cache key.
     * @param uploadContexts the UploadContext array
     * @return the UploadContext array as a string
     */
    private String getUploadContextArrayAsString(UploadContext[] uploadContexts) {
        String string = "null";
        if (uploadContexts != null) {
            StringBuffer sb = new StringBuffer();
            sb.append("{");
            for (int i = 0; i < uploadContexts.length; i++) {
                sb.append(uploadContexts[i].getMimeType()).append(",");
                sb.append(new String(uploadContexts[i].getUploadData())).append(",");
                sb.append(getNamedStringArrayAsString(uploadContexts[i].getMimeAttributes()));
            }
            sb.append("}");
            string = sb.toString();
        }
        return string;
    }
        
    /**
     * Converts an array of Locales to an array of Strings.
     * @param locales the array of Locale objects
     * @return an array of String objects
     */
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
    
    /**
     * This method helps transfer request parameters out of uPortal's
     * ChannelRuntimeData object into an array of WSRP's NamedStrings.
     * @param runtimeData the channel runtime data
     * @return an array of NamedStrings
     */
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
