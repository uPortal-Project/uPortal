/* Copyright 2003 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels.portlet;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.PortletContainer;
import org.apache.pluto.PortletContainerServices;
import org.apache.pluto.core.InternalActionResponse;
import org.apache.pluto.factory.PortletObjectAccess;
import org.apache.pluto.om.entity.PortletEntity;
import org.apache.pluto.om.portlet.PortletDefinition;
import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.services.information.DynamicInformationProvider;
import org.apache.pluto.services.information.InformationProviderAccess;
import org.apache.pluto.services.information.PortletActionProvider;
import org.apache.pluto.services.property.PropertyManager;
import org.apache.pluto.services.property.PropertyManagerService;
import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.ChannelRegistryStoreFactory;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IMultithreadedCacheable;
import org.jasig.portal.IMultithreadedCharacterChannel;
import org.jasig.portal.IMultithreadedDirectResponse;
import org.jasig.portal.IMultithreadedPrivileged;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.container.IPortletActionResponse;
import org.jasig.portal.container.PortletContainerImpl;
import org.jasig.portal.container.om.common.ObjectIDImpl;
import org.jasig.portal.container.om.entity.PortletEntityImpl;
import org.jasig.portal.container.om.portlet.PortletApplicationDefinitionImpl;
import org.jasig.portal.container.om.portlet.PortletDefinitionImpl;
import org.jasig.portal.container.om.portlet.UserAttributeImpl;
import org.jasig.portal.container.om.portlet.UserAttributeListImpl;
import org.jasig.portal.container.om.window.PortletWindowImpl;
import org.jasig.portal.container.services.FactoryManagerServiceImpl;
import org.jasig.portal.container.services.PortletContainerEnvironmentImpl;
import org.jasig.portal.container.services.information.DynamicInformationProviderImpl;
import org.jasig.portal.container.services.information.InformationProviderServiceImpl;
import org.jasig.portal.container.services.information.PortletStateManager;
import org.jasig.portal.container.services.log.LogServiceImpl;
import org.jasig.portal.container.services.property.PropertyManagerServiceImpl;
import org.jasig.portal.container.servlet.DummyParameterRequestWrapper;
import org.jasig.portal.container.servlet.PortletParameterRequestWrapper;
import org.jasig.portal.container.servlet.ServletObjectAccess;
import org.jasig.portal.container.servlet.ServletRequestImpl;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.security.IOpaqueCredentials;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.provider.NotSoOpaqueCredentials;
import org.jasig.portal.utils.SAXHelper;
import org.xml.sax.ContentHandler;

/**
 * A JSR 168 Portlet adapter that presents a portlet
 * through the uPortal channel interface.
 * <p> 
 * There is a related channel type called
 * "Portlet Adapter" that is included with uPortal, so to use
 * this channel, just select the "Portlet" type when publishing.
 * </p>
 * <p>
 * Note: A portlet can specify the String "password" in the 
 * user attributes section of the portlet.xml.  In this is done,
 * this adapter will look for the user's cached password. If
 * the user's password is being stored in memory by a caching
 * security context, the adapter will consult the cache to fill the
 * request for the attribute. If the user's password is not cached,
 * <code>null</code> will be set for the attributes value.
 * </p>
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class CPortletAdapter implements IMultithreadedCharacterChannel, IMultithreadedPrivileged, IMultithreadedCacheable, IMultithreadedDirectResponse {
    private static final Log log = LogFactory.getLog(CPortletAdapter.class);
        
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
            PropertyManagerService propertyManagerService = new PropertyManagerServiceImpl();
            
            logService.init(servletConfig, null);
            factorManagerService.init(servletConfig, null);
            informationProviderService.init(servletConfig, null);
            
            environment.addContainerService(logService);
            environment.addContainerService(factorManagerService);
            environment.addContainerService(informationProviderService);
            environment.addContainerService(propertyManagerService);

            //Call added in case the context has been re-loaded
            PortletContainerServices.destroyReference(uniqueContainerName);
            portletContainer = new PortletContainerImpl();
            portletContainer.init(uniqueContainerName, servletConfig, environment, new Properties());
            
            portletContainerInitialized = true;
        
        } catch (Exception e) {
            String message = "Initialization of the portlet container failed.";
            log.error( message, e);
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
            
            // Create the PortletDefinition
            PortletDefinitionImpl portletDefinition = (PortletDefinitionImpl)InformationProviderAccess.getStaticProvider().getPortletDefinition(ObjectIDImpl.createFromString(portletDefinitionId));
            if (portletDefinition == null) {
                throw new PortalException("Unable to find portlet definition for ID '" + portletDefinitionId + "'");
            }
            ChannelDefinition channelDefinition = ChannelRegistryStoreFactory.getChannelRegistryStoreImpl().getChannelDefinition(Integer.parseInt(sd.getChannelPublishId()));
            portletDefinition.setChannelDefinition(channelDefinition);
            portletDefinition.loadPreferences();
            
            // Create the PortletEntity
            PortletEntityImpl portletEntity = new PortletEntityImpl();
            portletEntity.setId(sd.getChannelPublishId());
            portletEntity.setPortletDefinition(portletDefinition);
            portletEntity.setUserLayout(pcs.getUserPreferencesManager().getUserLayoutManager().getUserLayout());
            portletEntity.setChannelDescription((IUserLayoutChannelDescription)pcs.getUserPreferencesManager().getUserLayoutManager().getNode(sd.getChannelSubscribeId()));
            portletEntity.setPerson(sd.getPerson());
            portletEntity.loadPreferences();
            
            // Add the user information into the request See PLT.17.2.
            Map userInfo = cd.getUserInfo();
            if (userInfo == null) {
                userInfo = new HashMap();
                IPerson person = sd.getPerson();
                if (person.getSecurityContext().isAuthenticated()) {
                    UserAttributeListImpl userAttributes = ((PortletApplicationDefinitionImpl)portletDefinition.getPortletApplicationDefinition()).getUserAttributes();
                    for (Iterator iter = userAttributes.iterator(); iter.hasNext(); ) {
                        UserAttributeImpl userAttribute = (UserAttributeImpl)iter.next();
                        String attName = userAttribute.getName();
                        String attValue = (String)person.getAttribute(attName);
                        final String PASSWORD_ATTR = "password";
                        if ((attValue == null || attValue.equals("")) && attName.equals(PASSWORD_ATTR)) {
                            attValue = getPassword(person.getSecurityContext());
                        }
                        userInfo.put(attName, attValue);
                    }
                    cd.setUserInfo(userInfo);
                }
            }
            
            // Wrap the request
            ServletRequestImpl wrappedRequest = new ServletRequestImpl(pcs.getHttpServletRequest(), sd.getPerson(), portletDefinition.getInitSecurityRoleRefSet());
             
            // Now create the PortletWindow and hold a reference to it
            PortletWindowImpl portletWindow = new PortletWindowImpl();
            portletWindow.setId(sd.getChannelSubscribeId());
            portletWindow.setPortletEntity(portletEntity);
			portletWindow.setChannelRuntimeData(rd);
            portletWindow.setHttpServletRequest(wrappedRequest);
            cd.setPortletWindow(portletWindow);
                
            // Ask the container to load the portlet
            synchronized(this) {
                portletContainer.portletLoad(portletWindow, wrappedRequest, pcs.getHttpServletResponse());
            }
            
            cd.setPortletWindowInitialized(true);
            
        } catch (Exception e) {
            String message = "Initialization of the portlet container failed.";
            log.error( message, e);
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
            
            switch (ev.getEventNumber()) {
                
                // Detect portlet mode changes  
                
                // Cannot use the PortletActionProvider to change modes here. It uses
                // PortletWindow information to store the changes and the window is
                // not current at this point.
                
                case PortalEvent.EDIT_BUTTON_EVENT:
                    cd.setNewPortletMode(PortletMode.EDIT);
                    break;
                case PortalEvent.HELP_BUTTON_EVENT:
                    cd.setNewPortletMode(PortletMode.HELP);
                    break;
                case PortalEvent.ABOUT_BUTTON_EVENT:
                    // We might want to consider a custom ABOUT mode here
                    break;
                    
                //Detect portlet window state changes
                case PortalEvent.MINIMIZE_EVENT:
                    cd.setNewWindowState(WindowState.MINIMIZED);
                    break;
                
                case PortalEvent.MAXIMIZE_EVENT:
                    cd.setNewWindowState(WindowState.NORMAL);
                    break;
                    
                case PortalEvent.DETACH_BUTTON_EVENT:
                    cd.setNewWindowState(WindowState.MAXIMIZED);
                    break;
            
                //Detect end of session or portlet removed from layout
                case PortalEvent.UNSUBSCRIBE:
                    //User is removing this portlet from their layout, remove all
                    //the preferences they have stored for it.
                    PortletEntityImpl pe = (PortletEntityImpl)cd.getPortletWindow().getPortletEntity();
                    try {
                        pe.removePreferences();
                    }
                    catch (Exception e) { }
                    
                case PortalEvent.SESSION_DONE:
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
        
        ChannelStaticData sd = channelState.getStaticData();
        ChannelData cd = channelState.getChannelData();
        
        try {
            PortletContainerServices.prepare(uniqueContainerName);
            
            if (cd.isPortletWindowInitialized() && !cd.hasProcessedAction()) {
				PortalControlStructures pcs = channelState.getPortalControlStructures();
				HttpServletRequest wrappedRequest = new ServletRequestImpl(pcs.getHttpServletRequest(), sd.getPerson(), 
                        cd.getPortletWindow().getPortletEntity().getPortletDefinition().getInitSecurityRoleRefSet());
                
                // Add the user information
                wrappedRequest.setAttribute(PortletRequest.USER_INFO, cd.getUserInfo());
 
                // Put the current runtime data and wrapped request into the portlet window
                PortletWindowImpl portletWindow = (PortletWindowImpl)cd.getPortletWindow();
                portletWindow.setChannelRuntimeData(rd);
                portletWindow.setHttpServletRequest(wrappedRequest);
                
                // Get the portlet url manager which will analyze the request parameters
                DynamicInformationProvider dip = InformationProviderAccess.getDynamicProvider(wrappedRequest);
                PortletStateManager psm = ((DynamicInformationProviderImpl)dip).getPortletStateManager(portletWindow);
                
                PortletActionProvider pap = dip.getPortletActionProvider(portletWindow);
 
                //If portlet is rendering as root, change mode to maximized, otherwise minimized
                WindowState newWindowState = cd.getNewWindowState();
                if (rd.isRenderingAsRoot()) {
                    if (WindowState.MINIMIZED.equals(newWindowState)) {
                        pap.changePortletWindowState(WindowState.MINIMIZED);
                    }
                    else {
                        pap.changePortletWindowState(WindowState.MAXIMIZED);
                    }
                } else if (newWindowState != null) {
                    pap.changePortletWindowState(newWindowState);
                }
                else {
                    pap.changePortletWindowState(WindowState.NORMAL);
                }
                cd.setNewWindowState(null);
                
                PortletMode newMode = cd.getNewPortletMode();
                if (newMode != null) {
                    pap.changePortletMode(newMode);
                }
                cd.setNewPortletMode(null);
                
                // Process action if this is the targeted channel and the URL is an action URL
                if (rd.isTargeted() && psm.isAction()) {
                    try {
                        StringWriter sw = new StringWriter();
                        PrintWriter pw = new PrintWriter(sw);
                        HttpServletResponse wrappedResponse = ServletObjectAccess.getStoredServletResponse(pcs.getHttpServletResponse(), pw);
                        wrappedRequest = new PortletParameterRequestWrapper(wrappedRequest);
                        
                        portletContainer.processPortletAction(portletWindow, wrappedRequest, wrappedResponse);
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
        PortletWindow portletWindow = cd.getPortletWindow();
        PortalControlStructures pcs = channelState.getPortalControlStructures();
        
        String markup = "<b>Problem rendering portlet " + sd.getParameter("portletDefinitionId") + "</b>";
        
        try {
            PortletContainerServices.prepare(uniqueContainerName);

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            HttpServletRequest wrappedRequest = ((PortletWindowImpl)cd.getPortletWindow()).getHttpServletRequest();
            HttpServletResponse wrappedResponse = ServletObjectAccess.getStoredServletResponse(pcs.getHttpServletResponse(), pw);
            transferActionResultsToRequest(channelState, wrappedRequest);
            
                                                
            // Hide the request parameters if this portlet isn't targeted
            if (!rd.isTargeted()) {
                wrappedRequest = new DummyParameterRequestWrapper(wrappedRequest, cd.getLastRequestParameters());
            }
            // Use the parameters from the last request so the portlet maintains it's state
            else {
                wrappedRequest = new PortletParameterRequestWrapper(wrappedRequest);
                cd.setLastRequestParameters(wrappedRequest.getParameterMap());
            }
            
            // Add the user information
            wrappedRequest.setAttribute(PortletRequest.USER_INFO, cd.getUserInfo());
            
            //System.out.println("Rendering portlet " + cd.getPortletWindow().getId());
            portletContainer.renderPortlet(portletWindow, wrappedRequest, wrappedResponse);
            
            //Support for the portlet modifying it's cache timeout
            Map properties = PropertyManager.getRequestProperties(portletWindow, wrappedRequest);
            String[] exprCacheTimeStr = (String[])properties.get(RenderResponse.EXPIRATION_CACHE);
            
            if (exprCacheTimeStr != null && exprCacheTimeStr.length > 0) {
                PortletEntity pe = portletWindow.getPortletEntity();
                PortletDefinition pd = pe.getPortletDefinition();
                                
                try {
                    Integer.parseInt(exprCacheTimeStr[0]); //Check for valid number
                    cd.setExpirationCache(exprCacheTimeStr[0]);
                }
                catch (NumberFormatException nfe) {
                    log.error("The specified RenderResponse.EXPIRATION_CACHE value of (" + exprCacheTimeStr + ") is not a number.", nfe);
                    throw nfe;
                }
            }
            
            markup = sw.toString();
            
            cd.setProcessedAction(false);
            ((PortletWindowImpl)cd.getPortletWindow()).setPortletActionResponse(null);
                        
        } catch (Throwable t) {
            // TODO: review this
            // t.printStackTrace();
            // since the stack trace will be logged, this printStackTrace()
            // was overkill? -andrew.petro@yale.edu
            
            log.error(t, t);
            throw new PortalException(t.getMessage());
        } finally {
            PortletContainerServices.release();
        }
        
        //Keep track of the last time the portlet was successfully rendered
        cd.setLastRenderTime(System.currentTimeMillis());
        
        return markup;
    }
    
    //***************************************************************
    // IMultithreadedCacheable methods
    //***************************************************************  
    
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
        
        PortletWindow pw = cd.getPortletWindow();
        PortletEntity pe = pw.getPortletEntity();
        PortletDefinition pd = pe.getPortletDefinition();
        
        //Expiration based caching support for the portlet.
        String portletSetExprCacheTime = cd.getExpirationCache();
        String exprCacheTimeStr = pd.getExpirationCache();
        try {
            if (portletSetExprCacheTime != null)
                exprCacheTimeStr = portletSetExprCacheTime;
            
            int exprCacheTime = Integer.parseInt(exprCacheTimeStr);

            if (exprCacheTime == 0) {
                return false;
            }
            else if (exprCacheTime > 0) {
                long lastRenderTime = cd.getLastRenderTime();

                if ((lastRenderTime + (exprCacheTime * 1000)) < System.currentTimeMillis())
                    return false;
            }
        }
        catch (Exception e) {
            if (log.isWarnEnabled()) {
                String portletId = staticData.getParameter(portletDefinitionIdParamName);
                log.warn("Error parsing portlet expiration time (" + exprCacheTimeStr + ") for portlet (" + portletId + ").", e);
            }
        }

        // Determine if the channel focus has changed
        boolean previouslyFocused = cd.isFocused();
        cd.setFocused(runtimeData.isRenderingAsRoot());
        boolean focusHasSwitched = cd.isFocused() != previouslyFocused;
    
        // Dirty cache only when we receive an event, one or more request params, or a change in focus
        boolean cacheValid = !(cd.hasReceivedEvent() || runtimeData.isTargeted() || focusHasSwitched);
    
        cd.setReceivedEvent(false);
        return cacheValid;
    }

    
    //***************************************************************
    // IDirectResponse methods
    //***************************************************************  
    
    public synchronized void setResponse(String uid, HttpServletResponse response) {        
        ChannelState channelState = (ChannelState)channelStateMap.get(uid);
        ChannelData cd = channelState.getChannelData();
        ChannelStaticData sd = channelState.getStaticData();
        PortalControlStructures pcs = channelState.getPortalControlStructures();
        
        try {
            PortletContainerServices.prepare(uniqueContainerName);

            PortletWindowImpl portletWindow = (PortletWindowImpl)cd.getPortletWindow();
            
            // Get the portlet url manager which will analyze the request parameters
            DynamicInformationProvider dip = InformationProviderAccess.getDynamicProvider(pcs.getHttpServletRequest());
            PortletStateManager psm = ((DynamicInformationProviderImpl)dip).getPortletStateManager(portletWindow);
            
            //Since the portlet is rendering through IDirectResponse change the window state to "exclusive"
            PortletActionProvider pap = dip.getPortletActionProvider(portletWindow);
            pap.changePortletWindowState(new WindowState("exclusive"));

            //Create the request to send to the portlet container
            HttpServletRequest wrappedRequest = new ServletRequestImpl(pcs.getHttpServletRequest(), sd.getPerson(), 
                    portletWindow.getPortletEntity().getPortletDefinition().getInitSecurityRoleRefSet());
            transferActionResultsToRequest(channelState, wrappedRequest);
                
            // Add the user information to the request
            wrappedRequest.setAttribute(PortletRequest.USER_INFO, cd.getUserInfo());
            wrappedRequest = new PortletParameterRequestWrapper(wrappedRequest);

            //render the portlet
            portletContainer.renderPortlet(cd.getPortletWindow(), wrappedRequest, response);
                        
        } catch (Throwable t) {
            log.error(t, t);
        } finally {
            PortletContainerServices.release();
        }
    }
    
    
    //***************************************************************
    // Helper methods
    //***************************************************************  

    /**
     * Checks if the portlet has just processed an action during this request. If so then the
     * changes that the portlet may have made during it's processAction are captured from the 
     * portlet's ActionResponse and they are added to the request that will be passed to the portlet
     * container.
     * 
     * <br>
     * <b>PortletContainerServices.prepare</b> MUST be called before this method is called.
     * <br>
     * <b>PortletContainerServices.release</b> MUST be called after this method is called. 
     * 
     * @param channelState The state to read the action information from
     * @param wrappedRequest The request to add data from the action to
     */
    private void transferActionResultsToRequest(ChannelState channelState, HttpServletRequest wrappedRequest) {
        ChannelData cd = channelState.getChannelData();
        PortalControlStructures pcs = channelState.getPortalControlStructures();
        
        if (cd.hasProcessedAction()) {
            IPortletActionResponse actionResponse = ((PortletWindowImpl)cd.getPortletWindow()).getPortletActionResponse();
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
    }
    
    /**
     * Retrieves the users password by iterating over
     * the user's security contexts and returning the first
     * available cached password.
     * 
     * @param baseContext The security context to start looking for a password from.
     * @return the users password
     */
    private String getPassword(ISecurityContext baseContext) {
        String password = null;
        IOpaqueCredentials oc = baseContext.getOpaqueCredentials();
        
        if (oc instanceof NotSoOpaqueCredentials) {
            NotSoOpaqueCredentials nsoc = (NotSoOpaqueCredentials)oc;
            password = nsoc.getCredentials();
        }

        // If still no password, loop through subcontexts to find cached credentials
        Enumeration en = baseContext.getSubContexts();
        while (password == null && en.hasMoreElements()) {
            ISecurityContext subContext = (ISecurityContext)en.nextElement();
            IOpaqueCredentials soc = subContext.getOpaqueCredentials();
            
            if (soc instanceof NotSoOpaqueCredentials) {
                NotSoOpaqueCredentials nsoc = (NotSoOpaqueCredentials)soc;
                password = nsoc.getCredentials();
            }
            
            if (password == null) {
                password = this.getPassword(subContext);
            }
        }
        
        return password;
    }
}

