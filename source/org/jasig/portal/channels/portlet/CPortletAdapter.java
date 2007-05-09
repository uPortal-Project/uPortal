/* Copyright 2003 - 2005 The JA-SIG Collaborative.  All rights reserved.
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
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.PortletContainer;
import org.apache.pluto.PortletContainerImpl;
import org.apache.pluto.PortletContainerServices;
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
import org.jasig.portal.container.om.common.ObjectIDImpl;
import org.jasig.portal.container.om.entity.PortletApplicationEntityImpl;
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
import org.jasig.portal.container.servlet.PortletAttributeRequestWrapper;
import org.jasig.portal.container.servlet.PortletParameterRequestWrapper;
import org.jasig.portal.container.servlet.ServletObjectAccess;
import org.jasig.portal.container.servlet.ServletRequestImpl;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.security.IOpaqueCredentials;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.provider.NotSoOpaqueCredentials;
import org.jasig.portal.utils.NullOutputStream;
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
public class CPortletAdapter 
	implements IMultithreadedCharacterChannel, IMultithreadedPrivileged, IMultithreadedCacheable, IMultithreadedDirectResponse, IPortletAdaptor {
    
	protected final Log log = LogFactory.getLog(getClass());
        
    protected static Map channelStateMap;
    private static boolean portletContainerInitialized;
    private static PortletContainer portletContainer;
    private static ServletConfig servletConfig;
    private static ChannelCacheKey systemCacheKey;
    private static ChannelCacheKey instanceCacheKey;
    
    private static final String uniqueContainerName = 
        PropertiesManager.getProperty("org.jasig.portal.channels.portlet.CPortletAdapter.uniqueContainerName", "Pluto-in-uPortal");
    private static final String REQUEST_PARAMS_KEY = CPortletAdapter.class.getName() + ".REQUEST_PARAMS";
        
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
    
    /**
     * 
     * @param uid A String uniquely identifying the IChannel 'instance' we are virtually representing
     * as an IMultithreadedChannel.  
     * @throws PortalException
     */
    protected void initPortletContainer(String uid) throws PortalException {

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

            // Create the PortletApplicationEntity
            final PortletApplicationEntityImpl portAppEnt = new PortletApplicationEntityImpl();
            portAppEnt.setId(portletDefinition.getId().toString());
            portAppEnt.setPortletApplicationDefinition(portletDefinition.getPortletApplicationDefinition());
            
            // Create the PortletEntity
            PortletEntityImpl portletEntity = new PortletEntityImpl();
            portletEntity.setId(sd.getChannelPublishId());
            portletEntity.setPortletDefinition(portletDefinition);
            portletEntity.setChannelDefinition(channelDefinition);
            portletEntity.setPortletApplicationEntity(portAppEnt);
            portletEntity.setUserLayout(pcs.getUserPreferencesManager().getUserLayoutManager().getUserLayout());
            portletEntity.setChannelDescription((IUserLayoutChannelDescription)pcs.getUserPreferencesManager().getUserLayoutManager().getNode(sd.getChannelSubscribeId()));
            portletEntity.setPerson(sd.getPerson());
            portletEntity.loadPreferences();
            
            // Add the user information into the request See PLT.17.2.
            Map userInfo = cd.getUserInfo();
            
            if (userInfo == null) {
                UserAttributeListImpl userAttributeList = ((PortletApplicationDefinitionImpl)portletDefinition.getPortletApplicationDefinition()).getUserAttributes();
                
                // here we ask an overridable method to get the user attributes.
                // you can extend CPortletAdapter to change the implementation of
                // how we get user attributes.  This whole initPortletWindow method
                // is also overridable.
                //
                // Note that we will only call getUserInfo() once.
                userInfo = getUserInfo(uid, sd, userAttributeList);
                if (log.isTraceEnabled()) {
                    log.trace("For user [" + uid + "] got user info : [" + userInfo + "]");
                }
                cd.setUserInfo(userInfo);
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
                    catch (Exception e) {
                        log.error(e,e);
                    }
                    
                case PortalEvent.SESSION_DONE:
                    // For both SESSION_DONE and UNSUBSCRIBE, we might want to
                    // release resources here if we need to

                    PortletWindowImpl windowImpl = (PortletWindowImpl)cd.getPortletWindow();

                    try {
                        PortletStateManager.clearState(windowImpl);
                    }
                    catch (IllegalStateException ise) {
                        //Ignore an illegal state when the PortletStateManager tries to
                        //access the session if it has already been destroyed.
                    }

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
            // Register this portlet's channel subscribe ID in the JNDI context
            // this is probably not necessary since afaik nothing other than this
            // portlet is reading this List. - andrew petro
            
            List portletIds = (List)sd.getJNDIContext().lookup("/portlet-ids");
            portletIds.add(sd.getChannelSubscribeId());
        } catch (Exception e) {
            throw new PortalException("Error accessing /portlet-ids JNDI context.", e);
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
        ChannelData cd = channelState.getChannelData();
        ChannelStaticData sd = channelState.getStaticData();
        PortalControlStructures pcs = channelState.getPortalControlStructures();
        
        channelState.setRuntimeData(rd);

        if (!cd.isPortletWindowInitialized()) {
            this.initPortletWindow(uid);
        }

        try {
            PortletContainerServices.prepare(uniqueContainerName);
            
            final PortletWindowImpl portletWindow = (PortletWindowImpl)cd.getPortletWindow();
            final PortletEntity portletEntity = portletWindow.getPortletEntity();
            final PortletDefinition portletDef = portletEntity.getPortletDefinition();
            final HttpServletRequest baseRequest = pcs.getHttpServletRequest();

            HttpServletRequest wrappedRequest = new ServletRequestImpl(baseRequest, sd.getPerson(), portletDef.getInitSecurityRoleRefSet());
            
            //Wrap the request to scope attributes to this portlet instance
            wrappedRequest = new PortletAttributeRequestWrapper(wrappedRequest);
            
            //Set up request attributes (user info, portal session, etc...)
            setupRequestAttributes(wrappedRequest, uid);

            // Put the current runtime data and wrapped request into the portlet window
            portletWindow.setChannelRuntimeData(rd);
            portletWindow.setHttpServletRequest(wrappedRequest);

            // Get the portlet url manager which will analyze the request parameters
            DynamicInformationProvider dip = InformationProviderAccess.getDynamicProvider(wrappedRequest);
            PortletStateManager psm = ((DynamicInformationProviderImpl)dip).getPortletStateManager(portletWindow);
            PortletActionProvider pap = dip.getPortletActionProvider(portletWindow);

            //If portlet is rendering as root, change mode to maximized, otherwise minimized
            WindowState newWindowState = cd.getNewWindowState();
            if (!psm.isAction() && rd.isRenderingAsRoot()) {
                if (WindowState.MINIMIZED.equals(newWindowState)) {
                    pap.changePortletWindowState(WindowState.MINIMIZED);
                }
                else {
                    pap.changePortletWindowState(WindowState.MAXIMIZED);
                }
            } else if (newWindowState != null) {
                pap.changePortletWindowState(newWindowState);
            }
            else if (!psm.isAction()) {
                pap.changePortletWindowState(WindowState.NORMAL);
            }
            cd.setNewWindowState(null);

            //Check for a portlet mode change
            PortletMode newMode = cd.getNewPortletMode();
            if (newMode != null) {
                pap.changePortletMode(newMode);
                PortletStateManager.setMode(portletWindow, newMode);
            }
            cd.setNewPortletMode(null);

            // Process action if this is the targeted channel and the URL is an action URL
            if (rd.isTargeted() && psm.isAction()) {
                //Create a sink to throw out and output (portlets can't output content during an action)
                PrintWriter pw = new PrintWriter(new NullOutputStream());
                HttpServletResponse wrappedResponse = ServletObjectAccess.getStoredServletResponse(pcs.getHttpServletResponse(), pw);

                try {
                    //See if a WindowState change was requested for an ActionURL
                    final String newWindowStateName = wrappedRequest.getParameter(PortletStateManager.UP_WINDOW_STATE);
                    if (newWindowStateName != null) {
                        pap.changePortletWindowState(new WindowState(newWindowStateName));
                    }

                    HttpServletRequest wrappedPortletRequest = new PortletParameterRequestWrapper(wrappedRequest);

                    portletContainer.processPortletAction(portletWindow, wrappedPortletRequest, wrappedResponse);
                } catch (Exception e) {
                    throw new PortalException(e);
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
     * @return markup representing channel content
     */
    protected String getMarkup(String uid) throws PortalException {
        ChannelState channelState = (ChannelState)channelStateMap.get(uid);
        ChannelData cd = channelState.getChannelData();
        ChannelStaticData sd = channelState.getStaticData();
        PortalControlStructures pcs = channelState.getPortalControlStructures();
        
        try {
            PortletContainerServices.prepare(uniqueContainerName);
            
            final PortletWindowImpl portletWindow = (PortletWindowImpl)cd.getPortletWindow();
            final PortletEntity portletEntity = portletWindow.getPortletEntity();
            final PortletDefinition portletDef = portletEntity.getPortletDefinition();
            final HttpServletRequest baseRequest = pcs.getHttpServletRequest();

            HttpServletRequest wrappedRequest = new ServletRequestImpl(baseRequest, sd.getPerson(), portletDef.getInitSecurityRoleRefSet());
            
            //Wrap the request to scope attributes to this portlet instance
            wrappedRequest = new PortletAttributeRequestWrapper(wrappedRequest);
            
            //Set up request attributes (user info, portal session, etc...)
            setupRequestAttributes(wrappedRequest, uid);

            
            final StringWriter sw = new StringWriter();
            HttpServletResponse wrappedResponse = ServletObjectAccess.getStoredServletResponse(pcs.getHttpServletResponse(), new PrintWriter(sw));
           
                                                
            //Use the parameters from the last request so the portlet maintains it's state
            final ChannelRuntimeData rd = channelState.getRuntimeData();
            final String sessionParamsKey = REQUEST_PARAMS_KEY + "." + uid;
            if (!rd.isTargeted()) {
                final HttpSession portalSession = pcs.getHttpServletRequest().getSession(false);
                
                final Map requestParams;
                if (portalSession != null) {
                    requestParams = (Map)portalSession.getAttribute(sessionParamsKey);
                }
                else {
                    requestParams = null;
                }

                wrappedRequest = new DummyParameterRequestWrapper(wrappedRequest, requestParams);
            }
            //Hide the request parameters if this portlet isn't targeted
            else {
                wrappedRequest = new PortletParameterRequestWrapper(wrappedRequest);

                final HttpSession portalSession = pcs.getHttpServletRequest().getSession(true);
                portalSession.setAttribute(sessionParamsKey, wrappedRequest.getParameterMap());
            }

            portletContainer.renderPortlet(portletWindow, wrappedRequest, wrappedResponse);
            
            //Support for the portlet modifying it's cache timeout
            final Map properties = PropertyManager.getRequestProperties(portletWindow, wrappedRequest);
            final String[] exprCacheTimeStr = (String[])properties.get(RenderResponse.EXPIRATION_CACHE);
            
            if (exprCacheTimeStr != null && exprCacheTimeStr.length > 0) {
                try {
                    Integer.parseInt(exprCacheTimeStr[0]); //Check for valid number
                    cd.setExpirationCache(exprCacheTimeStr[0]);
                }
                catch (NumberFormatException nfe) {
                    log.error("The specified RenderResponse.EXPIRATION_CACHE value of (" + exprCacheTimeStr + ") is not a number.", nfe);
                    throw nfe;
                }
            }
            
            //Keep track of the last time the portlet was successfully rendered
            cd.setLastRenderTime(System.currentTimeMillis());
            
            //Return the content
            return sw.toString();
                        
        } catch (Throwable t) {
            log.error(t, t);
            throw new PortalException(t);
        } finally {
            PortletContainerServices.release();
        }
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
            
            final PortletWindowImpl portletWindow = (PortletWindowImpl)cd.getPortletWindow();
            final PortletEntity portletEntity = portletWindow.getPortletEntity();
            final PortletDefinition portletDef = portletEntity.getPortletDefinition();
            final HttpServletRequest baseRequest = pcs.getHttpServletRequest();

            HttpServletRequest wrappedRequest = new ServletRequestImpl(baseRequest, sd.getPerson(), portletDef.getInitSecurityRoleRefSet());
            
            //Wrap the request to scope attributes to this portlet instance
            wrappedRequest = new PortletAttributeRequestWrapper(wrappedRequest);
            
            //Set up request attributes (user info, portal session, etc...)
            setupRequestAttributes(wrappedRequest, uid);
            
            //Hide the request parameters if this portlet isn't targeted 
            wrappedRequest = new PortletParameterRequestWrapper(wrappedRequest);

            
            //Since the portlet is rendering through IDirectResponse change the window state to "exclusive"
            DynamicInformationProvider dip = InformationProviderAccess.getDynamicProvider(pcs.getHttpServletRequest());
            PortletActionProvider pap = dip.getPortletActionProvider(portletWindow);
            pap.changePortletWindowState(new WindowState("exclusive"));
            

            HttpServletResponse wrappedResponse = new OutputStreamResponseWrapper(response);

            //render the portlet
            portletContainer.renderPortlet(cd.getPortletWindow(), wrappedRequest, wrappedResponse);
            
            //Ensure all the data gets written out
            wrappedResponse.flushBuffer();
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
            password = this.getPassword(subContext);
        }
        
        return password;
    }
    
    /**
     * Adds the appropriate information to the request attributes of the portlet.
     * 
     * This is an extension point.  You can override this method to set other
     * request attributes.
     * 
     * @param request The request to add the attributes to
     * @param uid The uid of the request so the appropriate ChannelState can be accessed
     */
    protected void setupRequestAttributes(final HttpServletRequest request, final String uid) {
        final ChannelState channelState = (ChannelState)channelStateMap.get(uid);
        final ChannelData cd = channelState.getChannelData();
 
        //Add the user information map
        request.setAttribute(PortletRequest.USER_INFO, cd.getUserInfo());
    }
    
    /**
     * Get the Map of portlet user attribute names to portlet user attribute values.
     * 
     * This is an extension point.  You can extend CPortletAdapter and override this
     * method to implement the particular user attribute Map creation strategy that
     * you need to implement.  Such strategies might rename uPortal
     * user attributes to names that your particular portlet knows how to consume,
     * transform the user attribute values to forms expected by your portlet, add
     * additional attributes, convey a CAS proxy ticket or other security token.
     * This extension point is the way to accomodate the particular user attributes
     * particular portlets require.
     * 
     * The default implementation of this method includes in the userInfo Map
     * those uPortal IPerson attributes matching entries in the list of attributes the
     * Portlet declared it wanted.  Additionally, the default implementation copies
     * the cached user password if the Portlet declares it wants the user attribute
     * 'password'.
     * 
     * We take the uid as an argument even though the default implementation does
     * not use it because overriding implementations might use the uid to key into
     * state maps.
     * 
     * @param uid a String uniquely identifying the IChannel 'instance' we are emulating
     * as an IMultithreaded channel.  
     * @param staticData data associated with the particular instance of the portlet window for the particular
     * user session
     * @param userAttributes the user attributes requested by the Portlet
     * @return a Map from portlet user attribute names to portlet user attribute values.
     */
    protected Map getUserInfo(String uid, ChannelStaticData staticData, UserAttributeListImpl userAttributes) {
        
        final String PASSWORD_ATTR = "password";
        
        Map userInfo = new HashMap();
        IPerson person = staticData.getPerson();
        if (person.getSecurityContext().isAuthenticated()) {
            
            // for each attribute the Portlet requested
            for (Iterator iter = userAttributes.iterator(); iter.hasNext(); ) {
                UserAttributeImpl userAttribute = (UserAttributeImpl)iter.next();
                String attName = userAttribute.getName();
                String attValue = (String)person.getAttribute(attName);
                if ((attValue == null || attValue.equals("")) && attName.equals(PASSWORD_ATTR)) {
                    attValue = getPassword(person.getSecurityContext());
                }
                userInfo.put(attName, attValue);
            }
        }
        return userInfo;
    }
}

