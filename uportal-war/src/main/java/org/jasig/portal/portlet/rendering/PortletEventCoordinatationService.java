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

package org.jasig.portal.portlet.rendering;

import java.io.Serializable;
import java.io.StringReader;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.portlet.Event;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.apache.commons.lang.StringUtils;
import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletContainerException;
import org.apache.pluto.container.PortletWindow;
import org.apache.pluto.container.driver.PortletContextService;
import org.apache.pluto.container.om.portlet.ContainerRuntimeOption;
import org.apache.pluto.container.om.portlet.EventDefinition;
import org.apache.pluto.container.om.portlet.EventDefinitionReference;
import org.apache.pluto.container.om.portlet.PortletApplicationDefinition;
import org.apache.pluto.container.om.portlet.PortletDefinition;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.portlet.container.EventImpl;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.jasig.portal.utils.Tuple;
import org.jasig.portal.utils.web.PortalWebUtils;
import org.jasig.portal.xml.XmlUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * uPortal's approach to event coordination is to simply queue the events and rely on the {@link IPortletExecutionManager}
 * to handle event execution. What this class does is for each {@link #processEvents(PortletContainer, PortletWindow, HttpServletRequest, HttpServletResponse, List)}
 * request from the portlet container is to add them to a Queue scoped to the portal's request.
 * 
 * It also provides {@link #getPortletEventQueue(PortletEventQueue, List, HttpServletRequest)} which is used to determine
 * which events to send to which portlet windows. 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service("eventCoordinationService")
public class PortletEventCoordinatationService implements IPortletEventCoordinationService {
    /**
	 * 
	 */
	public static final String GLOBAL_EVENT__CONTAINER_OPTION = "org.jasig.portal.globalEvent";

	private static final String PORTLET_EVENT_QUEUE = PortletEventCoordinatationService.class.getName() + ".PORTLET_EVENT_QUEUE";
    
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private IPortletWindowRegistry portletWindowRegistry;
    private IPortletEntityRegistry portletEntityRegistry;
    private IPortletDefinitionRegistry portletDefinitionRegistry;
    private IUserInstanceManager userInstanceManager;
    private Ehcache supportedEventCache;
    private IPortalRequestUtils portalRequestUtils;
    private XmlUtilities xmlUtilities;
    private PortletContextService portletContextService;
    
    @Autowired
    public void setPortletContextService(PortletContextService portletContextService) {
        this.portletContextService = portletContextService;
    }

    @Autowired
    public void setXmlUtilities(XmlUtilities xmlUtilities) {
        this.xmlUtilities = xmlUtilities;
    }

    @Autowired
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }

    @Autowired
    public void setSupportedEventCache(
            @Qualifier("org.jasig.portal.portlet.rendering.SupportedEventCache") Ehcache supportedEventCache) {
        this.supportedEventCache = supportedEventCache;
    }

    @Autowired
    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }

    @Autowired
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }

    @Autowired
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }

    @Autowired
    public void setPortletEntityRegistry(IPortletEntityRegistry portletEntityRegistry) {
        this.portletEntityRegistry = portletEntityRegistry;
    }

    /**
     * Returns a request scoped PortletEventQueue used to track events to process and events to dispatch
     */
    @Override
    public PortletEventQueue getPortletEventQueue(HttpServletRequest request) {
        request = this.portalRequestUtils.getOriginalPortalRequest(request);
        
        synchronized (PortalWebUtils.getRequestAttributeMutex(request)) {
            PortletEventQueue portletEventQueue = (PortletEventQueue)request.getAttribute(PORTLET_EVENT_QUEUE);
            if (portletEventQueue == null) {
                portletEventQueue = new PortletEventQueue();
                request.setAttribute(PORTLET_EVENT_QUEUE, portletEventQueue);
            }
            return portletEventQueue;
        }
    }
    
    @Override
    public void processEvents(PortletContainer container, PortletWindow plutoPortletWindow, HttpServletRequest request, HttpServletResponse response, List<Event> events) {
        final PortletEventQueue requestPortletEventQueue = this.getPortletEventQueue(request);
        this.logger.debug("Queued {} from {}", events, plutoPortletWindow);
        
        final IPortletWindow portletWindow = this.portletWindowRegistry.convertPortletWindow(request, plutoPortletWindow);
        final IPortletWindowId portletWindowId = portletWindow.getPortletWindowId();
        
        //Add list transformer to convert Event to QueuedEvent
        final List<QueuedEvent> queuedEvents = Lists.transform(events, new Function<Event, QueuedEvent>() {
			/* (non-Javadoc)
			 * @see com.google.common.base.Function#apply(java.lang.Object)
			 */
			@Override
			public QueuedEvent apply(Event event) {
				return new QueuedEvent(portletWindowId, event);
			}
		});
        
        requestPortletEventQueue.addEvents(queuedEvents);
    }
    
    
    @Override
    public void resolvePortletEvents(HttpServletRequest request, PortletEventQueue portletEventQueue) {
        final Queue<QueuedEvent> events = portletEventQueue.getUnresolvedEvents();
        
        //Skip all processing if there are no new events.
        if (events.isEmpty()) {
            return;
        }
        
        //Get all the portlets the user is subscribed to
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();
        
        //Make a local copy so we can remove data from it
        final Set<String> allLayoutNodeIds = new LinkedHashSet<String>(userLayoutManager.getAllSubscribedChannels());
        
        final Map<String, IPortletEntity> portletEntityCache = new LinkedHashMap<String, IPortletEntity>();
        
        while (!events.isEmpty()) {
            final QueuedEvent queuedEvent = events.poll();
            if (queuedEvent == null) {
                //no more queued events, done resolving
                return;
            }
            
            final IPortletWindowId sourceWindowId = queuedEvent.getPortletWindowId();
            final Event event = queuedEvent.getEvent();
            
        	final boolean globalEvent = isGlobalEvent(request, sourceWindowId, event);
        	
        	final Set<IPortletDefinition> portletDefinitions = new LinkedHashSet<IPortletDefinition>();
        	if (globalEvent) {
        		portletDefinitions.addAll(this.portletDefinitionRegistry.getAllPortletDefinitions());
        	}
            
            //Check each subscription to see what events it is registered to see
            for (final Iterator<String> layoutNodeIdItr = allLayoutNodeIds.iterator(); layoutNodeIdItr.hasNext(); ) {
                final String layoutNodeId = layoutNodeIdItr.next();
                
                IPortletEntity portletEntity = portletEntityCache.get(layoutNodeId);
                if (portletEntity == null) {
                    portletEntity = this.portletEntityRegistry.getOrCreatePortletEntity(request, userInstance, layoutNodeId);
                    
                    // if portlet entity registry returned null, then portlet has been deleted - remove it (see UP-3378)
                    if (portletEntity == null) {
                    	layoutNodeIdItr.remove();
                    	continue;
                    }
                    
                    final IPortletDefinitionId portletDefinitionId = portletEntity.getPortletDefinitionId();
                    final PortletDefinition portletDescriptor = this.portletDefinitionRegistry.getParentPortletDescriptor(portletDefinitionId);
                    if (portletDescriptor == null) {
                        //Missconfigured portlet, remove it from the list so we don't check again and ignore it
                        layoutNodeIdItr.remove();
                        continue;
                    }
                    
                    final List<? extends EventDefinitionReference> supportedProcessingEvents = portletDescriptor.getSupportedProcessingEvents();
                    //Skip portlets that don't handle any events and remove them from the set so they are not checked again
                    if (supportedProcessingEvents == null || supportedProcessingEvents.size() == 0) {
                        layoutNodeIdItr.remove();
                        continue;
                    }
                    
                    portletEntityCache.put(layoutNodeId, portletEntity);
                }
                
                final IPortletDefinition portletDefinition = portletEntity.getPortletDefinition();
                final IPortletDefinitionId portletDefinitionId = portletDefinition.getPortletDefinitionId();
                if (this.supportsEvent(event, portletDefinitionId)) {
                	this.logger.debug("{} supports event {}", portletDefinition, event);
                	
                	//If this is the default portlet entity remove the definition from the all defs set to avoid duplicate processing
                	final IPortletEntity defaultPortletEntity = this.portletEntityRegistry.getOrCreateDefaultPortletEntity(request, portletDefinitionId);
                	if (defaultPortletEntity.equals(portletEntity)) {
                		portletDefinitions.remove(portletDefinition);
                	}
                	
                    final IPortletEntityId portletEntityId = portletEntity.getPortletEntityId();
                    final Set<IPortletWindow> portletWindows = this.portletWindowRegistry.getAllPortletWindowsForEntity(request, portletEntityId);
                    
                    for (final IPortletWindow portletWindow : portletWindows) {
                        this.logger.debug("{} resolved target {}", event, portletWindow);
                        final IPortletWindowId portletWindowId = portletWindow.getPortletWindowId();
                        final Event unmarshalledEvent = this.unmarshall(portletWindow, event);
                        portletEventQueue.offerEvent(portletWindowId, new QueuedEvent(sourceWindowId, unmarshalledEvent) );
                    }
                }
                else {
                	portletDefinitions.remove(portletDefinition);
                }
            }
            
            if (!portletDefinitions.isEmpty()) {
            	final IPerson user = userInstance.getPerson();
        		final EntityIdentifier ei = user.getEntityIdentifier();
        		final IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
            	
	            //If the event is global there might still be portlet definitions that need targeting
	            for (final IPortletDefinition portletDefinition : portletDefinitions) {
	            	final IPortletDefinitionId portletDefinitionId = portletDefinition.getPortletDefinitionId();
	            	//Check if the user can render the portlet definition before doing event tests
	            	if (ap.canRender(portletDefinitionId.getStringId())) {
		            	if (this.supportsEvent(event, portletDefinitionId)) {
		            		this.logger.debug("{} supports event {}", portletDefinition, event);
		                	
		                	final IPortletEntity portletEntity = this.portletEntityRegistry.getOrCreateDefaultPortletEntity(request, portletDefinitionId);
		                    final IPortletEntityId portletEntityId = portletEntity.getPortletEntityId();
		                    final Set<IPortletWindow> portletWindows = this.portletWindowRegistry.getAllPortletWindowsForEntity(request, portletEntityId);
		                    
		                    for (final IPortletWindow portletWindow : portletWindows) {
		                        this.logger.debug("{} resolved target {}", event, portletWindow);
		                        final IPortletWindowId portletWindowId = portletWindow.getPortletWindowId();
		                        final Event unmarshalledEvent = this.unmarshall(portletWindow, event);
		                        portletEventQueue.offerEvent(portletWindowId, new QueuedEvent(sourceWindowId, unmarshalledEvent) );
		                    }
		            	}
	            	}
	            }
            }
        }
    }

	protected boolean isGlobalEvent(HttpServletRequest request, IPortletWindowId sourceWindowId, Event event) {
		final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(request, sourceWindowId);
		final IPortletEntity portletEntity = portletWindow.getPortletEntity();
		final IPortletDefinition portletDefinition = portletEntity.getPortletDefinition();
		final IPortletDefinitionId portletDefinitionId = portletDefinition.getPortletDefinitionId();
		final PortletApplicationDefinition parentPortletApplicationDescriptor = this.portletDefinitionRegistry.getParentPortletApplicationDescriptor(portletDefinitionId);
		
		final ContainerRuntimeOption globalEvents = parentPortletApplicationDescriptor.getContainerRuntimeOption(GLOBAL_EVENT__CONTAINER_OPTION);
		if (globalEvents != null) {
			final QName qName = event.getQName();
			final String qNameStr = qName.toString();
			for (final String globalEvent : globalEvents.getValues()) {
				if (qNameStr.equals(globalEvent)) {
					return true;
				}
			}
		}
		
		return false;
	}

    protected Event unmarshall(IPortletWindow portletWindow, Event event) {
        //TODO make two types of Event impls, one for marshalled data and one for unmarshalled data
        String value = (String)event.getValue();
        
        final XMLInputFactory xmlInputFactory = this.xmlUtilities.getXmlInputFactory();
        final XMLStreamReader xml;
        try {
            xml = xmlInputFactory.createXMLStreamReader(new StringReader(value));
        }
        catch (XMLStreamException e) {
            throw new IllegalStateException("Failed to create XMLStreamReader for portlet event: " + event, e);
        }
        
        // now test if object is jaxb
        final EventDefinition eventDefinitionDD = getEventDefintion(portletWindow, event.getQName()); 
        
        final PortletDefinition portletDefinition = portletWindow.getPlutoPortletWindow().getPortletDefinition();
        final PortletApplicationDefinition application = portletDefinition.getApplication();
        final String portletApplicationName = application.getName();
        
        final ClassLoader loader;
        try {
            loader = portletContextService.getClassLoader(portletApplicationName);
        }
        catch (PortletContainerException e) {
            throw new IllegalStateException("Failed to get ClassLoader for portlet application: " + portletApplicationName, e);
        }
        
        final String eventType = eventDefinitionDD.getValueType();
        final Class<? extends Serializable> clazz;
        try {
            clazz = loader.loadClass(eventType).asSubclass(Serializable.class);
        }
        catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Declared event type '" + eventType + "' cannot be found in portlet application: " + portletApplicationName, e);
        }
    
        //TODO cache JAXBContext in registered portlet application
        final JAXBElement<? extends Serializable> result;
        try {
            final JAXBContext jc = JAXBContext.newInstance(clazz);
            final Unmarshaller unmarshaller = jc.createUnmarshaller();
            result = unmarshaller.unmarshal(xml, clazz);
        }
        catch (JAXBException e) {
            throw new IllegalArgumentException("Cannot create JAXBContext for event type '" + eventType + "' from portlet application: " + portletApplicationName, e);
        }
    
        return new EventImpl(event.getQName(), result.getValue());
    }

    //TODO cache this resolution
    protected EventDefinition getEventDefintion(IPortletWindow portletWindow, QName name) {
        PortletApplicationDefinition appDD = portletWindow.getPlutoPortletWindow().getPortletDefinition().getApplication();
        for (EventDefinition def : appDD.getEventDefinitions()) {
            if (def.getQName() != null) {
                if (def.getQName().equals(name))
                    return def;
            }
            else {
                QName tmp = new QName(appDD.getDefaultNamespace(), def.getName());
                if (tmp.equals(name))
                    return def;
            }
        }
        throw new IllegalStateException();
    }

    protected Set<QName> getAllAliases(QName eventName, PortletApplicationDefinition portletApplicationDefinition) {
        final List<? extends EventDefinition> eventDefinitions = portletApplicationDefinition.getEventDefinitions();
        if (eventDefinitions == null || eventDefinitions.isEmpty()) {
            return Collections.emptySet();
        }

        final String defaultNamespace = portletApplicationDefinition.getDefaultNamespace();
        
        for (final EventDefinition eventDefinition : eventDefinitions) {
            final QName defQName = eventDefinition.getQualifiedName(defaultNamespace);
            if (defQName != null && defQName.equals(eventName)) {
                final List<QName> aliases = eventDefinition.getAliases();
                if (aliases == null || aliases.isEmpty()) {
                    return Collections.emptySet();
                }
                    
                return new LinkedHashSet<QName>(aliases);
            }
        }

        return Collections.emptySet();
    }
    
    protected boolean supportsEvent(Event event, IPortletDefinitionId portletDefinitionId) {
        final QName eventName = event.getQName();
        
        //The cache key to use
        final Tuple<IPortletDefinitionId, QName> key = new Tuple<IPortletDefinitionId, QName>(portletDefinitionId, eventName);
        
        //Check in the cache if the portlet definition supports this event
        final Element element = this.supportedEventCache.get(key);
        if (element != null) {
            final Boolean supported = (Boolean)element.getValue();
            if (supported != null) {
                return supported;
            }
        }

        final PortletApplicationDefinition portletApplicationDescriptor = this.portletDefinitionRegistry.getParentPortletApplicationDescriptor(portletDefinitionId);
        if (portletApplicationDescriptor == null) {
        	return false;
        }
        
        final Set<QName> aliases = this.getAllAliases(eventName, portletApplicationDescriptor);
        
        final String defaultNamespace = portletApplicationDescriptor.getDefaultNamespace();
        
        //No support found so far, do more complex namespace matching
        final PortletDefinition portletDescriptor = this.portletDefinitionRegistry.getParentPortletDescriptor(portletDefinitionId);
        if (portletDescriptor == null) {
        	return false;
        }

        final List<? extends EventDefinitionReference> supportedProcessingEvents = portletDescriptor.getSupportedProcessingEvents();
        for (final EventDefinitionReference eventDefinitionReference : supportedProcessingEvents) {
            final QName qualifiedName = eventDefinitionReference.getQualifiedName(defaultNamespace);
            if (qualifiedName == null) {
                continue;
            }
            
            //See if the supported qname and event qname match explicitly
            //Look for alias names
            if (qualifiedName.equals(eventName) || aliases.contains(qualifiedName)) {
                this.supportedEventCache.put(new Element(key, Boolean.TRUE));
                return true;
            }
            
            //Look for namespaced events
            if (StringUtils.isEmpty(qualifiedName.getNamespaceURI())) {
                final QName namespacedName = new QName(defaultNamespace, qualifiedName.getLocalPart());
                if (eventName.equals(namespacedName)) {
                    this.supportedEventCache.put(new Element(key, Boolean.TRUE));
                    return true;
                }
            }
        }
        

        this.supportedEventCache.put(new Element(key, Boolean.FALSE));
        return false;
    }
}
