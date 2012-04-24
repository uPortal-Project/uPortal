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

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;

import org.apache.commons.lang.Validate;
import org.apache.pluto.container.PortletWindow;
import org.apache.pluto.container.PortletWindowID;
import org.apache.pluto.container.om.portlet.PortletDefinition;
import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.IUserProfile;
import org.jasig.portal.concurrency.caching.RequestCache;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.dao.IStylesheetDescriptorDao;
import org.jasig.portal.layout.om.IStylesheetDescriptor;
import org.jasig.portal.layout.om.IStylesheetParameterDescriptor;
import org.jasig.portal.portlet.PortletUtils;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.url.IPortalRequestInfo;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.url.IUrlSyntaxProvider;
import org.jasig.portal.url.UrlState;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.jasig.portal.utils.Tuple;
import org.jasig.portal.utils.web.PortalWebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;

import com.google.common.collect.Iterators;
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
    public static final QName PORTLET_WINDOW_ID_ATTR_NAME = new QName("portletWindowId");
    
    static final char ID_PART_SEPERATOR = '.';
    static final Pattern ID_PART_SEPERATOR_PATTERN = Pattern.compile(Pattern.quote(String.valueOf(ID_PART_SEPERATOR)));
    
    static final String STATELESS_PORTLET_WINDOW_ID = "tw";
    static final String PORTLET_WINDOW_DATA_ATTRIBUTE = PortletWindowRegistryImpl.class.getName() + ".PORTLET_WINDOW_DATA";
    static final String PORTLET_WINDOW_ATTRIBUTE = PortletWindowRegistryImpl.class.getName() + ".PORTLET_WINDOW.thread-";

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final XMLEventFactory xmlEventFactory = XMLEventFactory.newInstance();
    
    private Set<WindowState> persistentWindowStates = Sets.newHashSet(WindowState.MINIMIZED);
    private IPortletEntityRegistry portletEntityRegistry;
    private IPortletDefinitionRegistry portletDefinitionRegistry;
    private IStylesheetDescriptorDao stylesheetDescriptorDao;
    private IUserInstanceManager userInstanceManager;
    private IPortalRequestUtils portalRequestUtils;
    private IUrlSyntaxProvider urlSyntaxProvider;
    
    
    /**
     * The set of WindowStates that should be copied to the {@link IPortletEntity} when {@link #storePortletWindow(HttpServletRequest, IPortletWindow)}
     * is called
     */
    public void setPersistentWindowStates(Set<WindowState> persistentWindowStates) {
        this.persistentWindowStates = persistentWindowStates;
    }
    
    @Autowired
    public void setUrlSyntaxProvider(IUrlSyntaxProvider urlSyntaxProvider) {
        this.urlSyntaxProvider = urlSyntaxProvider;
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
        
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IPortletEntity portletEntity = this.portletEntityRegistry.getOrCreatePortletEntityByFname(request, userInstance, fname);
        if (portletEntity == null) {
            return null;
        }
        
        final IPortletWindow portletWindow = this.getOrCreateDefaultPortletWindow(request, portletEntity.getPortletEntityId());
        logger.trace("Found portlet window {} for portlet definition fname {}", portletWindow, fname);
        
        return portletWindow;
    }
    
    @Override
    public IPortletWindow getOrCreateDefaultPortletWindowByLayoutNodeId(HttpServletRequest request, String subscribeId) {
        Validate.notNull(request, "HttpServletRequest cannot be null");
        Validate.notNull(subscribeId, "subscribeId cannot be null");
        
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IPortletEntity portletEntity = this.portletEntityRegistry.getOrCreatePortletEntity(request, userInstance, subscribeId);
        if (portletEntity == null) {
            logger.debug("No portlet entity found for id {}, no IPortletWindow will be returned.", subscribeId);
            return null;
        }
        logger.trace("Found portlet entity {} for id {}", portletEntity, subscribeId);
        
        final IPortletWindow portletWindow = this.getOrCreateDefaultPortletWindow(request, portletEntity.getPortletEntityId());
        logger.trace("Found portlet window {} for layout node {}", portletWindow, subscribeId);
        
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

        final PortletWindowCache<IPortletWindow> portletWindowMap = getPortletWindowMap(request);
        
        //Check if there is portlet window cached in the request
        IPortletWindow portletWindow = portletWindowMap.getWindow(portletWindowId);
        if (portletWindow != null) {
            logger.trace("Found IPortletWindow {} in request cache", portletWindow.getPortletWindowId());
            return portletWindow;
        }
        
        final PortletWindowData portletWindowData = this.getOrCreateDefaultPortletWindowData(request, portletEntityId, portletWindowId);
        portletWindow = wrapPortletWindowData(request, portletWindowData);
        if (portletWindow == null) {
            return null;
        }
        
        //Cache the wrapped window in the request
        return portletWindowMap.storeIfAbsentWindow(portletWindow);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletWindowRegistry#getPortletWindow(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindowId)
     */
    @Override
    public IPortletWindow getPortletWindow(HttpServletRequest request, IPortletWindowId portletWindowId) {
        Validate.notNull(request, "request can not be null");
        Validate.notNull(portletWindowId, "portletWindowId can not be null");
        
        final PortletWindowCache<IPortletWindow> portletWindowMap = getPortletWindowMap(request);
        
        IPortletWindow portletWindow = portletWindowMap.getWindow(portletWindowId);
        if (portletWindow != null) {
            logger.trace("Found IPortletWindow {} in request cache", portletWindow.getPortletWindowId());
            return portletWindow;
        }
        
        final PortletWindowIdImpl localPortletWindowId = this.convertPortletWindowId(request, portletWindowId);
        
        //Find the window data from the correct window data map
        final PortletWindowData portletWindowData;
        if (STATELESS_PORTLET_WINDOW_ID.equals(localPortletWindowId.getWindowInstanceId())) {
            final PortletWindowCache<PortletWindowData> statelessPortletWindowDataMap = this.getStatelessPortletWindowDataMap(request, false);
            if (statelessPortletWindowDataMap != null) {
                portletWindowData = statelessPortletWindowDataMap.getWindow(portletWindowId);
            }
            else {
                portletWindowData = null;
            }
        }
        else {
            portletWindowData = this.getPortletWindowData(request, portletWindowId);
        }
        
        if (portletWindowData == null) {
            logger.trace("No IPortletWindow {} exists, returning null");
            return null;
        }
        
        portletWindow = this.wrapPortletWindowData(request, portletWindowData);
        
        //Cache the wrapped window in the request
        return portletWindowMap.storeIfAbsentWindow(portletWindow);
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
        Validate.notNull(request, "request can not be null");
        Validate.notNull(portletEntityId, "portletEntityId can not be null");
        
        //TODO does a delegate portlet entity need some sort of special treatment or do we just assume that the calling code is using one?
        
        final IPortletWindowId portletWindowId = this.getDefaultPortletWindowId(request, portletEntityId);

        final PortletWindowCache<IPortletWindow> portletWindowMap = getPortletWindowMap(request);
        
        //Check if there is portlet window cached in the request
        IPortletWindow portletWindow = portletWindowMap.getWindow(portletWindowId);
        if (portletWindow != null) {
            logger.trace("Found IPortletWindow {} in request cache", portletWindow.getPortletWindowId());
            return portletWindow;
        }
        
        final PortletWindowData portletWindowData = this.getOrCreateDefaultPortletWindowData(request, portletEntityId, portletWindowId, delegationParentId);
        portletWindow = wrapPortletWindowData(request, portletWindowData);
        if (portletWindow == null) {
            return null;
        }
        
        //Cache the wrapped window in the request
        return portletWindowMap.storeIfAbsentWindow(portletWindow);
    }
    
    @Override
    public Set<IPortletWindow> getAllPortletWindowsForEntity(HttpServletRequest request, IPortletEntityId portletEntityId) {
        Validate.notNull(request, "request can not be null");
        Validate.notNull(portletEntityId, "portletEntityId can not be null");
        
        
        final PortletWindowCache<IPortletWindow> portletWindowMap = getPortletWindowMap(request);
        final Set<IPortletWindow> portletWindows = new LinkedHashSet<IPortletWindow>(portletWindowMap.getWindows(portletEntityId));
        
        //Check for session cached windows that haven't been accessed in this request
        final PortletWindowCache<PortletWindowData> portletWindowDataMap = this.getPortletWindowDataMap(request);
        this.addPortletWindowData(request, portletEntityId, portletWindows, portletWindowMap, portletWindowDataMap);

        //Check for stateless windows that exist on this request
        final PortletWindowCache<PortletWindowData> statelessPortletWindowDataMap = this.getStatelessPortletWindowDataMap(request, false);
        if (statelessPortletWindowDataMap != null) {
            this.addPortletWindowData(request, portletEntityId, portletWindows, portletWindowMap, statelessPortletWindowDataMap);
        }
        
        //If there were no windows in the set create the default one for the entity
        if (portletWindows.isEmpty()) {
            final IPortletWindow portletWindow = this.getOrCreateDefaultPortletWindow(request, portletEntityId);
            portletWindows.add(portletWindow);
        }
            
        return portletWindows;
    }
    
    @Override
    public Tuple<IPortletWindow, StartElement> getPortletWindow(HttpServletRequest request, StartElement element) {
        //Check if the layout node explicitly specifies the window id
        final Attribute windowIdAttribute = element.getAttributeByName(PORTLET_WINDOW_ID_ATTR_NAME);
        if (windowIdAttribute != null) {
            final String windowIdStr = windowIdAttribute.getValue();
            final IPortletWindowId portletWindowId = this.getPortletWindowId(request, windowIdStr);
            final IPortletWindow portletWindow = this.getPortletWindow(request, portletWindowId);
            return new Tuple<IPortletWindow, StartElement>(portletWindow, element);
        }

        //No explicit window id, look it up based on the layout node id
        final Attribute nodeIdAttribute = element.getAttributeByName(IUserLayoutManager.ID_ATTR_NAME);
        final String layoutNodeId = nodeIdAttribute.getValue();

        IPortletWindow portletWindow = this.getOrCreateDefaultPortletWindowByLayoutNodeId(request, layoutNodeId);
        if (portletWindow == null) {
            //No window for the layout node, return null
            return null;
        }

        final IPortalRequestInfo portalRequestInfo = this.urlSyntaxProvider.getPortalRequestInfo(request);
        if (portalRequestInfo.getUrlState() == UrlState.DETACHED) {
            //Handle detached portlets explicitly
            //TODO Can we ever have non-targeted portlets render in a detached request? If so should they all be stateless windows anyways? 
            final IPortletWindowId portletWindowId = portletWindow.getPortletWindowId();
            portletWindow = this.getOrCreateStatelessPortletWindow(request, portletWindowId);
        }
        
        element = this.addPortletWindowId(element, portletWindow.getPortletWindowId());
        
        return new Tuple<IPortletWindow, StartElement>(portletWindow, element);
    }
    
    protected StartElement addPortletWindowId(StartElement element, IPortletWindowId portletWindowId) {
        final Attribute windowIdAttribute = xmlEventFactory.createAttribute(PORTLET_WINDOW_ID_ATTR_NAME, portletWindowId.getStringId());
        
        //Clone the start element to add the new attribute
        final QName name = element.getName();
        final String prefix = name.getPrefix();
        final String namespaceURI = name.getNamespaceURI();
        final String localPart = name.getLocalPart();
        @SuppressWarnings("unchecked")
        final Iterator<Attribute> attributes = element.getAttributes();
        @SuppressWarnings("unchecked")
        final Iterator<Namespace> namespaces = element.getNamespaces();
        final NamespaceContext namespaceContext = element.getNamespaceContext();
        
        //Create a new iterator of the existing attributes + the new window id attribute
        final Iterator<Attribute> newAttributes = Iterators.concat(attributes, Iterators.forArray(windowIdAttribute));
        
        return xmlEventFactory.createStartElement(prefix, namespaceURI, localPart, newAttributes, namespaces, namespaceContext);
    }

    /**
     * @param request
     * @param portletEntityId
     * @param portletWindows
     * @param portletWindowMap
     * @param portletWindowDataMap
     */
    protected void addPortletWindowData(HttpServletRequest request, IPortletEntityId portletEntityId,
            final Set<IPortletWindow> portletWindows, final PortletWindowCache<IPortletWindow> portletWindowMap,
            final PortletWindowCache<PortletWindowData> portletWindowDataMap) {
        
        final Set<PortletWindowData> windows = portletWindowDataMap.getWindows(portletEntityId);
        if (windows == null) {
            return;
        }
        
        for (final PortletWindowData portletWindowData : windows) {
            final IPortletWindowId portletWindowId = portletWindowData.getPortletWindowId();
            
            //Skip data windows that aren't for this entity and for windows that are already in the request cache
            if (!portletEntityId.equals(portletWindowData.getPortletEntityId()) || portletWindowMap.containsWindow(portletWindowId)) {
                continue;
            }

            //Wrap the data in a window and stick it in the request cache
            IPortletWindow portletWindow = this.wrapPortletWindowData(request, portletWindowData);
            portletWindow = portletWindowMap.storeIfAbsentWindow(portletWindow);
            
            portletWindows.add(portletWindow);
        }
    }
    
    @Override
    public IPortletWindow getOrCreateStatelessPortletWindow(HttpServletRequest request, IPortletWindowId basePortletWindowId) {
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
            return statelessPortletWindow;
        }
        
        //Lookup the base portlet window to clone the stateless from
        final IPortletWindow basePortletWindow = this.getPortletWindow(request, basePortletWindowId);
        
        final PortletWindowCache<PortletWindowData> statelessPortletWindowDataMap = this.getStatelessPortletWindowDataMap(request, true);
        
        //If no base to clone from lookup the entity and pluto definition data
        if (basePortletWindow == null) {
            final IPortletEntity portletEntity = this.portletEntityRegistry.getPortletEntity(request, portletEntityId);
            if (portletEntity == null) {
                throw new IllegalArgumentException("No IPortletEntity could be found for " + portletEntity + " while creating stateless portlet window for " + basePortletWindowId);
            }
            
            final IPortletDefinition portletDefinition = portletEntity.getPortletDefinition();
            final IPortletDefinitionId portletDefinitionId = portletDefinition.getPortletDefinitionId();
            final PortletDefinition portletDescriptor = this.portletDefinitionRegistry.getParentPortletDescriptor(portletDefinitionId);
            
            final PortletWindowData portletWindowData = new PortletWindowData(statelessPortletWindowId, portletEntityId);
            statelessPortletWindowDataMap.storeWindow(portletWindowData);
            
            statelessPortletWindow = new StatelessPortletWindowImpl(portletWindowData, portletEntity, portletDescriptor);
        }
        //Clone the existing base window
        else {
            final PortletWindowData portletWindowData = new PortletWindowData(statelessPortletWindowId, portletEntityId);
            portletWindowData.setExpirationCache(basePortletWindow.getExpirationCache());
            portletWindowData.setPortletMode(basePortletWindow.getPortletMode());
            portletWindowData.setWindowState(basePortletWindow.getWindowState());
            portletWindowData.setPublicRenderParameters(basePortletWindow.getPublicRenderParameters());
            portletWindowData.setRenderParameters(basePortletWindow.getRenderParameters());
            
            statelessPortletWindowDataMap.storeWindow(portletWindowData);

            final IPortletEntity portletEntity = basePortletWindow.getPortletEntity();
            final PortletDefinition portletDescriptor = basePortletWindow.getPlutoPortletWindow().getPortletDefinition();
            statelessPortletWindow = new StatelessPortletWindowImpl(portletWindowData, portletEntity, portletDescriptor);
        }
        
        //Cache the stateless window in the request
        final PortletWindowCache<IPortletWindow> portletWindowMap = this.getPortletWindowMap(request);
        portletWindowMap.storeWindow(statelessPortletWindow);
        
        return statelessPortletWindow;
    }
    
    @Override
    public void storePortletWindow(HttpServletRequest request, IPortletWindow portletWindow) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IPerson person = userInstance.getPerson();
        if (person.isGuest()) {
            //Never persist things for the guest user, just rely on in-memory storage
            return;
        }
        
        final IStylesheetDescriptor themeStylesheetDescriptor = this.getThemeStylesheetDescriptor(request);
        
        final WindowState windowState = portletWindow.getWindowState();
        
        final IPortletEntity portletEntity = portletWindow.getPortletEntity();
        final WindowState entityWindowState = portletEntity.getWindowState(themeStylesheetDescriptor);
        
        //If the window and entity states are different
        if (windowState != entityWindowState && !windowState.equals(entityWindowState)) {
            final WindowState defaultWindowState = this.getDefaultWindowState(themeStylesheetDescriptor);
            
            //If a window state is set and is one of the persistent states set it on the entity
            if (!defaultWindowState.equals(windowState) && persistentWindowStates.contains(windowState)) {
                portletEntity.setWindowState(themeStylesheetDescriptor, windowState);
            }
            //If not remove the state from the entity
            else if (entityWindowState != null) {
                portletEntity.setWindowState(themeStylesheetDescriptor, null);
            }
            
            //Persist the modified entity
            this.portletEntityRegistry.storePortletEntity(request, portletEntity);
        }
    }
    
    @Override
    @RequestCache(keyMask = {false})
    public Set<IPortletWindow> getAllLayoutPortletWindows(HttpServletRequest request) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();
        final Set<String> allSubscribedChannels = userLayoutManager.getAllSubscribedChannels();
        
        final Set<IPortletWindow> allLayoutWindows = new LinkedHashSet<IPortletWindow>(allSubscribedChannels.size());

        for (final String channelSubscribeId : allSubscribedChannels) {
            final IPortletEntity portletEntity = this.portletEntityRegistry.getOrCreatePortletEntity(request, userInstance, channelSubscribeId);
            if (portletEntity == null) {
                this.logger.debug("No portlet entity found for layout node {} for user {}", channelSubscribeId, userInstance.getPerson().getUserName());
                continue;
            }
            
            final IPortletEntityId portletEntityId = portletEntity.getPortletEntityId();
            final IPortletWindow portletWindow = this.getOrCreateDefaultPortletWindow(request, portletEntityId);
            if (portletWindow == null) {
                this.logger.debug("No portlet window found for {}", portletEntity);
                continue;
            }
            
        	allLayoutWindows.add(portletWindow);
        }
        
        return allLayoutWindows;
    }
    
    @Override
    @RequestCache(keyMask = {false})
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
    
    protected PortletWindowIdImpl convertPortletWindowId(HttpServletRequest request, IPortletWindowId portletWindowId) {
        if (portletWindowId instanceof PortletWindowIdImpl) {
            return (PortletWindowIdImpl)portletWindowId;
        }
        
        return this.getPortletWindowId(request, portletWindowId.getStringId());
    }

    protected IPortletWindow wrapPortletWindowData(HttpServletRequest request, PortletWindowData portletWindowData) {
        final IPortletEntityId portletEntityId = portletWindowData.getPortletEntityId();
        final IPortletEntity portletEntity = this.portletEntityRegistry.getPortletEntity(request, portletEntityId);
        if (portletEntity == null) {
            return null;
        }
        
        final IPortletDefinition portletDefinition = portletEntity.getPortletDefinition();
        final PortletDefinition portletDescriptor = this.portletDefinitionRegistry.getParentPortletDescriptor(portletDefinition.getPortletDefinitionId());
        if (portletDescriptor == null) {
            return null;
        }
        
        final IPortletWindow portletWindow = new PortletWindowImpl(portletDescriptor, portletEntity, portletWindowData);
        
        logger.trace("Wrapping PortletWindowData {} as IPortletWindow", portletWindow.getPortletWindowId());
        
        return portletWindow;
    }

    protected PortletWindowData getPortletWindowData(HttpServletRequest request, IPortletWindowId portletWindowId) {
        final PortletWindowCache<PortletWindowData> portletWindowDataMap = getPortletWindowDataMap(request, false);
        if (portletWindowDataMap == null) {
            return null;
        }
        
        final PortletWindowData portletWindowData = portletWindowDataMap.getWindow(portletWindowId);
        if (portletWindowData == null) {
            logger.trace("No PortletWindowData {} in session cache", portletWindowId);
            return null;
        }
        
        logger.trace("Found PortletWindowData {} in session cache", portletWindowData.getPortletWindowId());
        return portletWindowData;
    }

    protected PortletWindowCache<IPortletWindow> getPortletWindowMap(HttpServletRequest request) {
        request = portalRequestUtils.getOriginalPortletOrPortalRequest(request);
        
        final String mapAttributeName = PORTLET_WINDOW_ATTRIBUTE + Thread.currentThread().getId();
        
        //No need to do this in a request attribute mutex since the map is scoped to a specific thread
        @SuppressWarnings("unchecked")
        PortletWindowCache<IPortletWindow> windowCache = (PortletWindowCache<IPortletWindow>)request.getAttribute(mapAttributeName);
        if (windowCache == null) {
            windowCache = new PortletWindowCache<IPortletWindow>(false);
            request.setAttribute(mapAttributeName, windowCache);
        }
        
        return windowCache;
    }

    protected PortletWindowCache<PortletWindowData> getPortletWindowDataMap(HttpServletRequest request) {
        return this.getPortletWindowDataMap(request, true);
    }
    
    @SuppressWarnings("unchecked")
    protected PortletWindowCache<PortletWindowData> getPortletWindowDataMap(HttpServletRequest request, boolean create) {
        request = portalRequestUtils.getOriginalPortalRequest(request);
        final HttpSession session = request.getSession(create);
        if (!create && session == null) {
            return null;
        }
        
        PortletWindowCache<PortletWindowData> windowCache;
        
        final Object mutex = WebUtils.getSessionMutex(session);
        synchronized (mutex) {
            windowCache = (PortletWindowCache<PortletWindowData>)session.getAttribute(PORTLET_WINDOW_DATA_ATTRIBUTE);
            if (windowCache == null) {
                windowCache = new PortletWindowCache<PortletWindowData>();
                session.setAttribute(PORTLET_WINDOW_DATA_ATTRIBUTE, windowCache);
            }
        }
        
        return windowCache;
    }
    
    @SuppressWarnings("unchecked")
    protected PortletWindowCache<PortletWindowData> getStatelessPortletWindowDataMap(HttpServletRequest request, boolean create) {
        request = portalRequestUtils.getOriginalPortalRequest(request);
        
        PortletWindowCache<PortletWindowData> windowCache;
        
        final Object mutex = PortalWebUtils.getRequestAttributeMutex(request);
        synchronized (mutex) {
            windowCache = (PortletWindowCache<PortletWindowData>)request.getAttribute(PORTLET_WINDOW_DATA_ATTRIBUTE);
            if (windowCache == null) {
                windowCache = new PortletWindowCache<PortletWindowData>();
                request.setAttribute(PORTLET_WINDOW_DATA_ATTRIBUTE, windowCache);
            }
        }
        
        return windowCache;
    }
    
    protected PortletWindowData getOrCreateDefaultPortletWindowData(HttpServletRequest request, IPortletEntityId portletEntityId, IPortletWindowId portletWindowId) {
        return getOrCreateDefaultPortletWindowData(request, portletEntityId, portletWindowId, null);
    }

    protected PortletWindowData getOrCreateDefaultPortletWindowData(HttpServletRequest request, IPortletEntityId portletEntityId, IPortletWindowId portletWindowId, IPortletWindowId delegationParentId) {
        //Sync on session map to make sure duplicate PortletWindowData is never created
        final PortletWindowCache<PortletWindowData> portletWindowDataMap = getPortletWindowDataMap(request);
        //Check if there portlet window data cached in the session
        PortletWindowData portletWindowData = portletWindowDataMap.getWindow(portletWindowId);
        if (portletWindowData != null) {
            logger.trace("Found PortletWindowData {} in session cache", portletWindowData.getPortletWindowId());
            return portletWindowData;
        }
        
        //Create new window data for and initialize
        portletWindowData = new PortletWindowData(portletWindowId, portletEntityId, delegationParentId);
        this.initializePortletWindowData(request, portletWindowData);
        
        //Store in the session cache
        portletWindowData = portletWindowDataMap.storeIfAbsentWindow(portletWindowData);
        logger.trace("Created PortletWindowData {} and stored session cache, wrapping as IPortletWindow and returning", portletWindowData.getPortletWindowId());
        
        return portletWindowData;
    }

    /**
     * Initializes a newly created {@link PortletWindow}, the default implementation sets up the appropriate
     * {@link WindowState} and {@link javax.portlet.PortletMode}
     */
    protected void initializePortletWindowData(HttpServletRequest request, PortletWindowData portletWindowData) {
        final IStylesheetDescriptor stylesheetDescriptor = getThemeStylesheetDescriptor(request);
        final IPortletEntityId portletEntityId = portletWindowData.getPortletEntityId();
        final IPortletEntity portletEntity = this.portletEntityRegistry.getPortletEntity(request, portletEntityId);
        final WindowState entityWindowState = portletEntity.getWindowState(stylesheetDescriptor);
        if (persistentWindowStates.contains(entityWindowState)) {
            portletWindowData.setWindowState(entityWindowState);
        }
        else if (entityWindowState != null) {
            //Set of persistent window states must have changed, nuke the old value
            this.logger.warn("PortletEntity.windowState=" + entityWindowState + " but that state is not in the set of persistent WindowStates. PortletEntity.windowState will be set to null");
            portletEntity.setWindowState(stylesheetDescriptor, null);
            this.portletEntityRegistry.storePortletEntity(request, portletEntity);
        }
    }

    protected WindowState getDefaultWindowState(final IStylesheetDescriptor stylesheetDescriptor) {
        final IStylesheetParameterDescriptor defaultWindowStateParam = stylesheetDescriptor.getStylesheetParameterDescriptor("dashboardForcedWindowState");
        
        if (defaultWindowStateParam != null) {
            return PortletUtils.getWindowState(defaultWindowStateParam.getDefaultValue());
        }
        
        return WindowState.NORMAL;
    }

    protected IStylesheetDescriptor getThemeStylesheetDescriptor(HttpServletRequest request) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserProfile userProfile = preferencesManager.getUserProfile();
        final int themeStylesheetId = userProfile.getThemeStylesheetId();
        
        return stylesheetDescriptorDao.getStylesheetDescriptor(themeStylesheetId);
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
