/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.portlet.registry;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.Validate;
import org.apache.pluto.container.PortletWindow;
import org.apache.pluto.container.PortletWindowID;
import org.apache.pluto.container.om.portlet.PortletDefinition;
import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.IUserProfile;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.dao.IStylesheetDescriptorDao;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.layout.om.IStylesheetDescriptor;
import org.jasig.portal.layout.om.IStylesheetParameterDescriptor;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.jasig.portal.utils.ConcurrentMapUtils;
import org.jasig.portal.utils.web.PortalWebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;

/**
 * Provides the default implementation of the window registry, the backing for the storage
 * of IPortletWindow objects is a Map stored in the HttpSession for the user.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service
public class PortletWindowRegistryImpl implements IPortletWindowRegistry {
    static final char ID_PART_SEPERATOR = '.';
    static final Pattern ID_PART_SEPERATOR_PATTERN = Pattern.compile(Pattern.quote(String.valueOf(ID_PART_SEPERATOR)));
    
    static final String STATELESS_PORTLET_WINDOW_ID = "tw";
    static final String PORTLET_WINDOW_DATA_ATTRIBUTE = PortletWindowRegistryImpl.class.getName() + ".PORTLET_WINDOW_DATA";
    static final String PORTLET_WINDOW_ATTRIBUTE = PortletWindowRegistryImpl.class.getName() + ".PORTLET_WINDOW.thread-";

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private Set<WindowState> persistentWindowStates = Sets.newHashSet(WindowState.MINIMIZED);
    private IPortletEntityRegistry portletEntityRegistry;
    private IPortletDefinitionRegistry portletDefinitionRegistry;
    private IStylesheetDescriptorDao stylesheetDescriptorDao;
    private IUserInstanceManager userInstanceManager;
    private IPortalRequestUtils portalRequestUtils;
    
    
    /**
     * The set of WindowStates that should be copied to the {@link IPortletEntity} when {@link #storePortletWindow(HttpServletRequest, IPortletWindow)}
     * is called
     */
    public void setPersistentWindowStates(Set<WindowState> persistentWindowStates) {
        this.persistentWindowStates = persistentWindowStates;
    }

    @Autowired
    public void setStylesheetDescriptorDao(IStylesheetDescriptorDao stylesheetDescriptorDao) {
        this.stylesheetDescriptorDao = stylesheetDescriptorDao;
    }
    @Autowired
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }
    @Autowired
    public void setPortletEntityRegistry(IPortletEntityRegistry portletEntityRegistry) {
        this.portletEntityRegistry = portletEntityRegistry;
    }
    @Autowired
    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }
    @Autowired
	public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
		this.portalRequestUtils = portalRequestUtils;
	}
    
	/* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletWindowRegistry#convertPortletWindow(javax.servlet.http.HttpServletRequest, org.apache.pluto.PortletWindow)
     */
    @Override
    public IPortletWindow convertPortletWindow(HttpServletRequest request, PortletWindow plutoPortletWindow) {
        Validate.notNull(request, "request can not be null");
        Validate.notNull(plutoPortletWindow, "portletWindow can not be null");
        
        //Conver the pluto portlet window ID into a uPortal portlet window ID
        final PortletWindowID plutoWindowId = plutoPortletWindow.getId();
        final IPortletWindowId portletWindowId;
        if (plutoWindowId instanceof IPortletWindowId) {
            portletWindowId = (IPortletWindowId)plutoWindowId;
        }
        else {
            portletWindowId = this.getPortletWindowId(request, plutoWindowId.getStringId());
        }
        
        //Do a new get to make sure the referenced data gets updated correctly
        return this.getPortletWindow(request, portletWindowId);
    }
    
    @Override
    public IPortletWindow getOrCreateDefaultPortletWindowByFname(HttpServletRequest request, String fname) {
        Validate.notNull(request, "HttpServletRequest cannot be null");
        Validate.notNull(fname, "fname cannot be null");
        
        final IPortletDefinition portletDefinition = this.portletDefinitionRegistry.getPortletDefinitionByFname(fname);
        if (portletDefinition == null) {
            logger.debug("No IPortletDefinition found for fname {}, no IPortletWindow will be returned.", fname);
            return null;
        }
        
        final IPortletDefinitionId portletDefinitionId = portletDefinition.getPortletDefinitionId();
        final IPortletWindow portletWindow = this.getOrCreateDefaultPortletWindow(request, portletDefinitionId);
        logger.trace("Found portlet window {} for portlet definition fname {}", portletWindow, fname);
        
        return portletWindow;
    }
    
    @Override
    public IPortletWindow getOrCreateDefaultPortletWindowByLayoutNodeId(HttpServletRequest request, String subscribeId) {
        Validate.notNull(request, "HttpServletRequest cannot be null");
        Validate.notNull(subscribeId, "subscribeId cannot be null");
        
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();
        final IUserLayoutNodeDescription node = userLayoutManager.getNode(subscribeId);
        if (node == null) {
            logger.debug("No layout node found for id {}, no IPortletWindow will be returned.", subscribeId);
            return null;
        }
        if (node.getType() != IUserLayoutChannelDescription.CHANNEL) {
            logger.debug("Layout node for id {} is not a portlet, it is a {}, no IPortletWindow will be returned.", subscribeId, node.getType());
            return null;
        }
        logger.trace("Found layout node {} for id {}", node, subscribeId);
        
        final IUserLayoutChannelDescription portletNode = (IUserLayoutChannelDescription)node;
        final String channelPublishId = portletNode.getChannelPublishId();
        final IPortletDefinition portletDefinition = this.portletDefinitionRegistry.getPortletDefinition(channelPublishId);
        if (portletDefinition == null) {
            logger.debug("No IPortletDefinition found for id {}, no IPortletWindow will be returned.", channelPublishId);
            return null;
        }
        
        final IPortletDefinitionId portletDefinitionId = portletDefinition.getPortletDefinitionId();
        final IPortletWindow portletWindow = this.getOrCreateDefaultPortletWindow(request, portletDefinitionId);
        logger.trace("Found portlet window {} for layout node {}", portletWindow, portletNode);
        
        return portletWindow;
    }
    
    @Override
    public IPortletWindow getOrCreateDefaultPortletWindow(HttpServletRequest request, IPortletDefinitionId portletDefinitionId) {
        Validate.notNull(request, "HttpServletRequest cannot be null");
        Validate.notNull(portletDefinitionId, "portletDefinition cannot be null");
     
        final IPortletEntity portletEntity = this.portletEntityRegistry.getOrCreateDefaultPortletEntity(request, portletDefinitionId);
        final IPortletEntityId portletEntityId = portletEntity.getPortletEntityId();
        
        return this.getOrCreateDefaultPortletWindow(request, portletEntityId);
    }

    @Override
    public IPortletWindow getOrCreateDefaultPortletWindow(HttpServletRequest request, IPortletEntityId portletEntityId) {
        Validate.notNull(request, "request can not be null");
        Validate.notNull(portletEntityId, "portletEntityId can not be null");
        
        final IPortletWindowId portletWindowId = this.getDefaultPortletWindowId(request, portletEntityId);

        final Map<IPortletWindowId, IPortletWindow> portletWindowMap = getPortletWindowMap(request);
        
        //Check if there is portlet window cached in the request
        IPortletWindow portletWindow = portletWindowMap.get(portletWindowId);
        if (portletWindow != null) {
            logger.trace("Found IPortletWindow {} in request cache", portletWindow.getPortletWindowId());
            return portletWindow;
        }
        
        final PortletWindowData portletWindowData = this.getOrCreateDefaultPortletWindowData(request, portletEntityId, portletWindowId);
        portletWindow = wrapPortletWindowData(request, portletWindowData);
        
        //Cache the wrapped window in the request
        portletWindowMap.put(portletWindowId, portletWindow);
        
        return portletWindow;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletWindowRegistry#getPortletWindow(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindowId)
     */
    @Override
    public IPortletWindow getPortletWindow(HttpServletRequest request, IPortletWindowId portletWindowId) {
        Validate.notNull(request, "request can not be null");
        Validate.notNull(portletWindowId, "portletWindowId can not be null");
        
        final Map<IPortletWindowId, IPortletWindow> portletWindowMap = getPortletWindowMap(request);
        
        IPortletWindow portletWindow = portletWindowMap.get(portletWindowId);
        if (portletWindow != null) {
            logger.trace("Found IPortletWindow {} in request cache", portletWindow.getPortletWindowId());
            return portletWindow;
        }
        
        final PortletWindowData portletWindowData = this.getPortletWindowData(request, portletWindowId);
        if (portletWindowData == null) {
            logger.trace("No IPortletWindow {} exists, returning null");
            return null;
        }
        
        portletWindow = this.wrapPortletWindowData(request, portletWindowData);
        
        //Cache the wrapped window in the request
        portletWindowMap.put(portletWindowId, portletWindow);
        
        return portletWindow;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletWindowRegistry#getPortletWindowId(java.lang.String)
     */
    @Override
    public PortletWindowIdImpl getPortletWindowId(HttpServletRequest request, String portletWindowId) {
        Validate.notNull(portletWindowId, "portletWindowId can not be null");
        
        final String[] portletWindowIdParts = ID_PART_SEPERATOR_PATTERN.split(portletWindowId);
        
        final String entityIdStr;
        final String instanceId;
        if (portletWindowIdParts.length == 1) {
            entityIdStr = portletWindowIdParts[0];
            instanceId = null;
        }
        else if (portletWindowIdParts.length == 2) {
            entityIdStr = portletWindowIdParts[0];
            instanceId = portletWindowIdParts[1];
        }
        else  {
            throw new IllegalArgumentException("Provided portlet window ID '" + portletWindowId + "' is not valid");
        }
        
        final IPortletEntity portletEntity = this.portletEntityRegistry.getPortletEntity(request, entityIdStr);
        if (portletEntity == null) {
            throw new IllegalArgumentException("No parent IPortletEntity found for id '" + entityIdStr + "' from portlet window id: " + portletWindowId);
        }

        return createPortletWindowId(instanceId, portletEntity.getPortletEntityId());
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletWindowRegistry#getDefaultPortletWindowId(org.jasig.portal.portlet.om.IPortletEntityId)
     */
    @Override
    public IPortletWindowId getDefaultPortletWindowId(HttpServletRequest request, IPortletEntityId portletEntityId) {
        final IPortletWindowId portletWindowId = this.createPortletWindowId(null, portletEntityId);
        logger.trace("Determined default portlet window id {} for portlet entity {}", portletWindowId, portletEntityId);
        return portletWindowId;
    }
    
    @Override
    public IPortletWindow createDelegatePortletWindow(HttpServletRequest request, IPortletEntityId portletEntityId, IPortletWindowId delegationParentId) {
//        Validate.notNull(request, "request can not be null");
//        Validate.notNull(portletEntityId, "portletEntityId can not be null");
//        Validate.notNull(delegationParentId, "delegationParentId can not be null");
//
//        //Create the window
//        final IPortletWindowId portletWindowId = this.getDefaultPortletWindowId(portletEntityId);
//        final IPortletWindow portletWindow = this.createPortletWindow(request, portletWindowId, portletEntityId, delegationParentId);
//        
//        //Store it in the request
//        this.storePortletWindow(request, portletWindow);
//        
//        return this.wrapPortletWindowForRequest(request, portletWindow);
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Set<IPortletWindow> getAllPortletWindowsForEntity(HttpServletRequest request, IPortletEntityId portletEntityId) {
        Validate.notNull(request, "request can not be null");
        Validate.notNull(portletEntityId, "portletEntityId can not be null");
        
        //TODO this is really expensive, maybe we need a domain specific object that is better than a Map and allows us to key this data off of windowId & entityId like the entity registry has

        final Set<IPortletWindow> portletWindows = new LinkedHashSet<IPortletWindow>();
        
        final Map<IPortletWindowId, IPortletWindow> portletWindowMap = getPortletWindowMap(request);
        //Add all of the request cached windows for the entity to the set
        for (final IPortletWindow portletWindow : portletWindowMap.values()) {
            final IPortletEntity portletEntity = portletWindow.getPortletEntity();
            if (portletEntityId.equals(portletEntity.getPortletEntityId())) {
                portletWindows.add(portletWindow);
            }
        }

        //Check for session cached windows that haven't been accessed in this request
        final ConcurrentMap<IPortletWindowId, PortletWindowData> portletWindowDataMap = getPortletWindowDataMap(request);
        for (final PortletWindowData portletWindowData : portletWindowDataMap.values()) {
            final IPortletWindowId portletWindowId = portletWindowData.getPortletWindowId();
            
            //Skip data windows that aren't for this entity and for windows that are already in the request cache
            if (!portletEntityId.equals(portletWindowData.getPortletEntityId()) || portletWindowMap.containsKey(portletWindowId)) {
                continue;
            }

            //Wrap the data in a window and stick it in the request cache
            IPortletWindow portletWindow = this.wrapPortletWindowData(request, portletWindowData);
            portletWindowMap.put(portletWindowId, portletWindow);
            
            portletWindows.add(portletWindow);
        }
        
        //If there were no windows in the set create the default one for the entity
        if (portletWindows.isEmpty()) {
            final IPortletWindow portletWindow = this.getOrCreateDefaultPortletWindow(request, portletEntityId);
            portletWindows.add(portletWindow);
        }
            
        return portletWindows;
    }
    
    @Override
    public IPortletWindowId getStatelessPortletWindowId(HttpServletRequest request, IPortletWindowId basePortletWindowId) {
        //Need the basePortletWindowId to be an instance of PortletWindowIdImpl so that we can extract the entity ID
        if (!(basePortletWindowId instanceof PortletWindowIdImpl)) {
            final String basePortletWindowIdStr = basePortletWindowId.getStringId();
            basePortletWindowId = this.getPortletWindowId(request, basePortletWindowIdStr);
            
        }
        
        //Get the entity ID for the portlet window
        final IPortletEntityId portletEntityId = ((PortletWindowIdImpl)basePortletWindowId).getPortletEntityId();
        
        //Create the stateless ID
        final PortletWindowIdImpl statelessPortletWindowId = this.createPortletWindowId(STATELESS_PORTLET_WINDOW_ID, portletEntityId);
        
        //See if there is already a request cached stateless window
        IPortletWindow statelessPortletWindow = this.getPortletWindow(request, statelessPortletWindowId);
        if (statelessPortletWindow != null) {
            return statelessPortletWindow.getPortletWindowId();
        }
        
        //Lookup the base portlet window to clone the stateless from
        final IPortletWindow basePortletWindow = this.getPortletWindow(request, basePortletWindowId);
        
        //If no base to clone from lookup the entity and pluto definition data
        if (basePortletWindow == null) {
            final IPortletEntity portletEntity = this.portletEntityRegistry.getPortletEntity(request, portletEntityId);
            if (portletEntity == null) {
                throw new IllegalArgumentException("No IPortletEntity could be found for " + portletEntity + " while creating stateless portlet window for " + basePortletWindowId);
            }
            
            final IPortletDefinition portletDefinition = portletEntity.getPortletDefinition();
            final IPortletDefinitionId portletDefinitionId = portletDefinition.getPortletDefinitionId();
            final PortletDefinition portletDescriptor = this.portletDefinitionRegistry.getParentPortletDescriptor(portletDefinitionId);
            
            statelessPortletWindow = new StatelessPortletWindowImpl(statelessPortletWindowId, portletEntity, portletDescriptor);
        }
        //Clone the existing base window
        else {
            statelessPortletWindow = new StatelessPortletWindowImpl(statelessPortletWindowId, basePortletWindow);
        }
        
        //Cache the stateless window in the request
        final Map<IPortletWindowId, IPortletWindow> portletWindowMap = this.getPortletWindowMap(request);
        portletWindowMap.put(statelessPortletWindow.getPortletWindowId(), statelessPortletWindow);
        
        return statelessPortletWindow.getPortletWindowId();
    }
    
    @Override
    public void storePortletWindow(HttpServletRequest request, IPortletWindow portletWindow) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IPerson person = userInstance.getPerson();
        if (person.isGuest()) {
            //Never persist things for the guest user, just rely on in-memory storage
            return;
        }
        
        final IStylesheetDescriptor stylesheetDescriptor = this.getStylesheetDescriptor(request);
        
        final WindowState windowState = portletWindow.getWindowState();
        
        final IPortletEntity portletEntity = portletWindow.getPortletEntity();
        final WindowState entityWindowState = portletEntity.getWindowState(stylesheetDescriptor);
        
        //If the window and entity states are different
        if (windowState != entityWindowState && !windowState.equals(entityWindowState)) {
            final boolean minimizeByDefault = this.getMinimizeByDefault(stylesheetDescriptor);
            //If a window state is set and is one of the persistent states set it on the entity
            if (persistentWindowStates.contains(windowState) && (!WindowState.MINIMIZED.equals(windowState) || (WindowState.MINIMIZED.equals(windowState) && !minimizeByDefault))) {
                portletEntity.setWindowState(stylesheetDescriptor, windowState);
            }
            //If not remove the state from the entity
            else if (entityWindowState != null) {
                portletEntity.setWindowState(stylesheetDescriptor, null);
            }
            
            //Persist the modified entity
            this.portletEntityRegistry.storePortletEntity(request, portletEntity);
        }
    }
    
    @Override
    public Set<IPortletWindow> getAllLayoutPortletWindows(HttpServletRequest request) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();
        final Set<String> allSubscribedChannels = userLayoutManager.getAllSubscribedChannels();
        
        final Set<IPortletWindow> allLayoutWindows = new LinkedHashSet<IPortletWindow>(allSubscribedChannels.size());

        for (final String channelSubscribeId : allSubscribedChannels) {
            final IPortletEntity portletEntity = this.portletEntityRegistry.getOrCreatePortletEntity(request, userInstance, channelSubscribeId);
            
            final IPortletEntityId portletEntityId = portletEntity.getPortletEntityId();
            final IPortletWindow portletWindow = this.getOrCreateDefaultPortletWindow(request, portletEntityId);
            allLayoutWindows.add(portletWindow);
        }
        
        return allLayoutWindows;
    }
    
    @Override
    public Set<IPortletWindow> getAllPortletWindows(HttpServletRequest request) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();
        final Set<String> allSubscribedChannels = userLayoutManager.getAllSubscribedChannels();
        
        final Set<IPortletWindow> allLayoutWindows = new LinkedHashSet<IPortletWindow>(allSubscribedChannels.size());

        for (final String channelSubscribeId : allSubscribedChannels) {
            final IPortletEntity portletEntity = this.portletEntityRegistry.getOrCreatePortletEntity(request, userInstance, channelSubscribeId);
            
            final IPortletEntityId portletEntityId = portletEntity.getPortletEntityId();
            final Set<IPortletWindow> portletWindows = this.getAllPortletWindowsForEntity(request, portletEntityId);
            allLayoutWindows.addAll(portletWindows);
        }
        
        return allLayoutWindows;
    }

    protected IPortletWindow wrapPortletWindowData(HttpServletRequest request, PortletWindowData portletWindowData) {
        final IPortletEntityId portletEntityId = portletWindowData.getPortletEntityId();
        final IPortletEntity portletEntity = this.portletEntityRegistry.getPortletEntity(request, portletEntityId);
        final IPortletDefinition portletDefinition = portletEntity.getPortletDefinition();
        final PortletDefinition portletDescriptor = this.portletDefinitionRegistry.getParentPortletDescriptor(portletDefinition.getPortletDefinitionId());
        final IPortletWindow portletWindow = new PortletWindowImpl(portletDescriptor, portletEntity, portletWindowData);
        
        logger.trace("Wrapping PortletWindowData {} as IPortletWindow", portletWindow.getPortletWindowId());
        
        return portletWindow;
    }

    protected PortletWindowData getPortletWindowData(HttpServletRequest request, IPortletWindowId portletWindowId) {
        final ConcurrentMap<IPortletWindowId, PortletWindowData> portletWindowDataMap = getPortletWindowDataMap(request, false);
        if (portletWindowDataMap == null) {
            return null;
        }
        
        final PortletWindowData portletWindowData = portletWindowDataMap.get(portletWindowId);
        if (portletWindowData == null) {
            logger.trace("No PortletWindowData {} in session cache", portletWindowId);
            return null;
        }
        
        logger.trace("Found PortletWindowData {} in session cache", portletWindowData.getPortletWindowId());
        return portletWindowData;
    }

    protected Map<IPortletWindowId, IPortletWindow> getPortletWindowMap(HttpServletRequest request) {
        request = portalRequestUtils.getOriginalPortletOrPortalRequest(request);
        
        final String mapAttributeName = PORTLET_WINDOW_ATTRIBUTE + Thread.currentThread().getId();
        
        //No need to do this in a request attribute mutex since the map is scoped to a specific thread
        @SuppressWarnings("unchecked")
        Map<IPortletWindowId, IPortletWindow> windowMap = (Map<IPortletWindowId, IPortletWindow>)request.getAttribute(mapAttributeName);
        if (windowMap == null) {
            windowMap = new HashMap<IPortletWindowId, IPortletWindow>();
            request.setAttribute(mapAttributeName, windowMap);
        }
        
        return windowMap;
    }

    protected ConcurrentMap<IPortletWindowId, PortletWindowData> getPortletWindowDataMap(HttpServletRequest request) {
        return this.getPortletWindowDataMap(request, true);
    }
    
    protected ConcurrentMap<IPortletWindowId, PortletWindowData> getPortletWindowDataMap(HttpServletRequest request, boolean create) {
        request = portalRequestUtils.getOriginalPortalRequest(request);
        final HttpSession session = request.getSession(create);
        if (!create && session == null) {
            return null;
        }
        return PortalWebUtils.getMapSessionAttribute(session, PORTLET_WINDOW_DATA_ATTRIBUTE, create);
    }

    protected PortletWindowData getOrCreateDefaultPortletWindowData(HttpServletRequest request, IPortletEntityId portletEntityId, final IPortletWindowId portletWindowId) {
        //Sync on session map to make sure duplicate PortletWindowData is never created
        final ConcurrentMap<IPortletWindowId, PortletWindowData> portletWindowDataMap = getPortletWindowDataMap(request);
        //Check if there portlet window data cached in the session
        PortletWindowData portletWindowData = portletWindowDataMap.get(portletWindowId);
        if (portletWindowData != null) {
            logger.trace("Found PortletWindowData {} in session cache", portletWindowData.getPortletWindowId());
            return portletWindowData;
        }
        
        //Create new window data for and initialize
        portletWindowData = new PortletWindowData(portletWindowId, portletEntityId);                
        this.initializePortletWindowData(request, portletWindowData);
        
        //Store in the session cache
        portletWindowData = ConcurrentMapUtils.putIfAbsent(portletWindowDataMap, portletWindowId, portletWindowData);
        logger.trace("Created PortletWindowData {} and stored session cache, wrapping as IPortletWindow and returning", portletWindowData.getPortletWindowId());
        
        return portletWindowData;
    }

    /**
     * Initializes a newly created {@link PortletWindow}, the default implementation sets up the appropriate
     * {@link WindowState} and {@link javax.portlet.PortletMode}
     */
    protected void initializePortletWindowData(HttpServletRequest request, PortletWindowData portletWindowData) {
        final IStylesheetDescriptor stylesheetDescriptor = getStylesheetDescriptor(request);
        final boolean minimizePortletDefault = getMinimizeByDefault(stylesheetDescriptor);
        

        // TODO: figure out how to make this work without accidentally persisting the default minimized state
        if (minimizePortletDefault) {
            portletWindowData.setWindowState(WindowState.MINIMIZED);
        }
        else {
            final IPortletEntityId portletEntityId = portletWindowData.getPortletEntityId();
            final IPortletEntity portletEntity = this.portletEntityRegistry.getPortletEntity(request, portletEntityId);
            final WindowState entityWindowState = portletEntity.getWindowState(stylesheetDescriptor);
            if (persistentWindowStates.contains(entityWindowState)) {
                portletWindowData.setWindowState(entityWindowState);
            }
            else {
                //Set of persistent window states must have changed, nuke the old value
                this.logger.warn("PortletEntity.windowState=" + entityWindowState + " but that state is not in the set of persistent WindowStates. PortletEntity.windowState will be set to null");
                portletEntity.setWindowState(stylesheetDescriptor, null);
                this.portletEntityRegistry.storePortletEntity(request, portletEntity);
            }
        }
    }

    /**
     * @param stylesheetDescriptor
     * @return
     */
    protected boolean getMinimizeByDefault(final IStylesheetDescriptor stylesheetDescriptor) {
        final IStylesheetParameterDescriptor minimizePortletsDefaultParam = stylesheetDescriptor.getStylesheetParameterDescriptor("minimizePortletsDefault");
        
        if (minimizePortletsDefaultParam != null) {
            final String defaultValue = minimizePortletsDefaultParam.getDefaultValue();
            return Boolean.parseBoolean(defaultValue);
        }
        
        return false;
    }

    /**
     * @param request
     * @return
     */
    protected IStylesheetDescriptor getStylesheetDescriptor(HttpServletRequest request) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserProfile userProfile = preferencesManager.getUserProfile();
        final int themeStylesheetId = userProfile.getThemeStylesheetId();
        
        final IStylesheetDescriptor stylesheetDescriptor = stylesheetDescriptorDao.getStylesheetDescriptor(themeStylesheetId);
        return stylesheetDescriptor;
    }
    
    /**
     * Generates a new, unique, portlet window ID for the window instance ID & entity id.
     * 
     * @param windowInstanceId The window instance id.
     * @param portletEntityId The parent entity id.
     * @return A portlet window id for the parameters.
     */
    protected PortletWindowIdImpl createPortletWindowId(String windowInstanceId, IPortletEntityId portletEntityId) {
        final StringBuilder compositeIdString = new StringBuilder(portletEntityId.getStringId());
        
        if (windowInstanceId != null) {
            compositeIdString.append(ID_PART_SEPERATOR).append(windowInstanceId);
        }
        
        return new PortletWindowIdImpl(portletEntityId, windowInstanceId, compositeIdString.toString());
    }
}
